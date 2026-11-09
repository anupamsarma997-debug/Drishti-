# Vidya Real-World Visual Dataset Candidates

Updated: 2026-11-09

This register distinguishes real photographs from synthetic/augmented images and records licensing before any training or redistribution.

## Priority candidate: Hardware Tools Complete Dataset (Mendeley Data, version 2)

- Dataset: https://data.mendeley.com/datasets/gmjkzdxhc6/2
- DOI: https://doi.org/10.17632/gmjkzdxhc6.2
- Published: 2026-10-15, version 2.
- License shown by the publisher: Creative Commons Attribution 4.0 International (CC BY 4.0).
- Dataset description: 24 fine-grained workshop hardware and hand-tool categories, captured in uncontrolled workshop environments using three smartphone camera modules (OnePlus Nord CE 4, Nothing CMF Phone 2 Pro, Nothing Phone 2).
- Annotation format: COCO JSON with 2D bounding boxes.
- Version 2 archive description: 4,633 original, unaugmented workshop images split into train/validation/test; plus 12,481 augmented benchmark images (10,464 train, 1,004 validation, 1,013 test) and 14,161 annotated instances.
- Version 2 says duplicate-image removal was performed to reduce cross-split leakage and class distribution imbalance was addressed.
- Source code and benchmark pipelines: https://github.com/mark-0polo/ML-CODES
- This is a much better real-photo validation candidate than synthetic-only I3D. However, it is still a hardware/tools dataset, not a comprehensive set of vehicle/industrial machine assemblies or fault labels.
- Acquisition status: candidate identified; archive not yet downloaded or audited by Vidya. An automated request to Mendeley's public archive metadata endpoint returned HTTP 401 (2026-11-09); its API docs require OAuth2. No token was available, and no credential was requested from the user or stored. The workflow now records this as an access limitation instead of claiming acquisition.
- Before use: download the publisher's exact version 2 archive, verify SHA-256, inspect archive layout and COCO category IDs, audit image integrity and duplicates, check raw vs augmented overlap, preserve original train/validation/test partitions, and retain attribution. Do not claim it has been acquired until the workflow confirms extraction and audit.

## Other candidates / caveats

### RoboCup @work 2023
- Source: https://zenodo.org/records/10003915
- Public Zenodo record exposes a 386.0 MB YOLO archive and 18 industrial-object classes (1,516 samples), plus a separate cavities dataset.
- The record's Rights section does not show a clear license. Do not train or redistribute until the rights holder clarifies permission.
- Competition-specific objects and relatively small sample count; not broad machine repair data.

### Mechanical Parts Dataset 2022
- Source: https://zenodo.org/records/7499618
- Record describes 2,250 images covering bearings, bolts, gears and nuts with 10,597 manual labels.
- Files are restricted/login-gated on the public record. Do not claim acquisition or bypass access restrictions.

### Industrial Machine Tool Element Surface Defect Dataset
- Source: https://radar.kit.edu/radar/en/dataset/JyTQstydAOIheMLN.Industrial%2BMachine%2BTool%2BElement%2BSurface%2BDefect%2BDataset
- 121.9 MB dataset; CC BY-SA 4.0; focused on surface defects of machine-tool elements.
- This may help a narrow defect-recognition experiment after inspecting its file structure and label schema. Share-alike terms need to be reviewed before combining with other assets or distributing a model.

## Vidya data policy

1. Synthetic, augmented, and original real photographs must be tracked separately.
2. Conditioning images and derived augmentations are not counted as independent original captures.
3. Never merge class IDs from different datasets without an explicit taxonomy mapping and source provenance.
4. Deduplicate exact hashes globally before splitting; split by physical object/session where metadata permits.
5. Keep licensing evidence, attribution, checksums, and source version in the dataset card.
6. Model training and real-world validation are separate gates. Synthetic test metrics cannot establish workshop performance.


## Acquired real dataset: KIT ball-screw surface defects (2Obe/BSData)

- Upstream: https://github.com/2Obe/BSData
- License: CC BY-SA 4.0, as declared by the upstream repository LICENSE. Share-alike obligations apply; do not assume a derived combined model can be distributed under a different license.
- Scope: real industrial ball-screw spindle surface photographs, with LabelMe JSON pitting annotations for a subset. This is a narrow industrial defect dataset, not general object/part recognition.
- Acquisition workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42565327267
- Acquired artifact: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42565327267/artifacts/13650593145 (122,256,410 bytes; ZIP integrity and image decode audit completed; 30-day retention).
- Initial archive audit: 1,107 images decoded; 1,106 unique SHA-256 hashes; 1 exact duplicate; 395 LabelMe JSON files; 394 same-basename image/JSON pairs; 485 annotation shapes; zero invalid image or JSON files.
- The repository README describes 1,104 dataset images and 394 annotated images. The archive also contains demo material; the audit script is being refined to report dataset-folder images separately from demo images and to report per-label annotation counts. Use that refined report before preparing training data.
- The repository also references a separate 21,835-patch classification dataset hosted by KIT. Its separate Hugging Face record lists CC BY-NC 4.0, so it is not cleared for commercial app/model use; it has not been acquired by this workflow.
- Model training: not performed. No recognition accuracy or field performance claim.

## Acquisition blockers encountered

### Strathclyde electronic-enclosure bolt dataset
- Publisher record: https://pureportal.strath.ac.uk/en/datasets/afrccrad1718corefboltsdetection/
- Publisher lists a 3.03 GB archive and CC BY 4.0.
- Automated GitHub Actions runner received HTTP 403 from the direct archive URL, so the archive was not acquired. Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42565227323.
- Do not report it as downloaded.

### Mendeley Hardware Tools Complete Dataset
- Candidate: https://data.mendeley.com/datasets/gmjkzdxhc6/2
- Publisher describes 24 real workshop tool/hardware categories and CC BY 4.0.
- Public archive metadata endpoint returned HTTP 401 without OAuth; no credentials were available or requested. No archive acquired. Access-check workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42564731466.


## Additional real-data leads (not yet acquired)

### I-AsSET — 32 industrial mechanical-component classes
- Source: https://huggingface.co/datasets/moiaraya/I-AsSET
- Dataset card describes 508 annotated real images across 32 mechanical-component classes and four industrial scenes, captured with a Zivid industrial RGB-D camera. The same repository also contains 8,000 synthetic training images, 3D assets, and model checkpoints; do not mix those with its 508 real test images.
- The full repository reports 218 GB, so only the real RGB images and their matching COCO/YOLO annotations should be considered for a targeted partial download.
- License: the card points to a repository `LICENSE` file, but the actual terms have not yet been verified in this workflow. Status: HOLD — do not download/use until the license text is inspected and confirms permission for the intended training and distribution.
- The 508 real images are a useful complement to the ball-screw pitting dataset because they cover multiple mechanical component classes, but are still not a broad field dataset for vehicles or every machine type.

### 3CAD — real 3C manufacturing anomalies
- Source repo: https://github.com/EnquanYang2022/3CAD
- Paper and project describe 27,039 real production-line images across 8 manufactured-part categories and 47 anomaly types with pixel-level masks.
- A Google Drive archive is linked by the project, but a clear dataset license was not verified in the source materials inspected. Status: HOLD — no download/training/redistribution until rights are clarified.
- This is an anomaly-localization dataset for aluminum/copper/iron 3C parts, not general machine-part recognition.

### M2 screw defect dataset
- Source: https://data.mendeley.com/datasets/2y6mcsyrpy/2
- Publisher describes 5,536 screw-head/thread OK/NG images and CC BY 4.0.
- The two RAR archives are 2.72 GB and 1.02 GB. Mendeley public metadata access has required OAuth in the earlier acquisition test; this specific dataset has not yet been downloaded or audited. Status: candidate, not acquired.
