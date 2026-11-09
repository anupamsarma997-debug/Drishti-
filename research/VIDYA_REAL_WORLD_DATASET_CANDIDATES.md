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
