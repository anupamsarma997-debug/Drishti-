#!/usr/bin/env python3
"""Download/extract/audit additional visual parts datasets; no model training."""
from __future__ import annotations
import argparse, hashlib, json, shutil, zipfile
from pathlib import Path, PurePosixPath
from collections import Counter

IMAGE_EXTS={".jpg",".jpeg",".png",".bmp",".webp",".tif",".tiff"}

def digest(p):
    h=hashlib.sha256()
    with p.open("rb") as f:
        for b in iter(lambda:f.read(1024*1024),b""): h.update(b)
    return h.hexdigest()

def extract_safe(zf, dest):
    root=dest.resolve()
    for info in zf.infolist():
        q=PurePosixPath(info.filename.replace("\\","/"))
        if q.is_absolute() or ".." in q.parts: raise ValueError("unsafe zip path: "+info.filename)
        out=(dest/Path(*q.parts)).resolve()
        if out!=root and root not in out.parents: raise ValueError("zip path traversal")
    zf.extractall(dest)

def audit(name, archive, base):
    dest=base/name
    if dest.exists(): shutil.rmtree(dest)
    dest.mkdir(parents=True)
    with zipfile.ZipFile(archive) as z:
        bad=z.testzip()
        if bad: raise RuntimeError(f"{name}: CRC failure at {bad}")
        entries=z.infolist()
        extract_safe(z,dest)
    files=[p for p in dest.rglob("*") if p.is_file()]
    images=[p for p in files if p.suffix.lower() in IMAGE_EXTS]
    labels=[p for p in files if p.suffix.lower()==".txt" and "label" in str(p.parent).lower()]
    yaml_files=[p for p in files if p.name.lower() in ("data.yaml","carparts-seg.yaml")]
    unique=set(); invalid=[]; dims=Counter()
    try:
        from PIL import Image
    except ImportError: raise RuntimeError("Pillow missing")
    for p in images:
        unique.add(digest(p))
        try:
            with Image.open(p) as im:
                dims[f"{im.width}x{im.height}"]+=1
                im.verify()
        except Exception: invalid.append(str(p.relative_to(dest)))
    class_ids=Counter(); malformed=0; label_lines=0
    for p in labels:
        for line in p.read_text(errors="replace").splitlines():
            if not line.strip(): continue
            parts=line.split()
            try:
                cid=int(parts[0]); coords=[float(x) for x in parts[1:]]
                if len(coords)<4 or not all(0<=v<=1 for v in coords): raise ValueError()
                class_ids[cid]+=1; label_lines+=1
            except Exception: malformed+=1
    return {
      "name":name,"archive_bytes":archive.stat().st_size,"archive_sha256":digest(archive),
      "zip_entries":len(entries),"extracted_files":len(files),
      "images":len(images),"unique_image_sha256":len(unique),
      "duplicate_image_files":len(images)-len(unique),"invalid_images":len(invalid),
      "image_dimensions":dict(dims),"label_files":len(labels),
      "valid_yolo_label_lines":label_lines,"label_class_id_instances":dict(sorted(class_ids.items())),
      "malformed_label_lines":malformed,
      "yaml_files":[str(p.relative_to(dest)) for p in yaml_files],
      "license_files":[str(p.relative_to(dest)) for p in files if p.name.upper().startswith("LICENSE")],
      "sample_images":[str(p.relative_to(dest)) for p in images[:8]],
      "invalid_image_examples":invalid[:8]
    }

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--pcb-archive",type=Path,required=True)
    ap.add_argument("--car-archive",type=Path,required=True)
    ap.add_argument("--output",type=Path,required=True)
    ap.add_argument("--work-dir",type=Path,required=True)
    a=ap.parse_args()
    if a.work_dir.exists(): shutil.rmtree(a.work_dir)
    a.work_dir.mkdir(parents=True)
    report={
      "sources":[
        {"name":"pcb-component-detection-dataset-yolo","url":"https://github.com/wangshnegqi/pcb-component-detection-dataset-yolo","license":"Dataset CC BY 4.0; repository/model Apache-2.0 per source LICENSE files","scope":"10 PCB/electronic component classes"},
        {"name":"ultralytics-carparts-seg","url":"https://docs.ultralytics.com/datasets/segment/carparts-seg/","license":"CC BY 4.0 per Ultralytics dataset documentation; original Roboflow dataset cited","scope":"23 automotive exterior part segmentation classes"}
      ],
      "datasets":[
        audit("pcb-component-detection-dataset-yolo",a.pcb_archive,a.work_dir),
        audit("ultralytics-carparts-seg",a.car_archive,a.work_dir)
      ],
      "limitations":[
        "PCB classes are electronic-board components, not general vehicle/farm/industrial machine spare parts.",
        "Carparts-Seg covers vehicle exterior body parts, not engine internals or repair diagnosis.",
        "Image count is not annotation count; verify label/image pairing and semantic quality before training.",
        "No Vidya model is trained or evaluated by this acquisition workflow."
      ]
    }
    a.output.parent.mkdir(parents=True,exist_ok=True)
    a.output.write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(report,indent=2))
    if any(d["images"]==0 or d["invalid_images"] for d in report["datasets"]): return 1
    return 0
if __name__=="__main__": raise SystemExit(main())
