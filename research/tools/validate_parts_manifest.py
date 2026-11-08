#!/usr/bin/env python3
"""Audit a Vidya visual-parts JSONL manifest using only the Python standard library.

This validates metadata, file hashes, duplicate files, augmentation ancestry and
capture-group split leakage. It does not validate semantic label correctness.
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

    ids: set[str] = set()
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

        task = row.get("task_type")
        if task not in TASKS:
            fail(f"unsupported task_type '{task}'")
        else:
            tasks[task] += 1

        annotation = row.get("annotation_type")
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

        rel = Path(str(row.get("relative_path", "")))
        if rel.is_absolute() or ".." in rel.parts:
            fail("relative_path must stay inside image_root")
        else:
            path = (image_root / rel).resolve()
            try:
                path.relative_to(image_root.resolve())
            except ValueError:
                fail("relative_path escapes image_root")
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
        if is_augmented == "augmented" and not str(row.get("parent_sample_id", "")).strip():
            fail("augmented sample requires parent_sample_id")

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
