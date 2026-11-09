#!/usr/bin/env python3
"""Audit real KIT/2Obe ball-screw defect images and LabelMe annotations."""
import argparse
import collections
import hashlib
import io
import json
import zipfile
from pathlib import PurePosixPath
from PIL import Image, ImageFile

ImageFile.LOAD_TRUNCATED_IMAGES = False
IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff", ".webp"}

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("archive")
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    ext_counts = collections.Counter()
    images, annotations, invalid, duplicates = [], [], [], []
    hashes = {}
    shape_labels = collections.Counter()
    with zipfile.ZipFile(args.archive) as archive:
        for item in archive.infolist():
            if item.is_dir():
                continue
            path = item.filename
            suffix = PurePosixPath(path).suffix.lower()
            ext_counts[suffix or "[no extension]"] += 1
            if suffix in IMAGE_EXTS:
                raw = archive.read(item)
                try:
                    image = Image.open(io.BytesIO(raw))
                    image.verify()
                    image = Image.open(io.BytesIO(raw))
                    digest = hashlib.sha256(raw).hexdigest()
                    if digest in hashes:
                        duplicates.append({"path": path, "same_as": hashes[digest]})
                    else:
                        hashes[digest] = path
                    images.append({
                        "path": path, "bytes": len(raw), "width": image.width,
                        "height": image.height, "format": image.format, "sha256": digest
                    })
                except Exception as exc:
                    invalid.append({"path": path, "error": str(exc)[:300]})
            elif suffix == ".json":
                try:
                    obj = json.loads(archive.read(item))
                    shapes = obj.get("shapes", []) if isinstance(obj, dict) else []
                    for shape in shapes:
                        shape_labels[str(shape.get("label", "[missing]"))] += 1
                    annotations.append({
                        "path": path, "shapes": len(shapes),
                        "imagePath": obj.get("imagePath") if isinstance(obj, dict) else None
                    })
                except Exception as exc:
                    invalid.append({"path": path, "error": "JSON: " + str(exc)[:300]})

    def basename(path):
        return PurePosixPath(path).with_suffix("").name

    image_stems = {basename(x["path"]) for x in images}
    annotation_stems = {basename(x["path"]) for x in annotations}
    parent_counts = collections.Counter(PurePosixPath(x["path"]).parent.name for x in images)
    jpg_count = sum(PurePosixPath(x["path"]).suffix.lower() == ".jpg" for x in images)
    report = {
        "source": "2Obe/BSData — KIT industrial ball-screw surface-defect dataset",
        "source_url": "https://github.com/2Obe/BSData",
        "license": "CC BY-SA 4.0 (repository LICENSE)",
        "archive_integrity": "ZIP readable; every image decoded and verified",
        "archive_file_count": sum(ext_counts.values()),
        "extension_counts": dict(sorted(ext_counts.items())),
        "decoded_image_count": len(images),
        "image_counts_by_parent_folder": dict(parent_counts),
        "jpg_count": jpg_count,
        "data_folder_image_count": parent_counts.get("data", 0),
        "demo_folder_image_count": parent_counts.get("demo", 0),
        "unique_image_sha256_count": len(hashes),
        "exact_duplicate_images": len(duplicates),
        "duplicate_image_paths": duplicates,
        "invalid_image_or_json_files": invalid,
        "labelme_json_count": len(annotations),
        "same_basename_image_json_pairs": len(image_stems & annotation_stems),
        "images_without_same_basename_json": len(image_stems - annotation_stems),
        "json_without_same_basename_image": len(annotation_stems - image_stems),
        "labelme_shapes_total": sum(x["shapes"] for x in annotations),
        "labelme_shape_label_counts": dict(sorted(shape_labels.items())),
        "labelme_annotations_with_empty_imagePath": sum(not x["imagePath"] for x in annotations),
        "image_dimensions": dict(collections.Counter(f'{x["width"]}x{x["height"]}' for x in images)),
        "sample_images": images[:10],
        "classification_patches_21835": "Not included in this repository archive; separate KIT publication, CC BY-NC 4.0",
        "model_training": "NOT PERFORMED",
        "scope": "Real ball-screw spindle surface photographs with pitting annotations; not general machine-part recognition"
    }
    with open(args.output, "w") as stream:
        json.dump(report, stream, indent=2)
        stream.write("\n")
    print(json.dumps(report, indent=2))

if __name__ == "__main__":
    main()
