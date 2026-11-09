#!/usr/bin/env python3
"""Structural audit for the University of Strathclyde real bolt dataset archive."""
import argparse
import collections
import hashlib
import json
import tarfile
from pathlib import PurePosixPath

IMAGE_EXTS={".jpg",".jpeg",".png",".bmp",".tif",".tiff",".webp"}
LABEL_EXTS={".txt",".xml",".json",".csv"}
def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("archive")
    ap.add_argument("--output",required=True)
    args=ap.parse_args()
    ext=collections.Counter()
    samples=[]
    members=0
    total_bytes=0
    images=[]
    labels=[]
    with tarfile.open(args.archive,"r:gz") as tf:
        for m in tf:
            if not m.isfile():
                continue
            members+=1
            p=PurePosixPath(m.name)
            e=p.suffix.lower()
            ext[e or "[no extension]"]+=1
            total_bytes+=m.size
            if len(samples)<100:
                samples.append({"path":m.name,"bytes":m.size})
            if e in IMAGE_EXTS:
                images.append({"path":m.name,"bytes":m.size})
            elif e in LABEL_EXTS:
                labels.append({"path":m.name,"bytes":m.size})
    image_stems={PurePosixPath(x["path"]).with_suffix("").as_posix() for x in images}
    label_stems={PurePosixPath(x["path"]).with_suffix("").as_posix() for x in labels}
    report={
      "source":"University of Strathclyde AFRC_CRAD_1718_COREF_Bolts_Detection",
      "publisher_url":"https://pureportal.strath.ac.uk/en/datasets/afrccrad1718corefboltsdetection/",
      "archive":"obj_train_data.tar.gz",
      "archive_integrity":"gzip stream and tar member listing passed before audit",
      "file_members":members,
      "uncompressed_member_bytes":total_bytes,
      "extension_counts":dict(sorted(ext.items())),
      "image_files":len(images),
      "annotation_files":len(labels),
      "image_annotation_same_stem_pairs":len(image_stems & label_stems),
      "images_without_same_stem_annotation":len(image_stems-label_stems),
      "annotations_without_same_stem_image":len(label_stems-image_stems),
      "sample_members":samples,
      "image_decode_audit":"NOT PERFORMED",
      "image_hash_deduplication":"NOT PERFORMED",
      "annotation_semantic_validation":"NOT PERFORMED",
      "model_training":"NOT PERFORMED",
      "license":"CC BY 4.0 per University of Strathclyde dataset record",
      "scope":"Real labelled electronic-enclosure images for detecting bolt presence/location; not a general machine-parts dataset"
    }
    with open(args.output,"w") as f:
        json.dump(report,f,indent=2)
        f.write("\n")
    print(json.dumps(report,indent=2))
if __name__=="__main__":
    main()
