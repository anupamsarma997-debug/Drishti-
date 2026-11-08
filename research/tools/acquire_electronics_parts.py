#!/usr/bin/env python3
"""Acquire and audit the public CC0 electronics-parts dataset repository archive.

This is an acquisition/structure audit, not model training or semantic validation.
"""
from __future__ import annotations
import argparse, hashlib, json, re, shutil, zipfile
from collections import Counter
from pathlib import Path, PurePosixPath

IMAGE_EXTS={".jpg",".jpeg",".png",".bmp",".tif",".tiff",".webp"}

def sha256(path: Path) -> str:
    h=hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda:f.read(1024*1024),b""):
            h.update(chunk)
    return h.hexdigest()

def safe_extract(zf: zipfile.ZipFile, target: Path) -> None:
    root=target.resolve()
    for info in zf.infolist():
        name=info.filename.replace("\\","/")
        p=PurePosixPath(name)
        if p.is_absolute() or ".." in p.parts:
            raise ValueError(f"unsafe archive path: {info.filename}")
        out=(target / Path(*p.parts)).resolve()
        if out != root and root not in out.parents:
            raise ValueError(f"archive path escapes destination: {info.filename}")
    zf.extractall(target)

def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument("--archive",type=Path,required=True)
    ap.add_argument("--output",type=Path,required=True)
    ap.add_argument("--work-dir",type=Path,required=True)
    args=ap.parse_args()
    if not args.archive.is_file(): raise SystemExit("source archive missing")
    if args.work_dir.exists(): shutil.rmtree(args.work_dir)
    args.work_dir.mkdir(parents=True)
    report={"source":"https://github.com/praneelchand10/Electronics-Parts-Dataset","source_archive":{"bytes":args.archive.stat().st_size,"sha256":sha256(args.archive)},"license":"CC0-1.0 (repository metadata; verify included LICENSE text)","nested_archives":[],"images":{"count":0,"unique_sha256":0,"dimensions":{},"extensions":{},"invalid":0},"warnings":[]}
    outer=args.work_dir/"source"
    outer.mkdir()
    with zipfile.ZipFile(args.archive) as z:
        bad=z.testzip()
        if bad: raise SystemExit(f"outer archive CRC failure: {bad}")
        safe_extract(z,outer)
    roots=[p for p in outer.iterdir() if p.is_dir()]
    root=roots[0] if len(roots)==1 else outer
    license_files=[p for p in root.rglob("*") if p.is_file() and p.name.upper().startswith("LICENSE")]
    report["license_files"]=[{"path":p.relative_to(root).as_posix(),"bytes":p.stat().st_size,"sha256":sha256(p)} for p in license_files]
    archives=sorted(p for p in root.rglob("*.zip") if p.is_file())
    part_groups={}
    ordinary=[]
    for p in archives:
        m=re.match(r"(.+\.zip)\.(\d{3})$",p.name,re.I)
        if m: part_groups.setdefault((p.parent,m.group(1)),[]).append(p)
        else: ordinary.append(p)
    combined=[]
    for (parent,name),parts in part_groups.items():
        parts.sort(key=lambda p:p.name)
        target=args.work_dir/(parent.name+"_"+name)
        with target.open("wb") as out:
            for part in parts:
                with part.open("rb") as src: shutil.copyfileobj(src,out)
        combined.append((target,"+".join(p.name for p in parts)))
    archive_items=[(p,p.name) for p in ordinary]+combined
    extracted=args.work_dir/"extracted"
    extracted.mkdir()
    seen_hashes=set()
    for index,(archive,label) in enumerate(archive_items):
        item={"name":label,"bytes":archive.stat().st_size,"sha256":sha256(archive),"valid_zip":False,"files":0,"images":0,"image_dimensions":{},"errors":[]}
        dest=extracted/f"archive_{index:02d}"
        dest.mkdir()
        try:
            with zipfile.ZipFile(archive) as z:
                bad=z.testzip()
                if bad: raise ValueError(f"CRC failure in {bad}")
                item["valid_zip"]=True
                item["files"]=sum(1 for x in z.infolist() if not x.is_dir())
                safe_extract(z,dest)
        except Exception as exc:
            item["errors"].append(str(exc))
            report["warnings"].append(f"{label}: {exc}")
            report["nested_archives"].append(item)
            continue
        report["nested_archives"].append(item)
    try:
        from PIL import Image
    except ImportError:
        raise SystemExit("Pillow missing; workflow must install pillow")
    for p in extracted.rglob("*"):
        if not p.is_file() or p.suffix.lower() not in IMAGE_EXTS: continue
        report["images"]["count"]+=1
        report["images"]["extensions"][p.suffix.lower()]=report["images"]["extensions"].get(p.suffix.lower(),0)+1
        digest=sha256(p); seen_hashes.add(digest)
        try:
            with Image.open(p) as im:
                key=f"{im.width}x{im.height}"
                report["images"]["dimensions"][key]=report["images"]["dimensions"].get(key,0)+1
                im.verify()
        except Exception:
            report["images"]["invalid"]+=1
    report["images"]["unique_sha256"]=len(seen_hashes)
    report["images"]["duplicate_files_by_hash"]=report["images"]["count"]-len(seen_hashes)
    report["archive_count"]=len(archive_items)
    report["warnings"].append("This source is limited to a small electronic-parts kitset; it is not a vehicle/farm/industrial machine parts or fault dataset.")
    args.output.parent.mkdir(parents=True,exist_ok=True)
    args.output.write_text(json.dumps(report,indent=2,sort_keys=True)+"\n",encoding="utf-8")
    print(json.dumps(report,indent=2,sort_keys=True))
    if report["images"]["count"]==0 or any(not a["valid_zip"] for a in report["nested_archives"]):
        return 1
    return 0

if __name__=="__main__": raise SystemExit(main())
