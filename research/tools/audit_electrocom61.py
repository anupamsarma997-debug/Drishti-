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
import csv
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


def index_by_stem(paths: list[Path], kind: str, split: str, errors: list[str]) -> dict[str, Path]:
    indexed: dict[str, Path] = {}
    for path in paths:
        if path.stem in indexed:
            errors.append(
                f"duplicate {kind} filename stem in {split}: "
                f"{indexed[path.stem].as_posix()} and {path.as_posix()}; "
                "use unique matching basenames"
            )
            continue
        indexed[path.stem] = path
    return indexed


def audit_metadata_csv(root: Path, image_split_by_name: dict[str, str], errors: list[str]) -> dict:
    candidates = [p for p in root.rglob("*") if p.is_file() and p.name.casefold() == "metadata_electrocom61.csv"]
    if not candidates:
        return {"status": "not_found", "rows": 0}
    if len(candidates) > 1:
        errors.append("multiple Metadata_ElectroCom61.csv files found")
        return {"status": "ambiguous", "rows": 0}

    metadata_path = candidates[0]
    required = {"IMAGE_NAME", "TYPE_OF_OBJECTS", "OBJECT_NAME", "BACKGROUND", "DEVICE_NAME", "DATA_TYPE"}
    seen: set[str] = set()
    row_count = 0
    split_counts: Counter = Counter()
    try:
        with metadata_path.open("r", encoding="utf-8-sig", newline="") as handle:
            reader = csv.DictReader(handle)
            fields = {str(name).strip().upper() for name in (reader.fieldnames or [])}
            missing_fields = sorted(required - fields)
            if missing_fields:
                errors.append("metadata CSV missing required columns: " + ", ".join(missing_fields))
                return {"status": "invalid_columns", "rows": 0}
            for line_no, row in enumerate(reader, 2):
                row_count += 1
                normalized = {str(k).strip().upper(): (v or "").strip() for k, v in row.items() if k is not None}
                raw_name = normalized.get("IMAGE_NAME", "")
                image_name = raw_name.replace("\\", "/").rsplit("/", 1)[-1]
                key = image_name.casefold()
                location = f"Metadata_ElectroCom61.csv:{line_no}"
                if not image_name:
                    errors.append(f"{location}: IMAGE_NAME is empty")
                    continue
                if key in seen:
                    errors.append(f"{location}: duplicate IMAGE_NAME '{image_name}'")
                seen.add(key)
                if key not in image_split_by_name:
                    errors.append(f"{location}: metadata image not found in split folders: {image_name}")
                raw_split = normalized.get("DATA_TYPE", "").casefold()
                split = SPLITS.get(raw_split)
                if split is None:
                    errors.append(f"{location}: DATA_TYPE must identify Train, Valid/Validation, or Test")
                else:
                    split_counts[split] += 1
                    actual_split = image_split_by_name.get(key)
                    if actual_split is not None and actual_split != split:
                        errors.append(f"{location}: DATA_TYPE says {raw_split}, but image is in {actual_split}")
                try:
                    object_count = int(normalized.get("TYPE_OF_OBJECTS", ""))
                    if object_count < 1:
                        raise ValueError
                except ValueError:
                    errors.append(f"{location}: TYPE_OF_OBJECTS must be a positive integer")
                if not normalized.get("OBJECT_NAME"):
                    errors.append(f"{location}: OBJECT_NAME is empty")
    except (OSError, UnicodeDecodeError, csv.Error) as exc:
        errors.append(f"cannot read metadata CSV: {exc}")
        return {"status": "read_error", "rows": row_count}

    if row_count != len(image_split_by_name):
        errors.append(
            f"metadata row count ({row_count}) does not match unique image filename count "
            f"({len(image_split_by_name)})"
        )
    return {"status": "checked", "rows": row_count, "split_rows": dict(split_counts)}


def audit(root: Path) -> tuple[list[str], dict]:
    errors: list[str] = []
    if not root.is_dir():
        return [f"dataset root is not a directory: {root}"], {"images": 0, "labels": 0, "errors": 1}

    split_dirs, layout_errors = locate_split_dirs(root)
    errors.extend(layout_errors)
    split_counts: Counter = Counter()
    class_counts: Counter = Counter()
    hashes: dict[str, tuple[str, str]] = {}
    image_split_by_name: dict[str, str] = {}
    total_images = total_labels = total_boxes = 0
    missing_labels: list[str] = []
    orphan_labels: list[str] = []

    for split, (image_dir, label_dir) in split_dirs.items():
        image_paths = [p for p in image_dir.rglob("*") if p.is_file() and p.suffix.lower() in IMAGE_EXTENSIONS]
        label_paths = [p for p in label_dir.rglob("*") if p.is_file() and p.suffix.lower() == ".txt"]
        images = index_by_stem(image_paths, "image", split, errors)
        labels = index_by_stem(label_paths, "label", split, errors)
        for stem in sorted(set(images) - set(labels)):
            missing_labels.append(f"{split}/{stem}")
        for stem in sorted(set(labels) - set(images)):
            orphan_labels.append(f"{split}/{stem}")
        for stem, image_path in images.items():
            total_images += 1
            split_counts[split] += 1
            filename_key = image_path.name.casefold()
            previous_split = image_split_by_name.get(filename_key)
            if previous_split is not None and previous_split != split:
                errors.append(f"image filename appears in multiple splits: {image_path.name}")
            image_split_by_name[filename_key] = split
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

    metadata_report = audit_metadata_csv(root, image_split_by_name, errors)
    report = {
        "dataset_root": str(root),
        "metadata_csv": metadata_report,
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
