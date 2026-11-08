#!/usr/bin/env python3
"""Audit one official I3D Tools Dataset parquet shard, including multi-box rows and 16-class ID bounds and shard-level integrity and annotation-count and source-integrity and multi-shard readiness and image-integrity audit across the final shard; no training."""
from __future__ import annotations

import argparse
import hashlib
import json
from collections import Counter
from io import BytesIO
from pathlib import Path

import pyarrow.parquet as pq
from PIL import Image


def image_bytes(value):
    if value is None:
        return None
    if isinstance(value, dict):
        return value.get("bytes")
    try:
        return value["bytes"]
    except Exception:
        return None


def parse_yolo_annotation(annotation):
    """Parse one or more YOLO boxes; tolerate newline- or whitespace-separated rows."""
    tokens = annotation.strip().split()
    if not tokens or len(tokens) % 5:
        raise ValueError(f"expected a multiple of 5 YOLO fields, got {len(tokens)}")
    boxes = []
    for offset in range(0, len(tokens), 5):
        group = tokens[offset:offset + 5]
        cid = int(group[0])
        x, y, width, height = map(float, group[1:])
        if cid < 0 or cid > 15:
            raise ValueError(f"class id out of expected 0..15 range: {cid}")
        if min(x, y, width, height) < 0 or max(x, y, width, height) > 1:
            raise ValueError("bbox coordinate outside normalized 0..1 range")
        if width <= 0 or height <= 0:
            raise ValueError("bbox width/height must be positive")
        boxes.append((cid, x, y, width, height))
    return boxes


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--parquet", type=Path, required=True)
    ap.add_argument("--output", type=Path, required=True)
    args = ap.parse_args()

    parquet = pq.ParquetFile(args.parquet)
    rows = primary = conditioning = 0
    invalid_images = []
    primary_hashes = set()
    dimensions = Counter()
    class_counts = Counter()
    malformed_rows = 0
    annotation_rows = valid_annotation_rows = 0
    box_count = 0
    malformed_examples = []

    for batch in parquet.iter_batches(batch_size=64):
        data = batch.to_pydict()
        for index in range(batch.num_rows):
            rows += 1
            image_column = data.get("image", [None] * batch.num_rows)[index]
            raw = image_bytes(image_column)
            if raw:
                primary += 1
                primary_hashes.add(hashlib.sha256(raw).hexdigest())
                try:
                    with Image.open(BytesIO(raw)) as im:
                        im.verify()
                    with Image.open(BytesIO(raw)) as im:
                        dimensions[f"{im.width}x{im.height}"] += 1
                except Exception as exc:
                    invalid_images.append({"row": rows, "column": "image", "error": str(exc)[:160]})

            conditioning_raw = image_bytes(
                data.get("conditioning_image", [None] * batch.num_rows)[index]
            )
            if conditioning_raw:
                conditioning += 1
                try:
                    with Image.open(BytesIO(conditioning_raw)) as im:
                        im.verify()
                except Exception as exc:
                    invalid_images.append(
                        {"row": rows, "column": "conditioning_image", "error": str(exc)[:160]}
                    )

            annotation = data.get("yolo_annotation", [None] * batch.num_rows)[index]
            if annotation and annotation.strip():
                annotation_rows += 1
                try:
                    boxes = parse_yolo_annotation(annotation)
                    valid_annotation_rows += 1
                    box_count += len(boxes)
                    for box in boxes:
                        class_counts[box[0]] += 1
                except Exception as exc:
                    malformed_rows += 1
                    if len(malformed_examples) < 12:
                        malformed_examples.append({
                            "row": rows,
                            "reason": str(exc),
                            "annotation": annotation[:500],
                        })

    report = {
        "dataset": "I3D-Tools-Dataset",
        "source": "https://huggingface.co/datasets/i3dlabiisc/I3D-Tools-Dataset",
        "source_license": "MIT as listed on Hugging Face dataset page; retain attribution and verify upstream terms before redistribution.",
        "synthetic_data": True,
        "shard": args.parquet.name,
        "shard_bytes": args.parquet.stat().st_size,
        "parquet_sha256": hashlib.sha256(args.parquet.read_bytes()).hexdigest(),
        "rows": rows,
        "primary_images": primary,
        "conditioning_images": conditioning,
        "unique_primary_image_hashes": len(primary_hashes),
        "duplicate_primary_image_rows": primary - len(primary_hashes),
        "invalid_image_entries": len(invalid_images),
        "primary_image_dimensions": dict(dimensions),
        "rows_with_yolo_annotation": annotation_rows,
        "valid_yolo_annotation_rows": valid_annotation_rows,
        "valid_yolo_boxes": box_count,
        "class_id_box_counts": dict(sorted(class_counts.items())),
        "malformed_yolo_annotation_rows": malformed_rows,
        "malformed_annotation_examples": malformed_examples,
        "invalid_image_examples": invalid_images[:20],
        "limitations": [
            "This is one of twelve parquet shards, not the complete 5.52 GB dataset.",
            "Images are synthetic; results cannot establish recognition on real workshop photographs.",
            "Dataset-level MIT label does not establish licenses for any separately embedded third-party assets; inspect source documentation.",
            "No model training or accuracy claim is made.",
        ],
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    if not rows or not primary or invalid_images or malformed_rows:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
