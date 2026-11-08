# Vidya Visual Dataset Acquisition Log
Updated: 2026-11-08
Repository: https://github.com/anupamsarma997-debug/Drishti-

This log separates downloaded archives from extracted/validated images, and both from model training. No model accuracy is claimed here.

## Successfully acquired and audited

### Electronics-Parts-Dataset
- Source: https://github.com/praneelchand10/Electronics-Parts-Dataset
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42493272704
- Artifact: `vidya-electronics-parts-acquired` (236,045,799 bytes; expires 2026-12-08)
- Downloaded source archive: 95,160,178 bytes; SHA-256 `db259bddb1f9788ebb7dc0040139811496b93f3deb967edfaf6e4f89a6debfd6`.
- Ten nested ZIPs passed CRC checks and extracted. 10,480 PNG files decoded; zero invalid and zero byte-identical duplicates.
- Important: files include multiple resolution variants; they are not 10,480 independent physical captures. The paper describes 1,734 original 30x30 crops.
- Scope: capacitor, potentiometer, TO-220 linear voltage regulator only.
- Rights: GitHub LICENSE says CC0-1.0 while paper says CC BY 4.0. Use conservative attribution/CC BY 4.0 pending author clarification.

### PCB component detection dataset
- Source: https://github.com/wangshnegqi/pcb-component-detection-dataset-yolo
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42493627038
- Included in artifact `vidya-additional-visual-parts` (565,332,682 bytes; expires 2026-12-08).
- Archive: 143,579,599 bytes; 130 images, 130 unique hashes, zero invalid/duplicate images; 130 label files; 3,300 valid YOLO label rows; 10 classes.
- Rights noted in source: dataset CC BY 4.0; repository code Apache-2.0.
- Scope: PCB/electronics components, not general mechanical parts.

### Ultralytics Carparts-Seg
- Source: https://docs.ultralytics.com/datasets/segment/carparts-seg/
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42493627038
- Included in artifact `vidya-additional-visual-parts`.
- Archive: 139,535,504 bytes; 3,833 images, 3,833 unique hashes, zero invalid/duplicate images; 3,833 label files; 20,374 valid YOLO label rows; 23 classes; all 640x640.
- Source documentation states CC BY 4.0 and cites the original Roboflow dataset.
- Scope: vehicle exterior/body components; does not cover engine internals or diagnose faults.

## Acquisition in progress / extraction repair

### Bonfiglioli bearing defect dataset
- Source: https://zenodo.org/records/11034252
- Source describes actual bearing parts photographed with a HIK vision camera; 97.5 MB RAR. The record does not provide a clear license in its rights field, so this dataset remains quarantined: no training, redistribution or commercial use until permission is clarified.
- First acquisition workflow downloaded the complete 92.98 MiB archive, SHA-256 `26f7cf53d0c9d5c6591e0aab619e184022ccad875f6f9510e00ac32121c790c5`, but `unar` failed on 8 files during extraction. No image count is claimed.
- Fix committed: add Zenodo MD5 verification against `d252807928f29c11c2a6344afb7f8eef`, install 7-Zip and use it as an extraction fallback.
- Retry workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42494131192
- Android CI for the same code commit: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42494131151
- Do not mark this dataset acquired/usable until checksum, extraction, image decode and audit report all pass.


### Bonfiglioli bearing defect dataset — archive acquired and audited
- Source: https://zenodo.org/records/11034252
- Successful workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42494281941
- Artifact: `vidya-bonfiglioli-bearing-dataset` (213,476,597 bytes; expires 2026-12-08); includes original RAR, extracted files and JSON report.
- Archive: 97,505,343 bytes; SHA-256 `26f7cf53d0c9d5c6591e0aab619e184022ccad875f6f9510e00ac32121c790c5`; Zenodo MD5 checksum matched.
- Extraction: 59 files; 58 images; 57 unique image hashes; 1 duplicate; 0 invalid images.
- Image dimensions vary; images are high-resolution Hikvision captures in folders named for part/defect groups.
- License remains unstated in the record rights field at audit time. Quarantine only: do not train, redistribute or use commercially until rights holder clarifies.
- Scope is narrow laboratory bearing-defect imagery, not general-purpose machine-part recognition. No labels or class meanings are assumed merely from folder names.

## Promising candidates not yet downloaded

### add_real_parts_images (Roboflow Universe)
- Source: https://universe.roboflow.com/ayush-bf338/add_real_parts_images
- Public project page lists 6,777 images, 25 instance-segmentation classes and CC BY 4.0.
- Classes include screw, nut, bolt, washer, bearing, bearing block, bush, chain, clamp, clutch, collar, coupling, gear, gearbox, hydraulic cylinder, impeller, motor pump, pulley, seal, shaft, snap ring, spring and valve.
- Export is not yet acquired. Roboflow version export needs an authenticated export flow/API key; never paste a private key into chat. If needed, add it only as a GitHub Actions repository secret. Check source overlap before merging.

### real_img_parts (Roboflow Universe)
- Source: https://universe.roboflow.com/ayush-4s4m4/real_img_parts
- Public project page lists 6,235 images, 25 similar classes and CC BY 4.0.
- Likely overlaps with add_real_parts_images; do not sum image counts or treat it as independent until hashes/source provenance are compared.
- Not downloaded.

## Current engineering status
- Android CI run 42493860995 passed on commit 82c1416231b51c02c9aba119d068a2f811671c29; the debug APK was built and uploaded.
- Android CI run 42494131151 is testing the checksum/7-Zip retry commit.
- No Vidya specialist model has been trained yet. Existing archives are separate tasks/formats and must not be naively merged into one class list.
- Next gates: finish bearing archive audit; obtain Roboflow exports only through authorized access; normalize per-dataset labels; split by original source/physical instance where possible; train narrow baselines; evaluate on held-out sources; only then consider Android model integration.
