#!/usr/bin/env python3
"""Extract and audit the Zenodo Bonfiglioli bearing-defect archive."""
from __future__ import annotations
import argparse, hashlib, json, subprocess
from pathlib import Path
from collections import Counter
IMAGE_EXTS={".jpg",".jpeg",".png",".bmp",".tif",".tiff",".webp"}

def sha(p):
 h=hashlib.sha256()
 with p.open("rb") as f:
  for b in iter(lambda:f.read(1024*1024),b""): h.update(b)
 return h.hexdigest()

def main():
 ap=argparse.ArgumentParser()
 ap.add_argument("--archive",type=Path,required=True)
 ap.add_argument("--output",type=Path,required=True)
 ap.add_argument("--extract-dir",type=Path,required=True)
 a=ap.parse_args()
 a.extract_dir.mkdir(parents=True,exist_ok=True)
 proc=subprocess.run(["unar","-quiet","-force-overwrite","-output-directory",str(a.extract_dir),str(a.archive)],capture_output=True,text=True)
 if proc.returncode!=0:
  raise RuntimeError("RAR extraction failed: "+proc.stderr[-2000:])
 files=[p for p in a.extract_dir.rglob("*") if p.is_file()]
 images=[p for p in files if p.suffix.lower() in IMAGE_EXTS]
 from PIL import Image
 dims=Counter(); invalid=[]; hashes=set()
 for p in images:
  hashes.add(sha(p))
  try:
   with Image.open(p) as im:
    dims[f"{im.width}x{im.height}"]+=1
    im.verify()
  except Exception: invalid.append(str(p.relative_to(a.extract_dir)))
 report={
  "dataset":"Bonfiglioli bearing defect dataset",
  "source":"https://zenodo.org/records/11034252",
  "archive_bytes":a.archive.stat().st_size,
  "archive_sha256":sha(a.archive),
  "extracted_files":len(files),
  "images":len(images),
  "unique_image_sha256":len(hashes),
  "duplicate_image_files":len(images)-len(hashes),
  "invalid_images":len(invalid),
  "image_dimensions":dict(dims),
  "sample_images":[str(p.relative_to(a.extract_dir)) for p in images[:12]],
  "license_status":"NOT STATED in Zenodo record rights field at acquisition time; quarantine only. Do not train, redistribute, or use commercially until rights holder clarifies.",
  "scope":"Laboratory images of actual bearing parts captured with a Hikvision camera for defect detection; not general machine-part recognition.",
  "limitations":["Image count is not yet a labeled-instance count.","No class labels or split semantics are assumed without inspection.","No Vidya model training or accuracy claim is made."]
 }
 a.output.parent.mkdir(parents=True,exist_ok=True)
 a.output.write_text(json.dumps(report,indent=2)+"\n",encoding="utf-8")
 print(json.dumps(report,indent=2))
 if not images or invalid: return 1
 return 0
if __name__=="__main__": raise SystemExit(main())
