# Vidya Visual Recognition — Real-World Validation Protocol

## Gate before any user-facing claim
The synthetic I3D tool dataset is a research baseline only. A model trained on it must not be described as reliable for real machinery until it passes a separate, independently captured real-world evaluation. No safety claim may be inferred from object detection.

## Target scope
The current I3D label map contains 16 hand tools / workshop objects:
ball bearing, gear, hammer, measuring tape, nail, nut, O-ring, plier, saw, scissors, screw, screwdriver, spring, utility knife, washer, wrench.

It does not represent a complete inventory of automotive, agricultural, home-appliance, pump, electrical, or industrial machine parts. Do not silently map a visually similar object to an unsupported part class.

## Real-world image collection
1. Collect images only with permission and avoid faces, number plates, addresses, serial numbers, screens, or other personal/confidential information.
2. Record capture metadata without personal identifiers: class, physical object instance ID, device/camera family, approximate distance band, lighting band, background, viewpoint, and occlusion level.
3. Capture multiple independent physical instances per class. Repeated frames or crops of one object are not independent instances.
4. Include difficult but realistic cases: clutter, low light, glare, shadows, partial occlusion, small objects, worn/dirty objects, and different backgrounds.
5. Obtain consent and retain a source/rights record for every image. Exclude images with unclear usage rights.
6. Keep the real-world holdout test set separate from synthetic training data and do not tune thresholds on the final test set.

## Annotation quality
- Use bounding boxes around visible object extents.
- Use only the 16 defined classes. Mark unsupported/ambiguous objects as ignore or out-of-scope, not as a guessed class.
- Have a second reviewer inspect a sample of labels and resolve disagreements before training/evaluation.
- Validate class IDs, normalized coordinates, image integrity, and empty/negative examples.

## Split design
- Split by physical object instance and capture session, not by random adjacent frames.
- Keep all images of the same physical instance in only one split.
- Suggested real-world split: 70% train, 15% validation, 15% locked test, with class balance reviewed before training.
- Preserve a locked test set from different capture sessions and, where possible, different device families and locations.

## Required metrics
Report overall and per-class:
- precision, recall, F1;
- AP50 and AP50:95;
- confusion matrix, false-positive and false-negative examples;
- performance by lighting, distance, occlusion, and device group;
- inference latency, peak memory, model size, and battery/thermal behavior on supported Android phones.

Report sample counts and confidence intervals where feasible. Include unsupported-object false detections and empty-scene false alarms. Never report only aggregate mAP if a class has very few test examples.

## Minimum release gates
These are proposed project gates, not measured results:
- 100% of the test manifest has verified source permission and valid annotations.
- No train/validation/test leakage by exact image hash or physical object instance.
- Every class in the release scope has a meaningful locked-test sample count; otherwise mark it unsupported.
- Publish per-class precision/recall and representative false-positive/false-negative cases.
- Test on real target Android devices; verify offline behavior and resource usage.
- Human review remains required for repair, electrical, vehicle, and machinery safety decisions.

## Safety boundary
Object detection can identify a visible object; it cannot establish that equipment is de-energized, safe to touch, correctly installed, or free from hidden defects. Vidya must never use a detected object or high confidence score as proof of safety. Continue to show uncertainty and direct users to qualified technicians for hazardous work.

## Current status
- Synthetic source dataset audit: completed.
- Global exact-image deduplication: passed.
- Full train/validation/test preparation: in progress; see `VIDYA_VISUAL_DATASET_ACQUISITION_LOG.md`.
- Model training: not performed.
- Real-world dataset: not collected.
- Real-world metrics and Android device benchmarks: not measured.
