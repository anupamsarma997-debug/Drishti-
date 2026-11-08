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

## Historical failed attempt — resolved

### Bonfiglioli bearing defect dataset
- Source: https://zenodo.org/records/11034252
- Source describes actual bearing parts photographed with a HIK vision camera; 97.5 MB RAR. The record does not provide a clear license in its rights field, so this dataset remains quarantined: no training, redistribution or commercial use until permission is clarified.
- Earlier attempt downloaded the 92.98 MiB archive but failed extraction because the runner lacked the expected 7-Zip executable. This was resolved by installing `7zip-rar` and using Ubuntu's `7z` executable.
- The successful, checksum-verified extraction and audit are recorded in the next section. The dataset remains quarantined because the Zenodo record does not state a clear license.


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
- Android CI run 42494527709 passed on commit d2bc0aa7f069810ada3ea3ebc531af2ffa545dc6; debug APK artifact was uploaded (80,372,566 bytes).
- Latest Android CI for subsequent dataset workflow commits is tracked in GitHub Actions; no Android application source change is included in the I3D dataset workflow.
- No Vidya specialist model has been trained yet. Existing archives are separate tasks/formats and must not be naively merged into one class list.
- Next gates: finish bearing archive audit; obtain Roboflow exports only through authorized access; normalize per-dataset labels; split by original source/physical instance where possible; train narrow baselines; evaluate on held-out sources; only then consider Android model integration.


### I3D Tools Dataset — first shard acquired and audited
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42494453950
- Artifact: `vidya-i3d-tools-shard-00000-audited` (468,184,622 bytes; expires 2026-12-08); includes the parquet shard and audit report.
- Shard `train-00000-of-00012.parquet`: 470,815,684 bytes; SHA-256 `b9a3d842e48a09fcd5d3b324dc084518a8814cb0b494b94b12bbf4a093ba0741`.
- 3,065 rows; 3,065 primary images and 3,065 conditioning images; all primary images 1024x1024; 3,065 unique primary image hashes; zero duplicates and zero invalid images.
- 3,065 YOLO annotation rows; 3,065 valid boxes; zero malformed annotations. Shard currently contains class IDs 0: 2,000 boxes and 1: 1,065 boxes (consistent with the source's first two listed categories, ball_bearing and gear; confirm exact mapping against the dataset's official metadata before training).
- This is only 1 of 12 shards (~8.3% by shard count), not the full 5.52 GB dataset. It is synthetic imagery, so it cannot validate real-world workshop recognition.


### I3D Tools Dataset — second shard acquired and audited
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42494664824 (completed successfully).
- Artifact: `vidya-i3d-tools-shard-00001-audited` (453,366,099 bytes; expires 2026-12-08); includes the parquet shard and audit report.
- Shard `train-00001-of-00012.parquet`: 456,191,298 bytes; SHA-256 `1355be106c389c114addd9ef891d8d3864c8612abf3541721f2186f802f88332`.
- 3,065 rows; 3,065 primary images and 3,065 conditioning images; all primary images 1024x1024; 3,065 unique primary image hashes; zero duplicates and zero invalid images.
- 3,065 YOLO annotation rows; 3,065 valid boxes; zero malformed annotations. Shard class ID counts: 1: 935, 2: 2,000, 3: 130.
- Combined I3D shards 00000 and 00001: 6,130 primary images, 6,130 unique primary image hashes, 6,130 valid boxes; still only 2 of 12 shards. Do not claim the whole dataset has been acquired.
- The two shards have overlapping class IDs and are consistent with sequentially numbered categories, but the official zero-based class mapping must be confirmed from the source metadata before producing a final class map.

### I3D Tools Dataset — third shard acquired and audited
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42494928495 (completed successfully).
- Artifact: `vidya-i3d-tools-shard-00002-audited` (482,462,832 bytes; expires 2026-12-08); includes the Parquet shard and audit report.
- Shard `train-00002-of-00012.parquet`: 486,088,143 bytes; SHA-256 `ac406077bf22efe77510b2b375c10a9c601f4e1c0d220ec867951213e6dc6d1e`.
- 3,065 rows; 3,065 primary images and 3,065 conditioning images; all primary images 1024x1024; 3,065 unique primary image hashes within this shard; zero invalid images.
- 3,065 annotation rows; all 3,065 annotation rows valid after correcting the audit parser to support multiple YOLO boxes per image; 7,845 valid bounding boxes; zero malformed rows.
- All 16 class IDs (0–15) appear in this shard. Class ID box counts are recorded in the JSON audit report.
- Combined first three shards: 9,195 primary images, 9,195 conditioning images and 13,975 valid YOLO boxes. Within-shard duplicates are zero; cross-shard duplicate checking remains pending.
- This remains synthetic training material, not real-world validation. No model has been trained.


### I3D Tools Dataset — fourth shard acquired and audited
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42495025879 (download and audit steps succeeded; artifact upload completed).
- Artifact: `vidya-i3d-tools-shard-00003-audited` (494,585,464 bytes; expires 2026-12-08); artifact ID `13622963218`.
- Shard `train-00003-of-00012.parquet`: SHA-256 `9b19899eba4302a50d2ff44505064aa31be0cfc5b2c4c5ce41953916639dc8ab`.
- 3,065 rows, 3,065 primary images, 3,065 unique primary hashes; zero exact duplicates within shard and zero invalid images.
- 3,065 annotation rows; 3,065 valid; 15,325 valid YOLO boxes; zero malformed annotation rows. All 16 class IDs present.
- First four shards combined: 12,260 primary-image rows, 12,260 within-shard unique hashes (cross-shard duplicate scan not yet performed), and 29,300 valid YOLO boxes.
- Data remains synthetic. No training or real-world accuracy claim.
### I3D YOLO preparation pipeline
- Added `research/tools/prepare_i3d_yolo_dataset.py`: exports only primary images (not conditioning images), converts annotations to YOLO labels, removes exact duplicate image hashes, rejects conflicting labels, and makes deterministic 80/10/10 train/val/test splits. Writes `dataset.yaml`, manifest and preparation report.
- Added unit tests in `research/tools/test_prepare_i3d_yolo_dataset.py`, including multi-box parsing and duplicate-image handling.
- Android CI now installs PyArrow/Pillow and runs all dataset audit/preparation tests before Gradle tests and APK build. - CI run https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42495292944 passed the full Python dataset test suite (31 tests), Gradle unit tests and debug APK build; APK artifact ID `13623242531` (80,372,565 bytes). The preparation test first exposed an incorrect test assumption about seed changes; the test was corrected, and the subsequent full run passed.


### I3D Tools Dataset — fifth shard acquired and audited
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42495254135 (completed successfully).
- Artifact: `vidya-i3d-tools-shard-00004-audited` (465,435,238 bytes; expires 2026-12-08); artifact ID `13622394539`.
- Shard `train-00004-of-00012.parquet`: SHA-256 `31c4e5d40e81ebc5e3ad44e15cb862e96aa81678ec69f65e76a262f51a4dbe2a`.
- 3,065 rows, 3,065 primary images, 3,065 unique primary hashes; zero within-shard duplicate rows and zero invalid images.
- 3,065 annotation rows; 5,117 valid YOLO boxes; zero malformed annotation rows.
- First five shards combined: 15,325 primary-image rows, 15,325 within-shard unique hashes (cross-shard duplicate scan still pending), 34,417 valid YOLO boxes.


### I3D Tools Dataset — sixth shard acquired and audited
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42495359661 (completed successfully).
- Artifact: `vidya-i3d-tools-shard-00005-audited` (436,471,658 bytes; expires 2026-12-08); artifact ID `13623053164`.
- Shard `train-00005-of-00012.parquet`: SHA-256 `fc2fe86f00da20037d086339acc2209ac7fea6ff49238bcfa96a2d4a09e06d07`.
- 3,064 rows, 3,064 primary images, 3,064 unique primary hashes; zero within-shard duplicate rows and zero invalid images.
- 3,064 annotation rows; 3,064 valid YOLO boxes; zero malformed annotation rows.
- First six shards combined: 18,389 primary-image rows and 37,481 valid YOLO boxes. Counts are within-shard unique; cross-shard duplicate scan is still pending.


### I3D Tools Dataset — seventh shard acquired and audited
- Workflow: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42495459107 (completed successfully).
- Artifact: `vidya-i3d-tools-shard-00006-audited` (455,409,098 bytes; expires 2026-12-08); artifact ID `13623775041`.
- Shard `train-00006-of-00012.parquet`: SHA-256 `7189510959114fb946114b9b6ead23904984243cc88026fda2f7905d9d4513ed`.
- 3,064 rows, 3,064 primary images, 3,064 unique primary hashes; zero within-shard duplicate rows and zero invalid images.
- 3,064 annotation rows; 3,064 valid YOLO boxes; zero malformed annotation rows.
- First seven shards combined: 21,453 primary-image rows and 40,545 valid YOLO boxes. Counts are within-shard unique; cross-shard duplicate scan is still pending.
