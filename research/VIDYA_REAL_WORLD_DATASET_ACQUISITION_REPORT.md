# Vidya Real-World Dataset Acquisition Report

Updated: 2026-11-09

## Acquired: KIT / 2Obe ball-screw surface-defect dataset

<badge>Not a trained model</badge>

- Upstream source: https://github.com/2Obe/BSData
- Upstream license: CC BY-SA 4.0 (repository LICENSE); preserve attribution and share-alike terms.
- Acquisition run: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42565434806
- Acquired artifact: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42565434806/artifacts/13651292184
- Artifact size: 122,256,919 bytes (~122.3 MB); 30-day GitHub Actions retention.
- ZIP archive integrity passed. Every image file in the archive decoded successfully; every JSON annotation parsed successfully.

### Verified dataset counts

| Measure | Count |
|---|---:|
| Canonical real images in upstream `data/` folder | 1,104 |
| Exact unique hashes across canonical data images | 1,104 |
| LabelMe annotation JSON files | 395 |
| Matching image/annotation basename pairs | 394 |
| Annotated pitting polygon shapes | 485 |
| Polygon label | Pitting |
| Invalid/corrupt image or JSON files | 0 |
| Exact duplicate across full archive | 1 |
| Demo images (excluded) | 2 |

The one exact duplicate is an extra copy under `data/test/` of a canonical image already present directly under `data/`. It is not counted as an additional image. The archive also contains two demo images; exclude them from training. One of the 395 JSON files has no matching image basename. Do not use it without resolving the mismatch.

### What the dataset actually covers

Real images of ball-screw spindle surfaces, with pitting/wear annotations for a subset. The repository describes 1,104 images and 394 annotated images. This is useful for a narrow industrial surface-defect research task, not general object recognition across motors, pumps, vehicles, tractors, home appliances, or all machine parts.

The separate 21,835-image ball-screw classification patch dataset is not included in this acquired archive. Its separate Hugging Face record states CC BY-NC 4.0; it is not cleared for commercial use and has not been acquired.

## Failed acquisition attempts (not counted as acquired)

### University of Strathclyde electronic-enclosure bolt dataset
- Publisher describes a 3.03 GB archive and lists CC BY 4.0.
- GitHub Actions runner got HTTP 403 from the publisher download endpoint.
- Run: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42565227323
- Status: NOT acquired.

### Mendeley Hardware Tools Complete Dataset
- Publisher describes 24 real workshop hardware/tool classes and CC BY 4.0.
- Public archive metadata endpoint returned HTTP 401 and requires OAuth.
- Access-check run: https://github.com/anupamsarma997-debug/Drishti-/actions/runs/42564731466
- Status: NOT acquired.

## Next safe data-preparation gate

1. Extract only canonical `data/` images and their 394 matching LabelMe JSON files.
2. Validate every polygon coordinate against its corresponding image dimensions; retain the upstream source split/metadata and inspect wear-development group IDs in `image_data.json`.
3. Split by wear-development / physical sequence group where metadata permits, never random-split adjacent frames from the same wear sequence across train and test.
4. Convert polygon masks to a documented segmentation format only after annotation QA.
5. Keep synthetic I3D tool-detection data separate from these real industrial defect images. Do not merge incompatible tasks/classes.
6. Train and report only after data rights, split integrity, and model/framework license review.

## Safety and evidence limits

- Acquisition and image/annotation structural audit are complete.
- No model has been trained on this dataset.
- No accuracy, precision, recall, mAP, phone latency, or real-world repair capability is claimed.
- Surface-pitting images cannot diagnose all bearing or machine failures and cannot prove a machine is safe or de-energized.
