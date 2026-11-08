# Vidya Visual Parts AI — Dataset, Training and Reliability Protocol

Status: **design specification only**. This document does not mean that datasets have been downloaded, labels audited, models trained, or accuracy measured.

## 1. Separate the four deliverables

1. **Dataset acquisition and rights audit** — obtain the archive, record its exact version, licence, attribution, permitted commercial use, redistribution restrictions and source checksum.
2. **Dataset preparation** — inspect every annotation schema, map labels to a controlled taxonomy, identify duplicates/near-duplicates, remove corrupt or unusable samples, and preserve provenance.
3. **Model training** — train a task-specific model using a documented configuration and reproducible data split.
4. **Reliability validation** — evaluate on held-out physical machines, makes/models, capture sessions and lighting conditions; publish errors and abstention behaviour.

A source listing or a reported image count is not evidence that any of these stages has been completed.

## 2. Define the task before selecting images

Vidya must not treat all images as one generic “parts” dataset. Keep separate tasks and labels:

- **Machine-family recognition:** motorcycle, car, tractor, pump, appliance, industrial machine, etc.
- **Component-family recognition:** bearing, belt, pulley, filter, hose, connector, switch, capacitor, pump impeller, etc.
- **Fine-grained part identification:** exact part number / OEM fitment. This requires model, year, variant, serial range and authoritative catalogue evidence; an image classifier alone cannot establish fitment.
- **Visual-condition detection:** corrosion, crack, wear, leakage evidence, deformation, missing fastener, etc. These are distinct visual tasks and do not prove root cause or internal condition.
- **Instance retrieval:** determine whether two photographs show the same physical object. This is not the same as semantic component classification.

Never merge these tasks into a single accuracy number.

## 3. Dataset source handling

Candidate sources in the global resource register remain candidates until the actual archive is audited.

- **IRISP industrial spare-parts dataset:** useful for same-object retrieval experiments. Object IDs identify physical instances; they must not be silently converted into semantic labels such as “bearing” or “injector”.
- **Electronics Parts Dataset (CC0):** narrow electronic-component classes only; not a general machine-parts dataset.
- **IVD-SEG / corrosion / machined-workpiece / PCB / wind-turbine datasets:** use only for the specific task and domain described by their source metadata. A turbine-blade “oil leakage” label is not an engine oil-leak label.
- **MVTec AD, AutoVI Renault and KolektorSDD2:** remain on HOLD for commercial Vidya unless the exact licence or separate permission permits the intended commercial use.

For each downloaded archive record: canonical source URL, dataset title/version, download date, archive SHA-256, licence text/version, attribution, commercial-use decision, redistribution decision, number of files, number of valid images, annotation format, label list, image dimensions, known augmentation, and audit notes. Do not bundle third-party datasets into the APK by default.

## 4. Controlled annotation record

Each image/annotation record should retain at least:

- `sample_id` — stable internal identifier, not a filename-derived label.
- `source_dataset`, `source_version`, `source_url`, `licence_id`, `archive_sha256`.
- `original_file_sha256`, image dimensions, file format and decode status.
- `machine_family`, `make`, `model`, `year_or_range`, `variant` — use explicit `unknown` / null when not known; never infer these from appearance.
- `component_label_original` and `component_label_canonical`.
- `task_type`: machine_family, component_family, instance_retrieval, or visual_condition.
- `annotation_type`: image_label, bounding_box, polygon_mask, keypoint, or instance_id.
- `annotator_id`, `reviewer_id`, annotation date, confidence/uncertainty note.
- `capture_group_id`, physical-machine/session identifier where available, and split assignment.
- `is_original_or_augmented`; augmentation must retain a parent sample ID.

Unknown or ambiguous labels must remain unknown/ambiguous and be excluded from supervised fine-grained training until reviewed.

## 5. Quality gates before training

Reject or quarantine samples with broken files, missing rights metadata, unsupported formats, unusable blur/occlusion, conflicting labels, or unclear annotation semantics.

Run:
- exact duplicate detection using file hashes;
- near-duplicate detection using perceptual hashes / image embeddings, followed by human review;
- leakage checks across train/validation/test by physical object, machine, capture session and source video/burst;
- class-count and long-tail analysis;
- annotation spot checks and inter-annotator agreement on a reviewed subset;
- separate counts for original images, augmented images, boxes, masks and unique physical objects.

Do not count augmented copies as independent real-world examples. Do not split adjacent video frames randomly across train and test.

## 6. Training and evaluation protocol

Start with one narrow, useful task and a transparent baseline. Compare a small mobile-friendly classifier/detector against a pretrained baseline only where the licence permits. Record model architecture, input resolution, preprocessing, random seed, training code revision, dataset manifest hash, hyperparameters, compute environment, model checksum and export/conversion steps.

Report, as applicable:
- per-class precision, recall, F1 and support;
- macro-F1 and confusion matrix;
- detection mAP at stated IoU thresholds, or segmentation IoU/Dice;
- top-1 and top-k accuracy for classification;
- calibration / confidence reliability and false-positive/false-negative examples;
- out-of-distribution and unknown-part rejection behaviour;
- model size, cold-start time, sustained latency, memory and battery impact on named Android devices.

Never report only overall accuracy. Keep a final test set untouched until model selection is complete. Publish the test protocol and limitations with every result.

## 7. Reliability and safe product behaviour

- The model may say “possible component family”; it must not claim exact OEM part number or compatibility without verified catalogue evidence.
- Show top candidates and uncertainty; allow “not sure / unknown part”.
- Low confidence, conflicting cues, poor image quality or unsupported machine family must trigger recapture or technician escalation, not a forced guess.
- A photo cannot establish internal damage, electrical isolation, pressure release, safe temperature, or that a machine is safe to touch.
- Do not instruct users to touch live wiring, moving parts, hot/pressurised systems, bypass guards/interlocks, or perform hazardous repairs.
- Never equate a visual condition label with a confirmed root-cause diagnosis.

## 8. Proposed release gates (targets, not achieved results)

These are proposed minimum gates for a pilot, to be reviewed by a qualified domain team before release:

1. 100% of training samples have traceable source and rights metadata.
2. 100% of test samples are separated by physical machine / capture group from training.
3. Each released class has a documented minimum number of distinct physical instances and independent capture sessions; class-specific counts must be published rather than hidden in a total.
4. A domain expert reviews the class map and a statistically meaningful sample of annotations.
5. Per-class metrics, confusion matrix, calibration, unknown rejection and failure examples are published.
6. Field evaluation includes multiple devices, lighting, angles, dirt, occlusion and different makes/models.
7. Safety-critical categories remain advisory-only until independently validated; exact-part fitment remains blocked without authoritative model-specific catalogue verification.

Passing a CI build does not pass these dataset or field-validation gates.

## 9. Current truthful status

- Source research/register exists; it is not a locally acquired dataset.
- No 141,937-image archive has been downloaded, hashed, inspected or labelled by Vidya.
- No image dataset has been imported into the Android app.
- No Vidya parts-recognition model has been trained or benchmarked.
- Existing camera object detection is generic on-device detection, not specialist parts recognition.
- Android CI can test compilation/unit tests; it cannot validate camera performance or real machine recognition.

## 10. Immediate execution order

1. Select one narrow pilot taxonomy (suggested: common electrical/electronic components OR a small set of agricultural-machine components; do not mix both in one first model).
2. Audit one source archive end-to-end, including licence and label semantics.
3. Generate a machine/object-grouped manifest and duplicate/leakage report.
4. Have domain reviewers correct labels and define an explicit unknown class.
5. Train a baseline outside the Android app; retain a reproducible training report.
6. Evaluate on independently captured images from physical machines.
7. Only after passing review, export and integrate a mobile model; keep the existing generic detector clearly labelled as generic until then.

---
This protocol is an engineering plan, not evidence of model performance, dataset ownership, OEM approval, or safety certification.


## 11. Separate visual and sensor dataset tracks

| Track | Suitable data | What it can support | What it cannot prove |
|---|---|---|---|
| Workshop tool detection | Hardware Tools Complete Dataset | Detect listed workshop tool categories | Internal machine-part identity or fault diagnosis |
| Car exterior parts/damage | Humans in the Loop CC0 dataset | Exterior panel localisation and visible damage segmentation | Engine, brake, transmission or electrical faults |
| Electronics components | ElectroCom61 v2 | Listed electronic-component detection | Vehicle/equipment fitment or failure diagnosis |
| Industrial instance retrieval | IRISP | Same-object retrieval across views | Semantic part name, fault state, OEM part number |
| Motor/pump/gearbox signals | Vibration/acoustic/current/temperature datasets | Narrow sensor-based condition monitoring | Camera recognition or field-wide fault diagnosis |
| Indian road scenes | IDD | Vehicle/road-scene context | Mechanical parts or machine repair |

## 12. Acquisition priority and blockers

1. Hardware Tools Complete Dataset: obtain the Mendeley archive; preserve raw and augmented subsets separately; audit COCO categories, image counts, duplicate hashes, source-image lineage, split leakage and attribution before training.
2. Humans in the Loop car parts/damage: publisher access form is required; obtain archive and confirm CC0 notice before use.
3. ElectroCom61 v2: obtain the 2,121-image archive and audit the primary data.yaml class-ID mapping; keep electronics as a separate pilot.
4. IRISP: archive index is available but listed archives total about 80 GB; do not pull all archives to a phone. Start only with a documented minimal subset if permitted by upstream license/split protocol.
5. Sensor datasets: defer until a compatible sensor-input pathway and signal-processing evaluation plan exist.

No model training begins until archive bytes, licence files, class taxonomy, labels, duplicate/leakage reports and a reproducible split have been checked. Do not bundle multi-gigabyte source datasets in the Android APK.
