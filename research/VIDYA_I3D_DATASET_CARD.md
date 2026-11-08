# Vidya I3D Tools Dataset Card

## Status
- Source dataset: I3D-Tools-Dataset, hosted on Hugging Face by `i3dlabiisc`.
- Source page: https://huggingface.co/datasets/i3dlabiisc/I3D-Tools-Dataset
- Source revision used: recorded per-shard in `VIDYA_VISUAL_DATASET_ACQUISITION_LOG.md`.
- Official source reports 36,773 rows, 16 classes and approximately 5.52 GB.
- Dataset is tagged synthetic by its publisher. It is not a substitute for photographs of real workshop tools, vehicle components or industrial machinery.
- Vidya has not trained a model from this dataset and has no measured recognition accuracy from it.

## Label map
| ID | Class |
|---:|---|
| 0 | ball_bearing |
| 1 | gear |
| 2 | hammer |
| 3 | measuring_tape |
| 4 | nail |
| 5 | nut |
| 6 | oring |
| 7 | plier |
| 8 | saw |
| 9 | scissors |
| 10 | screw |
| 11 | screwdriver |
| 12 | spring |
| 13 | utility_knife |
| 14 | washer |
| 15 | wrench |

## Acquisition and audit
- Twelve official Parquet shards were downloaded from the dataset source and individually audited.
- The global audit checks primary `image` bytes by SHA-256 across all 12 shards.
- `conditioning_image` is not counted as a separate original capture or exported as training input.
- Audit reported 36,773 primary image rows, 36,773 globally unique primary image hashes, zero exact duplicate rows, zero missing primary images, zero malformed YOLO annotation rows, 55,865 valid YOLO boxes, and zero conflicting labels for duplicate images.
- Audit scripts: `tools/audit_i3d_tools_parquet.py`, `tools/audit_i3d_cross_shard.py`.
- Preparation script: `tools/prepare_i3d_yolo_dataset.py`.

## Preparation design
- Validate each primary image and each YOLO annotation.
- Deduplicate exact image bytes globally before assigning splits.
- Abort if the same exact image has conflicting labels.
- Deterministic SHA-256-based 80/10/10 train/validation/test split.
- Export only primary images and YOLO labels, plus `dataset.yaml`, `manifest.json`, and `preparation_report.json`.
- The split is deterministic but not class-stratified. Rare classes may have uneven representation; inspect the preparation report before training.

## Licensing and permitted use
- The Hugging Face dataset page identifies the dataset as MIT licensed. That metadata is not, by itself, proof that every embedded image or upstream asset is free of third-party restrictions.
- Preserve the source attribution, license notice, and source revision. Review the dataset card and upstream asset provenance before redistribution or commercial use.
- Do not claim ownership of the source images.

## Intended use and limitations
- Research baseline for 2D object-detection experiments on the 16 listed tool classes.
- Not intended for safety-critical decisions, electrical isolation checks, hidden-fault diagnosis, vehicle roadworthiness, or industrial lockout/tagout decisions.
- It does not cover all machine parts, machine make/model variants, wiring, fault symptoms, or repair procedures.
- Synthetic-to-real domain shift is expected. Real-world validation on independently collected, correctly licensed photographs is mandatory before integrating any detector into Vidya's user-facing advice.

## Model and evaluation status
- Model training: not performed.
- Trained weights: none produced.
- Validation metrics: none measured.
- Real-world test set: not yet collected.
- Android on-device inference integration: not yet validated for this dataset.
- Passing data-integrity audits confirms the files and labels passed the stated checks; it does not demonstrate model performance.
