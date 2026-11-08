#!/usr/bin/env python3
"""Audit a Vidya visual-parts JSONL manifest using only the Python standard library.

Checks metadata, task/annotation compatibility, file hashes, duplicate files,
augmentation ancestry and capture-group split leakage. It does not validate
semantic label correctness or grant permission to use a source dataset.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
from collections import Counter, defaultdict
from pathlib import Path

REQUIRED = (
    "sample_id", "source_dataset", "source_version", "source_url", "licence_id",
    "original_file_sha256", "relative_path", "task_type", "annotation_type",
    "component_label_original", "component_label_canonical", "capture_group_id",
    "split", "is_original_or_augmented",
)
TASKS = {"machine_family", "component_family", "instance_retrieval", "visual_condition"}
ANNOTATIONS = {"image_label", "bounding_box", "polygon_mask", "keypoint", "instance_id"}
TASK_ANNOTATIONS = {
    "machine_family": {"image_label", "bounding_box"},
    "component_family": {"image_label", "bounding_box"},
    "instance_retrieval": {"instance_id"},
    "visual_condition": {"image_label", "bounding_box", "polygon_mask", "keypoint"},
}
SPLITS = {"train", "validation", "test"}
UNKNOWN = {"", "unknown", "uncertain", "unlabelled", "unlabeled", "null", "none"}


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def audit(manifest: Path, image_root: Path) -> tuple[list[str], dict]:
    errors: list[str] = []
    rows: list[dict] = []
    for line_no, line in enumerate(manifest.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        try:
            row = json.loads(line)
            if not isinstance(row, dict):
                raise ValueError("each JSONL line must be an object")
            row["_line"] = line_no
            rows.append(row)
        except (json.JSONDecodeError, ValueError) as exc:
            errors.append(f"line {line_no}: invalid JSON record: {exc}")

    if not rows:
        errors.append("manifest contains no JSON records")

    ids: set[str] = set()
    rows_by_id: dict[str, dict] = {}
    file_hashes: dict[str, str] = {}
    group_splits: dict[str, set[str]] = defaultdict(set)
    labels: Counter = Counter()
    tasks: Counter = Counter()
    split_counts: Counter = Counter()
    valid_files = 0

    for row in rows:
        line = row["_line"]

        def fail(message: str) -> None:
            errors.append(f"line {line} ({row.get('sample_id', '?')}): {message}")

        for key in REQUIRED:
            if key not in row or row[key] is None or str(row[key]).strip() == "":
                fail(f"missing required field '{key}'")

        sample_id = str(row.get("sample_id", "")).strip()
        if sample_id:
            if sample_id in ids:
                fail(f"duplicate sample_id '{sample_id}'")
            ids.add(sample_id)
            rows_by_id.setdefault(sample_id, row)

        task = row.get("task_type")
        annotation = row.get("annotation_type")
        if task not in TASKS:
            fail(f"unsupported task_type '{task}'")
        else:
            tasks[task] += 1
            if annotation not in TASK_ANNOTATIONS[task]:
                fail(
                    f"annotation_type '{annotation}' is incompatible with "
                    f"task_type '{task}'; allowed: {sorted(TASK_ANNOTATIONS[task])}"
                )
        if annotation not in ANNOTATIONS:
            fail(f"unsupported annotation_type '{annotation}'")

        split = row.get("split")
        if split not in SPLITS:
            fail(f"split must be one of {sorted(SPLITS)}")
        else:
            split_counts[split] += 1

        group = str(row.get("capture_group_id", "")).strip()
        if group and split in SPLITS:
            group_splits[group].add(split)

        label = str(row.get("component_label_canonical", "")).strip()
        labels[label or "<missing>"] += 1
        if label.lower() in UNKNOWN and task in {"component_family", "machine_family"}:
            fail("unknown label must not be treated as a supervised class; quarantine or mark explicitly")

        rel_text = str(row.get("relative_path", "")).strip()
        if rel_text:
            rel = Path(rel_text)
            if rel.is_absolute() or ".." in rel.parts:
                fail("relative_path must stay inside image_root")
            else:
                try:
                    root = image_root.resolve(strict=True)
                    path = (root / rel).resolve(strict=True)
                    path.relative_to(root)
                except FileNotFoundError:
                    fail(f"image file not found: {rel}")
                    path = None
                except (ValueError, OSError):
                    fail("relative_path escapes image_root or cannot be resolved")
                    path = None
                if path is not None:
                    if not path.is_file():
                        fail(f"image file not found: {rel}")
                    else:
                        actual = sha256(path)
                        expected = str(row.get("original_file_sha256", "")).lower()
                        if actual != expected:
                            fail(f"SHA-256 mismatch for {rel}")
                        else:
                            valid_files += 1
                        prior = file_hashes.get(actual)
                        if prior and prior != sample_id:
                            fail(f"duplicate image bytes also used by sample_id '{prior}'")
                        else:
                            file_hashes[actual] = sample_id

        is_augmented = row.get("is_original_or_augmented")
        if is_augmented not in {"original", "augmented"}:
            fail("is_original_or_augmented must be 'original' or 'augmented'")
        parent_id = str(row.get("parent_sample_id", "")).strip()
        if is_augmented == "augmented" and not parent_id:
            fail("augmented sample requires parent_sample_id")
        if is_augmented == "original" and parent_id:
            fail("original sample must not declare parent_sample_id")

    # Check augmentation lineage only after all sample IDs have been collected.
    for row in rows:
        if row.get("is_original_or_augmented") != "augmented":
            continue
        line = row["_line"]
        parent_id = str(row.get("parent_sample_id", "")).strip()
        parent = rows_by_id.get(parent_id)
        if not parent:
            errors.append(f"line {line} ({row.get('sample_id', '?')}): parent_sample_id '{parent_id}' does not exist in manifest")
            continue
        if parent.get("split") != row.get("split"):
            errors.append(f"line {line} ({row.get('sample_id', '?')}): augmented sample and parent must remain in the same split")
        if parent.get("capture_group_id") != row.get("capture_group_id"):
            errors.append(f"line {line} ({row.get('sample_id', '?')}): augmented sample and parent must share capture_group_id")

    for group, assigned in group_splits.items():
        if len(assigned) > 1:
            errors.append(f"capture_group_id '{group}' leaks across splits: {sorted(assigned)}")

    report = {
        "records": len(rows),
        "valid_file_hashes": valid_files,
        "split_counts": dict(split_counts),
        "task_counts": dict(tasks),
        "label_counts": dict(labels),
        "unique_capture_groups": len(group_splits),
        "errors": len(errors),
    }
    return errors, report


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", required=True, type=Path, help="JSONL manifest")
    parser.add_argument("--image-root", required=True, type=Path, help="root for relative_path values")
    args = parser.parse_args()
    errors, report = audit(args.manifest, args.image_root)
    print(json.dumps(report, indent=2, sort_keys=True))
    if errors:
        print("\nAUDIT ERRORS:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1
    print("\nManifest checks passed. Semantic labels still require expert review.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
