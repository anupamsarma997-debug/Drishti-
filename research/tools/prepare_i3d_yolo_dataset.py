#!/usr/bin/env python3
"""Convert audited I3D parquet shards into a deduplicated YOLO detection dataset.

Only the primary `image` column is exported. conditioning_image is deliberately
not counted as an independent capture. Images are deduplicated by SHA-256 before
a deterministic 80/10/10 split, preventing exact duplicate leakage across splits.
This prepares data only; it does not train or validate a model. The workflow supplies all twelve individually audited source shards.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import random
import shutil
from collections import Counter
from io import BytesIO
from pathlib import Path

import pyarrow.parquet as pq
from PIL import Image

CLASS_NAMES = [
    "ball_bearing", "gear", "hammer", "measuring_tape",
    "nail", "nut", "oring", "plier", "saw", "scissors",
    "screw", "screwdriver", "spring", "utility_knife",
    "washer", "wrench",
]


def extract_image(value):
    if isinstance(value, dict):
        return value.get("bytes")
    try:
        return value["bytes"]
    except Exception:
        return None


def parse_boxes(annotation: str):
    tokens = annotation.strip().split()
    if not tokens:
        return []
    if len(tokens) % 5:
        raise ValueError(f"YOLO annotation has {len(tokens)} fields; expected multiple of five")
    boxes = []
    for i in range(0, len(tokens), 5):
        cls = int(tokens[i])
        x, y, w, h = map(float, tokens[i + 1:i + 5])
        if cls < 0 or cls >= len(CLASS_NAMES):
            raise ValueError(f"class id {cls} is outside 0..{len(CLASS_NAMES)-1}")
        if not (0 <= x <= 1 and 0 <= y <= 1 and 0 < w <= 1 and 0 < h <= 1):
            raise ValueError("normalized bbox values are outside valid ranges")
        boxes.append((cls, x, y, w, h))
    return boxes


def split_for_hash(digest: str, seed: int):
    value = int(hashlib.sha256(f"{seed}:{digest}".encode()).hexdigest()[:8], 16) / 0xFFFFFFFF
    if value < 0.8:
        return "train"
    if value < 0.9:
        return "val"
    return "test"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--parquet", nargs="+", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--seed", type=int, default=20261108)
    args = parser.parse_args()

    records = {}
    duplicate_rows = 0
    source_rows = 0
    invalid_rows = []
    for shard in sorted(args.parquet):
        pf = pq.ParquetFile(shard)
        for batch in pf.iter_batches(batch_size=64, columns=["image", "yolo_annotation"]):
            data = batch.to_pydict()
            for idx, (image_value, annotation) in enumerate(zip(data["image"], data["yolo_annotation"])):
                source_rows += 1
                raw = extract_image(image_value)
                if not raw:
                    invalid_rows.append({"shard": shard.name, "row_in_batch": idx, "error": "missing primary image"})
                    continue
                digest = hashlib.sha256(raw).hexdigest()
                try:
                    boxes = parse_boxes(annotation or "")
                    with Image.open(BytesIO(raw)) as image:
                        image.verify()
                    with Image.open(BytesIO(raw)) as image:
                        image = image.convert("RGB")
                        width, height = image.size
                        if width < 1 or height < 1:
                            raise ValueError("invalid image dimensions")
                        image_bytes = BytesIO()
                        image.save(image_bytes, format="JPEG", quality=95, optimize=True)
                except Exception as exc:
                    invalid_rows.append({"shard": shard.name, "row_in_batch": idx, "error": str(exc)[:180]})
                    continue
                label = "\n".join(f"{c} {x:.8f} {y:.8f} {w:.8f} {h:.8f}" for c, x, y, w, h in boxes)
                if digest in records:
                    duplicate_rows += 1
                    if records[digest]["label"] != label:
                        invalid_rows.append({"shard": shard.name, "row_in_batch": idx, "error": "same image hash has conflicting annotations"})
                    continue
                records[digest] = {
                    "jpeg": image_bytes.getvalue(),
                    "label": label,
                    "box_count": len(boxes),
                    "classes": [box[0] for box in boxes],
                }

    if invalid_rows:
        raise SystemExit("Data preparation stopped: " + json.dumps(invalid_rows[:20], indent=2))
    if not records:
        raise SystemExit("No valid primary images found")

    if args.output.exists():
        shutil.rmtree(args.output)
    for split in ("train", "val", "test"):
        (args.output / "images" / split).mkdir(parents=True, exist_ok=True)
        (args.output / "labels" / split).mkdir(parents=True, exist_ok=True)

    split_counts = Counter()
    class_counts = Counter()
    total_boxes = 0
    manifest = []
    for digest in sorted(records):
        record = records[digest]
        split = split_for_hash(digest, args.seed)
        stem = digest[:20]
        (args.output / "images" / split / f"{stem}.jpg").write_bytes(record["jpeg"])
        (args.output / "labels" / split / f"{stem}.txt").write_text(record["label"] + ("\n" if record["label"] else ""), encoding="utf-8")
        split_counts[split] += 1
        total_boxes += record["box_count"]
        class_counts.update(record["classes"])
        manifest.append({"sha256": digest, "split": split, "image": f"images/{split}/{stem}.jpg", "label": f"labels/{split}/{stem}.txt", "boxes": record["box_count"]})

    yaml = [
        f"path: {args.output.resolve()}",
        "train: images/train",
        "val: images/val",
        "test: images/test",
        "names:",
    ] + [f"  {i}: {name}" for i, name in enumerate(CLASS_NAMES)]
    (args.output / "dataset.yaml").write_text("\n".join(yaml) + "\n", encoding="utf-8")
    (args.output / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    report = {
        "dataset": "I3D-Tools-Dataset",
        "synthetic_data": True,
        "source_rows": source_rows,
        "unique_primary_images": len(records),
        "duplicate_primary_rows_removed": duplicate_rows,
        "valid_yolo_boxes": total_boxes,
        "split_images": dict(split_counts),
        "class_box_counts": {CLASS_NAMES[k]: v for k, v in sorted(class_counts.items())},
        "seed": args.seed,
        "split_method": "SHA-256 image deduplication followed by deterministic 80/10/10 hash split",
        "conditioning_images_exported": False,
        "real_world_validation": "not performed",
        "training_performed": False,
    }
    (args.output / "preparation_report.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
