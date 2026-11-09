#!/usr/bin/env python3
"""Prepare KIT/2Obe LabelMe pitting polygons as a leakage-aware YOLO-seg dataset."""
import argparse
import collections
import hashlib
import json
import shutil
from pathlib import Path
from PIL import Image

def find_repo(root: Path) -> Path:
    if (root / "data").is_dir() and (root / "label").is_dir():
        return root
    candidates = [p for p in root.iterdir() if p.is_dir() and (p / "data").is_dir() and (p / "label").is_dir()]
    if len(candidates) != 1:
        raise ValueError(f"Expected one extracted BSData root, found {len(candidates)}")
    return candidates[0]

class DSU:
    def __init__(self, values):
        self.parent = {v: v for v in values}
    def find(self, x):
        while self.parent[x] != x:
            self.parent[x] = self.parent[self.parent[x]]
            x = self.parent[x]
        return x
    def union(self, a, b):
        ra, rb = self.find(a), self.find(b)
        if ra != rb:
            self.parent[rb] = ra

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--source-root", required=True)
    ap.add_argument("--output", required=True)
    args = ap.parse_args()
    repo = find_repo(Path(args.source_root))
    out = Path(args.output)
    if out.exists():
        shutil.rmtree(out)
    for split in ("train", "val", "test"):
        (out / split / "images").mkdir(parents=True)
        (out / split / "labels").mkdir(parents=True)

    metadata = json.loads((repo / "image_data.json").read_text())
    meta_by_annotation = {x["annotations"]: x for x in metadata["images"] if x.get("annotations")}
    records = []
    rejected = []
    seen_hashes = {}
    for label_path in sorted((repo / "label").glob("*.json")):
        image_name = label_path.with_suffix(".jpg").name
        image_path = repo / "data" / image_name
        if not image_path.is_file():
            rejected.append({"annotation": label_path.name, "reason": "matching data image missing"})
            continue
        meta = meta_by_annotation.get(label_path.name)
        if not meta or meta.get("filename") != image_name:
            rejected.append({"annotation": label_path.name, "reason": "metadata mapping missing or inconsistent"})
            continue
        try:
            obj = json.loads(label_path.read_text())
            with Image.open(image_path) as im:
                im.verify()
            with Image.open(image_path) as im:
                width, height = im.size
            if obj.get("imagePath") != image_name:
                raise ValueError("LabelMe imagePath does not match paired image")
            polygons = []
            for shape in obj.get("shapes", []):
                if shape.get("label") != "Pitting" or shape.get("shape_type") != "polygon":
                    raise ValueError("Unexpected label or non-polygon shape")
                pts = shape.get("points", [])
                if len(pts) < 3:
                    raise ValueError("Polygon has fewer than three points")
                norm = []
                for point in pts:
                    x, y = float(point[0]), float(point[1])
                    if not (0 <= x <= width and 0 <= y <= height):
                        raise ValueError("Polygon coordinate outside image bounds")
                    norm.extend((x / width, y / height))
                polygons.append("0 " + " ".join(f"{v:.8f}" for v in norm))
            if not polygons:
                rejected.append({"annotation": label_path.name, "reason": "no pitting polygons"})
                continue
            digest = hashlib.sha256(image_path.read_bytes()).hexdigest()
            if digest in seen_hashes:
                rejected.append({"annotation": label_path.name, "reason": "duplicate image hash", "same_as": seen_hashes[digest]})
                continue
            seen_hashes[digest] = image_name
            source_split = meta.get("train_test_split")
            if source_split not in ("train", "test"):
                raise ValueError(f"Unknown source split: {source_split}")
            wear_groups = meta.get("wear_dev") or [f"ungrouped:{image_name}"]
            records.append({
                "image": image_path, "annotation": label_path, "image_name": image_name,
                "polygons": polygons, "source_split": source_split,
                "wear_groups": wear_groups, "type": meta.get("type"), "sha256": digest
            })
        except Exception as exc:
            rejected.append({"annotation": label_path.name, "reason": str(exc)})

    train_records = [r for r in records if r["source_split"] == "train"]
    test_records = [r for r in records if r["source_split"] == "test"]
    # Keep any overlapping wear-development sequence entirely within one split.
    dsu = DSU([r["image_name"] for r in train_records])
    owner = {}
    for r in train_records:
        for group in r["wear_groups"]:
            if group in owner:
                dsu.union(r["image_name"], owner[group])
            else:
                owner[group] = r["image_name"]
    components = collections.defaultdict(list)
    for r in train_records:
        components[dsu.find(r["image_name"])].append(r)
    component_list = sorted(
        components.values(),
        key=lambda group: hashlib.sha256("|".join(sorted(x["image_name"] for x in group)).encode()).hexdigest()
    )
    val_components = set()
    if len(component_list) >= 2:
        target = max(1, round(len(train_records) * 0.15))
        count = 0
        for idx in reversed(range(1, len(component_list))):
            if count >= target:
                break
            val_components.add(idx)
            count += len(component_list[idx])
    split_records = {"train": [], "val": [], "test": list(test_records)}
    for idx, group in enumerate(component_list):
        split = "val" if idx in val_components else "train"
        split_records[split].extend(group)

    class_shapes = collections.Counter()
    split_shapes = collections.Counter()
    for split, group in split_records.items():
        for r in group:
            shutil.copy2(r["image"], out / split / "images" / r["image_name"])
            label_name = Path(r["image_name"]).with_suffix(".txt")
            (out / split / "labels" / label_name).write_text("\n".join(r["polygons"]) + "\n")
            class_shapes.update([0] * len(r["polygons"]))
            split_shapes[split] += len(r["polygons"])

    report = {
        "source": "2Obe/BSData / KIT ball-screw surface-defect dataset",
        "license": "CC BY-SA 4.0; retain attribution and share-alike",
        "task": "single-class Pitting polygon segmentation",
        "source_annotated_records_valid": len(records),
        "source_train_annotated_images": len(train_records),
        "source_test_annotated_images": len(test_records),
        "prepared_split_images": {k: len(v) for k, v in split_records.items()},
        "prepared_split_pitting_polygons": dict(split_shapes),
        "total_pitting_polygons": sum(split_shapes.values()),
        "train_validation_group_components": len(component_list),
        "validation_split_method": "deterministic connected-component split over shared wear_dev sequence IDs; original source test split held untouched",
        "source_test_split_preserved": True,
        "class_names": ["Pitting"],
        "rejected_records": rejected,
        "training_performed": False,
        "real_world_phone_validation": False,
        "safety_note": "Pitting segmentation is a narrow surface-defect task; it does not diagnose all machine failures or prove equipment safe."
    }
    (out / "data.yaml").write_text(
        "path: .\ntrain: train/images\nval: val/images\ntest: test/images\nnc: 1\nnames: [Pitting]\n"
    )
    (out / "preparation_report.json").write_text(json.dumps(report, indent=2) + "\n")
    (out / "ATTRIBUTION.txt").write_text(
        "Source: 2Obe/BSData, KIT ball-screw surface-defect dataset.\n"
        "License: Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0).\n"
        "Retain upstream attribution and comply with share-alike terms for adaptations.\n"
        "This is a narrow industrial pitting dataset, not a general machine-parts dataset.\n"
    )
    print(json.dumps(report, indent=2))
    if rejected:
        print(f"WARNING: {len(rejected)} annotation records were rejected; inspect report before training.")

if __name__ == "__main__":
    main()
