#!/usr/bin/env python3
"""Audit real KIT/2Obe ball-screw defect images and LabelMe annotations."""
import argparse, collections, hashlib, io, json, tarfile, zipfile
from pathlib import PurePosixPath
from PIL import Image, ImageFile
ImageFile.LOAD_TRUNCATED_IMAGES=False
IMG={".jpg",".jpeg",".png",".bmp",".tif",".tiff",".webp"}
def main():
 p=argparse.ArgumentParser()
 p.add_argument("archive"); p.add_argument("--output",required=True)
 a=p.parse_args()
 ext=collections.Counter(); images=[]; jsons=[]; bad=[]; hashes={}; dup=[]; shape_labels=collections.Counter(); json_imagepaths=[]
 with zipfile.ZipFile(a.archive) as z:
  for info in z.infolist():
   if info.is_dir(): continue
   path=info.filename; suffix=PurePosixPath(path).suffix.lower(); ext[suffix or "[none]"]+=1
   if suffix in IMG:
    raw=z.read(info)
    try:
     im=Image.open(io.BytesIO(raw)); im.verify()
     im=Image.open(io.BytesIO(raw)); dims=im.size; fmt=im.format
     digest=hashlib.sha256(raw).hexdigest()
     if digest in hashes: dup.append({"path":path,"same_as":hashes[digest]})
     else: hashes[digest]=path
     images.append({"path":path,"bytes":len(raw),"width":dims[0],"height":dims[1],"format":fmt,"sha256":digest})
    except Exception as e: bad.append({"path":path,"error":str(e)[:300]})
   elif suffix==".json":
    try:
     obj=json.loads(z.read(info))
     shapes=obj.get("shapes",[]) if isinstance(obj,dict) else []
     jsons.append({"path":path,"shapes":len(shapes),"imagePath":obj.get("imagePath")})
    except Exception as e: bad.append({"path":path,"error":"JSON: "+str(e)[:300]})
 image_stems={PurePosixPath(x["path"]).with_suffix("").name for x in images}
 json_stems={PurePosixPath(x["path"]).with_suffix("").name for x in jsons}
 report={
  "source":"2Obe/BSData — KIT industrial ball-screw surface-defect dataset",
  "source_url":"https://github.com/2Obe/BSData",
  "license":"CC BY-SA 4.0 (repository LICENSE)",
  "archive_integrity":"ZIP readable; each image decoded and verified",
  "archive_file_count":sum(ext.values()),
  "extension_counts":dict(sorted(ext.items())),
  "decoded_image_count":len(images),\n  "image_counts_by_parent_folder":dict(collections.Counter(PurePosixPath(x["path"]).parent.name for x in images)),\n  "jpg_count":sum(PurePosixPath(x["path"]).suffix.lower()==".jpg" for x in images),\n  "data_folder_image_count":sum(PurePosixPath(x["path"]).parent.name=="data" for x in images),\n  "demo_folder_image_count":sum(PurePosixPath(x["path"]).parent.name=="demo" for x in images),
  "unique_image_sha256_count":len(hashes),
  "exact_duplicate_images":len(dup),\n  "duplicate_image_paths":dup,
  "invalid_image_or_json_files":bad,
  "labelme_json_count":len(jsons),
  "same_basename_image_json_pairs":len(image_stems & json_stems),
  "images_without_same_basename_json":len(image_stems-json_stems),
  "json_without_same_basename_image":len(json_stems-image_stems),
  "labelme_shapes_total":sum(x["shapes"] for x in jsons),\n  "labelme_shape_label_counts":dict(sorted(shape_labels.items())),\n  "labelme_imagePath_values_missing":sum(not x for x in json_imagepaths),
  "image_dimensions":dict(collections.Counter(f'{x["width"]}x{x["height"]}' for x in images)),
  "samples":images[:10],
  "classification_patches_21835":"NOT INCLUDED IN THIS REPOSITORY ARCHIVE; separate KIT publication link",
  "model_training":"NOT PERFORMED",
  "scope":"Real ball-screw spindle surface photographs with pitting annotations; not general machine-part recognition"
 }
 with open(a.output,"w") as f: json.dump(report,f,indent=2); f.write("\n")
 print(json.dumps(report,indent=2))
if __name__=="__main__": main()
