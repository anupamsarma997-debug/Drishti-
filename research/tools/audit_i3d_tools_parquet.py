#!/usr/bin/env python3
"""Audit one official I3D Tools Dataset parquet shard; no training."""
from __future__ import annotations
import argparse, hashlib, json
from collections import Counter
from pathlib import Path
import pyarrow.parquet as pq
from PIL import Image
from io import BytesIO

def image_bytes(value):
    if value is None: return None
    if isinstance(value, dict): return value.get("bytes")
    try: return value["bytes"]
    except Exception: return None

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--parquet",type=Path,required=True)
    ap.add_argument("--output",type=Path,required=True)
    a=ap.parse_args()
    pf=pq.ParquetFile(a.parquet)
    schema=str(pf.schema)
    rows=0; primary=0; conditioning=0; invalid=[]; hashes=set()
    dims=Counter(); classes=Counter(); bad_labels=0; label_rows=0
    for batch in pf.iter_batches(batch_size=64):
        d=batch.to_pydict()
        for i in range(batch.num_rows):
            rows+=1
            raw=image_bytes(d.get("image",[None]*batch.num_rows)[i])
            if raw:
                primary+=1; hashes.add(hashlib.sha256(raw).hexdigest())
                try:
                    with Image.open(BytesIO(raw)) as im:
                        im.verify()
                    with Image.open(BytesIO(raw)) as im:
                        dims[f"{im.width}x{im.height}"]+=1
                except Exception: invalid.append({"row":rows,"column":"image"})
            raw2=image_bytes(d.get("conditioning_image",[None]*batch.num_rows)[i])
            if raw2:
                conditioning+=1
                try:
                    with Image.open(BytesIO(raw2)) as im: im.verify()
                except Exception: invalid.append({"row":rows,"column":"conditioning_image"})
            ann=d.get("yolo_annotation",[None]*batch.num_rows)[i]
            if ann:
                label_rows+=1
                try:
                    p=ann.strip().split()
                    if len(p)!=5: raise ValueError("expected class plus 4 bbox values")
                    cid=int(p[0]); x,y,w,h=map(float,p[1:])
                    if min(x,y,w,h)<0 or max(x,y,w,h)>1 or w<=0 or h<=0: raise ValueError("bbox out of range")
                    classes[cid]+=1
                except Exception: bad_labels+=1
    report={
      "dataset":"I3D-Tools-Dataset",
      "source":"https://huggingface.co/datasets/i3dlabiisc/I3D-Tools-Dataset",
      "source_license":"MIT as listed on Hugging Face dataset page; retain attribution and verify upstream terms before redistribution.",
      "synthetic_data":True,
      "shard":a.parquet.name,
      "shard_bytes":a.parquet.stat().st_size,
      "parquet_sha256":hashlib.sha256(a.parquet.read_bytes()).hexdigest(),
      "rows":rows,"primary_images":primary,"conditioning_images":conditioning,
      "unique_primary_image_hashes":len(hashes),
      "duplicate_primary_image_rows":primary-len(hashes),
      "invalid_image_entries":len(invalid),
      "primary_image_dimensions":dict(dims),
      "rows_with_yolo_annotation":label_rows,
      "valid_yolo_boxes":sum(classes.values()),
      "class_id_box_counts":dict(sorted(classes.items())),
      "malformed_yolo_annotation_rows":bad_labels,
      "invalid_image_examples":invalid[:20],
      "limitations":[
        "This is one of twelve parquet shards, not the complete 5.52 GB dataset.",
        "Images are synthetic; results cannot establish recognition on real workshop photographs.",
        "Dataset-level MIT label does not establish licenses for any separately embedded third-party assets; inspect source documentation.",
        "No model training or accuracy claim is made."
      ]
    }
    a.output.parent.mkdir(parents=True,exist_ok=True)
    a.output.write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(report,indent=2))
    if not rows or not primary or invalid or bad_labels: return 1
    return 0
if __name__=="__main__": raise SystemExit(main())
