# Vidya I3D-Tools Dataset Preparation Report

**Status:** Prepared successfully; training not started.  
**Workflow:** [GitHub Actions run 42496477259](https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42496477259)  
**Prepared dataset artifact:** [vidya-i3d-tools-yolo-prepared.zip](https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42496477259/artifacts/13624217016)  
**Artifact size:** 6,487,378,410 bytes (about 6.49 GB; GitHub artifact retention through 2026-12-08).

## Verified preparation report

The workflow printed the preparation report and completed the artifact upload successfully:

| Measure | Verified value |
|---|---:|
| Source rows across 12 Parquet shards | 36,773 |
| Unique primary images | 36,773 |
| Exact duplicate primary rows removed | 0 |
| Valid YOLO bounding boxes | 55,865 |
| Train images | 29,358 |
| Validation images | 3,725 |
| Test images | 3,690 |
| Total prepared images | 36,773 |
| Classes represented | 16 / 16 |
| Conditioning images exported | No |
| Invalid rows / annotation conflicts | 0 |
| Model training performed | No |
| Real-world validation performed | No |

Split assignment uses SHA-256 image deduplication followed by deterministic hash-based 80/10/10 assignment, seed `20261108`. It is not class-stratified; inspect per-split class distribution before training and report any sparse or missing class in a split.

## Class bounding-box counts

| Class | Boxes |
|---|---:|
| ball_bearing | 2,749 |
| gear | 3,661 |
| hammer | 3,658 |
| measuring_tape | 2,334 |
| nail | 4,331 |
| nut | 3,710 |
| oring | 2,389 |
| plier | 2,803 |
| saw | 3,831 |
| scissors | 3,358 |
| screw | 6,050 |
| screwdriver | 2,908 |
| spring | 5,006 |
| utility_knife | 3,553 |
| washer | 2,423 |
| wrench | 3,101 |

## Important interpretation and limits

- This is the I3D-Tools-Dataset, described by its source as synthetic imagery. It is a tool-detection dataset, not a general machine-parts, fault-diagnosis, bike/car/tractor, or industrial-equipment dataset.
- The primary `image` column alone is counted. The `conditioning_image` column is not treated as another independent capture.
- Zero exact duplicates across the audited 12 shards does not establish visual diversity, annotation quality beyond structural checks, or generalization.
- These counts are data-preparation results, not model metrics. No trained weights, mAP, precision, recall, accuracy, latency, or device performance are claimed.
- The prepared artifact is larger than the GitHub connector's 512 MB direct-download limit; the artifact is retained on GitHub Actions and can be fetched by a workflow using GitHub's artifact download action.

## Training gate / next steps

1. Review the 16-class distribution separately in train, validation, and test; current split is deterministic but not stratified.
2. Select a permissively licensed training implementation compatible with a future commercial Android app. Do not silently accept a restrictive training framework or pretrained weights.
3. Train from random initialization if the goal is an independently trained Vidya model; explicitly record architecture, code/license versions, hyperparameters, random seed, hardware, and checkpoint hashes.
4. Use validation only for model selection. Keep the test split untouched until final evaluation.
5. Report per-class precision, recall, AP50, AP50-95, confusion matrix, and failure examples. Do not convert synthetic test metrics into claims about real workshops.
6. Collect separately consented, real workshop/mobile photographs across physical instances, lighting, backgrounds, occlusion, and device cameras. Split real data by physical instance/session, then evaluate domain shift.
7. Integrate a model into Android only after measured model size, memory, cold/warm latency, thermal behavior, and supported runtime/operator compatibility have passed on actual target phones.

**Safety:** Visual tool detection is not proof that equipment is de-energized or safe. Vidya must not imply that a missing detection means no hazard, and must not issue unverified repair instructions.
