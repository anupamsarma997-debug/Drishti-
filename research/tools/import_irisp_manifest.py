#!/usr/bin/env python3
"""PROVISIONAL IRISP importer for an UNVERIFIED folder-layout assumption.

The upstream repository currently says the full dataset/download is TBA and
does not publish an archive or exact directory tree. Do not use this adapter
until you have inspected an authorised archive and confirmed its layout.
IRISP object IDs are instance identities, NOT semantic part names.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
from typing import Iterator

SOURCE_URL = "https://github.com/imyliu1717/industrial_spare_part_dataset"
SOURCE_VERSION = "a9c1b014afcee71e37307272e5112b572ec89014"
LICENSE_ID = "CC-BY-SA-4.0"
IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp", ".webp", ".tif", ".tiff"}


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def iter_images(root: Path, split_dir: str) -> Iterator[tuple[str, Path]]:
    # Provisional assumption only: <split>/phone/<object-id>/<image>
    base = root / split_dir / "phone"
    if not base.is_dir():
        return
    for object_dir in sorted(base.iterdir(), key=lambda p: p.name.casefold()):
        if object_dir.is_symlink():
            raise ValueError(f"Symbolic-link object folder is not allowed: {object_dir}")
        if not object_dir.is_dir():
            continue
        for path in sorted(object_dir.rglob("*"), key=lambda p: p.as_posix().casefold()):
            if path.is_symlink():
                raise ValueError(f"Symbolic-link image path is not allowed: {path}")
            if path.is_file() and path.suffix.lower() in IMAGE_EXTENSIONS:
                yield object_dir.name, path


def build_records(image_root: Path) -> list[dict]:
    root = image_root.resolve(strict=True)
    if not root.is_dir():
        raise ValueError(f"Image root is not a directory: {root}")
    records: list[dict] = []
    seen_ids: set[str] = set()
    for source_split, target_split in (("train", "train"), ("val", "validation"), ("test", "test")):
        for object_id, path in iter_images(root, source_split):
            resolved = path.resolve(strict=True)
            try:
                relative = resolved.relative_to(root).as_posix()
            except ValueError as exc:
                raise ValueError(f"Image escapes image root: {path}") from exc
            digest = sha256(resolved)
            sample_id = "irisp-" + hashlib.sha256(relative.encode("utf-8")).hexdigest()[:24]
            if sample_id in seen_ids:
                raise ValueError(f"Duplicate sample ID generated for {relative}")
            seen_ids.add(sample_id)
            records.append({
                "sample_id": sample_id,
                "source_dataset": "IRISP",
                "source_version": SOURCE_VERSION,
                "source_url": SOURCE_URL,
                "licence_id": LICENSE_ID,
                "original_file_sha256": digest,
                "relative_path": relative,
                "task_type": "instance_retrieval",
                "annotation_type": "instance_id",
                "component_label_original": "unknown",
                "component_label_canonical": "unknown",
                "capture_group_id": "irisp-object-" + object_id,
                "split": target_split,
                "is_original_or_augmented": "original",
                "instance_id_original": object_id,
                "semantic_label_status": "NOT_PROVIDED_BY_SOURCE",
                "source_camera_domain": "phone",
                "layout_status": "PROVISIONAL_UNVERIFIED",
            })
    if not records:
        raise ValueError("No images found in the assumed train/val/test phone layout; inspect the archive and write a verified adapter.")
    return records


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--image-root", required=True, type=Path,
                        help="Extracted IRISP images directory")
    parser.add_argument("--output", required=True, type=Path, help="Output JSONL manifest")
    parser.add_argument("--acknowledge-unverified-layout", action="store_true",
                        help="Confirm that you inspected the archive and verified this assumed layout")
    args = parser.parse_args()
    if not args.acknowledge_unverified_layout:
        parser.error("upstream archive layout is not published; inspect your authorised archive, then pass --acknowledge-unverified-layout only if the assumed folders match")
    records = build_records(args.image_root)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8", newline="\n") as stream:
        for record in records:
            stream.write(json.dumps(record, ensure_ascii=False, sort_keys=True) + "\n")
    print(json.dumps({
        "records_written": len(records),
        "split_counts": {split: sum(r["split"] == split for r in records)
                         for split in ("train", "validation", "test")},
        "semantic_part_labels": 0,
        "layout_status": "PROVISIONAL_UNVERIFIED",
        "warning": "This importer assumes train/phone, val/phone, test/phone folders; upstream has not published its archive layout. Object IDs are instance identities, not component classes. Review CC-BY-SA-4.0 obligations."
    }, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
