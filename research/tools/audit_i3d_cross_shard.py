#!/usr/bin/env python3
"""Audit exact primary-image duplicates, annotation conflicts, and global counts across all I3D shards."""
from __future__ import annotations

import argparse
import hashlib
import json
from collections import Counter
from pathlib import Path

import pyarrow.parquet as pq
from audit_i3d_tools_parquet import image_bytes, parse_yolo_annotation

def normalized_label(value):
    if not value or not value.strip():
        return ()
    boxes = parse_yolo_annotation(value)
    return tuple(sorted((c, round(x, 8), round(y, 8), round(w, 8), round(h, 8))
                        for c, x, y, w, h in boxes))

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--parquet", nargs="+", type=Path, required=True)
    ap.add_argument("--output", type=Path, required=True)
    args = ap.parse_args()

    seen = {}
    per_shard = {}
    duplicate_rows = 0
    conflicting = []
    missing_images = 0
    invalid_annotations = []
    total_rows = total_boxes = 0
    class_counts = Counter()

    for shard in sorted(args.parquet):
        shard_rows = shard_images = shard_boxes = shard_duplicates = 0
        pf = pq.ParquetFile(shard)
        for batch in pf.iter_batches(batch_size=64, columns=["image", "yolo_annotation"]):
            data = batch.to_pydict()
            for image_value, annotation in zip(data["image"], data["yolo_annotation"]):
                total_rows += 1
                shard_rows += 1
                raw = image_bytes(image_value)
                if not raw:
                    missing_images += 1
                    continue
                shard_images += 1
                digest = hashlib.sha256(raw).hexdigest()
                try:
                    label = normalized_label(annotation)
                except Exception as exc:
                    invalid_annotations.append({"shard": shard.name, "row": shard_rows, "error": str(exc)[:160]})
                    continue
                total_boxes += len(label)
                shard_boxes += len(label)
                class_counts.update(box[0] for box in label)
                previous = seen.get(digest)
                if previous is None:
                    seen[digest] = (label, shard.name)
                else:
                    duplicate_rows += 1
                    shard_duplicates += 1
                    if previous[0] != label and len(conflicting) < 100:
                        conflicting.append({
                            "sha256": digest,
                            "first_shard": previous[1],
                            "duplicate_shard": shard.name,
                            "first_label": previous[0],
                            "duplicate_label": label,
                        })
        per_shard[shard.name] = {
            "rows": shard_rows,
            "primary_images": shard_images,
            "valid_boxes": shard_boxes,
            "duplicate_primary_rows_vs_previous_shards": shard_duplicates,
        }

    report = {
        "dataset": "I3D-Tools-Dataset",
        "synthetic_data": True,
        "shards_supplied": len(args.parquet),
        "total_rows": total_rows,
        "primary_image_rows": sum(v["primary_images"] for v in per_shard.values()),
        "globally_unique_primary_images": len(seen),
        "cross_shard_and_within_input_duplicate_rows": duplicate_rows,
        "valid_yolo_boxes": total_boxes,
        "class_id_box_counts": {str(k): v for k, v in sorted(class_counts.items())},
        "missing_primary_images": missing_images,
        "invalid_annotation_rows": len(invalid_annotations),
        "conflicting_duplicate_annotations": len(conflicting),
        "conflict_examples": conflicting,
        "per_shard": per_shard,
        "split_policy": "Deduplicate exact primary-image SHA-256 before assigning any train/validation/test split.",
        "conditioning_images_counted_as_captures": False,
        "model_training_performed": False,
        "real_world_validation_performed": False,
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    if len(args.parquet) != 12 or missing_images or invalid_annotations or conflicting:
        return 1
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
