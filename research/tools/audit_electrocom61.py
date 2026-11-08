#!/usr/bin/env python3
"""Audit an extracted ElectroCom61-style YOLO archive without modifying it.

Expected split layout (case-insensitive):
  Train/Image, Train/Label
  Valid/Image, Valid/Label
  Test/Image, Test/Label

This validates archive structure and YOLO box syntax only. It does not
authenticate the source, validate semantic class names, decode image pixels,
or establish that the dataset is suitable for machine-fault diagnosis.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
from collections import Counter, defaultdict
from pathlib import Path

SPLITS = {"train": "train", "valid": "validation", "validation": "validation", "test": "test"}
IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp", ".webp", ".tif", ".tiff"}
CLASS_COUNT = 61


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def locate_split_dirs(root: Path) -> tuple[dict[str, tuple[Path, Path]], list[str]]:
    errors: list[str] = []
    found: dict[str, tuple[Path, Path]] = {}
    for child in root.iterdir():
        if not child.is_dir():
            continue
        split = SPLITS.get(child.name.lower())
        if not split:
            continue
        if split in found:
            errors.append(f"multiple directories map to split '{split}'")
            continue
        children = {p.name.lower(): p for p in child.iterdir() if p.is_dir()}
        image_dir, label_dir = children.get("image"), children.get("label")
        if image_dir is None or label_dir is None:
            errors.append(f"{child.name}: expected Image/ and Label/ subdirectories")
            continue
        found[split] = (image_dir, label_dir)
    for required in ("train", "validation", "test"):
        if required not in found:
            errors.append(f"missing split directory for '{required}'")
    return found, errors


def audit(root: Path) -> tuple[list[str], dict]:
    errors: list[str] = []
    if not root.is_dir():
        return [f"dataset root is not a directory: {root}"], {"images": 0, "labels": 0, "errors": 1}

    split_dirs, layout_errors = locate_split_dirs(root)
    errors.extend(layout_errors)
    split_counts: Counter = Counter()
    class_counts: Counter = Counter()
    hashes: dict[str, tuple[str, str]] = {}
    total_images = total_labels = total_boxes = 0
    missing_labels: list[str] = []
    orphan_labels: list[str] = []

    for split, (image_dir, label_dir) in split_dirs.items():
        images = {p.stem: p for p in image_dir.rglob("*") if p.is_file() and p.suffix.lower() in IMAGE_EXTENSIONS}
        labels = {p.stem: p for p in label_dir.rglob("*.txt") if p.is_file()}
        for stem in sorted(set(images) - set(labels)):
            missing_labels.append(f"{split}/{stem}")
        for stem in sorted(set(labels) - set(images)):
            orphan_labels.append(f"{split}/{stem}")
        for stem, image_path in images.items():
            total_images += 1
            split_counts[split] += 1
            try:
                digest = sha256(image_path)
                previous = hashes.get(digest)
                if previous and previous[1] != split:
                    errors.append(f"identical image bytes cross splits: {previous[0]} and {split}/{image_path.name}")
                else:
                    hashes[digest] = (f"{split}/{image_path.name}", split)
            except OSError as exc:
                errors.append(f"cannot hash image {image_path}: {exc}")
        for stem, label_path in labels.items():
            total_labels += 1
            try:
                lines = label_path.read_text(encoding="utf-8").splitlines()
            except (OSError, UnicodeDecodeError) as exc:
                errors.append(f"cannot read label {label_path}: {exc}")
                continue
            for line_no, line in enumerate(lines, 1):
                if not line.strip():
                    continue
                parts = line.split()
                location = f"{split}/{label_path.name}:{line_no}"
                if len(parts) != 5:
                    errors.append(f"{location}: expected 5 YOLO fields, got {len(parts)}")
                    continue
                try:
                    class_value = float(parts[0])
                    coords = [float(v) for v in parts[1:]]
                except ValueError:
                    errors.append(f"{location}: fields must be numeric")
                    continue
                if not class_value.is_integer() or not (0 <= int(class_value) < CLASS_COUNT):
                    errors.append(f"{location}: class_id must be an integer from 0 to {CLASS_COUNT - 1}")
                    continue
                x, y, width, height = coords
                if not all(0.0 <= v <= 1.0 for v in coords):
                    errors.append(f"{location}: normalized coordinates must be within [0, 1]")
                    continue
                if width <= 0 or height <= 0:
                    errors.append(f"{location}: box width and height must be greater than zero")
                    continue
                class_counts[int(class_value)] += 1
                total_boxes += 1

    for item in missing_labels:
        errors.append(f"image has no matching label file: {item}")
    for item in orphan_labels:
        errors.append(f"label has no matching image: {item}")

    report = {
        "dataset_root": str(root),
        "expected_class_ids": CLASS_COUNT,
        "images": total_images,
        "label_files": total_labels,
        "bounding_boxes": total_boxes,
        "split_image_counts": dict(split_counts),
        "observed_class_ids": len(class_counts),
        "class_box_counts": {str(k): class_counts[k] for k in sorted(class_counts)},
        "missing_label_count": len(missing_labels),
        "orphan_label_count": len(orphan_labels),
        "errors": len(errors),
    }
    return errors, report


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", required=True, type=Path, help="extracted dataset root")
    args = parser.parse_args()
    errors, report = audit(args.root)
    print(json.dumps(report, indent=2, sort_keys=True))
    if errors:
        print("\nARCHIVE AUDIT ERRORS:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1
    print("\nStructure and YOLO syntax checks passed; semantic labels still need review.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
