# Vidya Global Technical Resource Register
Version: 0.1 — 2026-11-08
Status: Initial research register; not an exhaustive inventory of everything published worldwide.

## Mission and scope
Build Vidya as an independent, safety-first, Android-oriented machine understanding and troubleshooting system. Scope: machines, vehicles, appliances, agricultural equipment, motors, pumps, workshop tools and industrial equipment. No unrelated assistant features.

No Gemini API/Nano, Azure AI API, or hosted proprietary model is part of the target runtime. Public documentation may be studied; private model weights, private training data, restricted manuals, scraped service outputs and reverse-engineered internals are excluded.

## Resource admission rules
Every candidate must record canonical source, exact version/date, asset type, machine domain, license/terms, commercial-use status, redistribution/training permission, provenance, data quality, limitations, Android feasibility and test plan.

A dataset license does not automatically cover its hosting code, paper, annotations, images, or third-party content. Publicly downloadable does not mean public domain. Unclear assets remain on hold until permission is obtained.

Priority labels: P0 = usable foundation after source/license verification; P1 = narrow research/benchmark; HOLD = rights or suitability unclear; EXCLUDE = incompatible.

## Initial public datasets and knowledge sources

### Repair history
1. Open Repair Alliance — Open Repair Data Standard v0.3
   Source: https://openrepair.org/open-data/downloads/
   Repository: https://github.com/openrepair/data
   Source page (Oct 2026) reports 416,282 EEE records + 203,591 non-EEE records, data through July 2026. Stated license: CC BY-SA 4.0.
   Contains product category/brand/year, reported problem, repair status/barrier and session metadata.
   Vidya use: taxonomy discovery, repair barriers, repair outcome analysis, structured repair-report schema.
   Limit: community logs are not verified diagnostic procedures or exact-model service manuals.
   Priority: P0 for analysis, with attribution/share-alike obligations.

2. The Restart Project repair dataset
   Source: https://therestartproject.org/download-dataset/
   Source page reports 130,000+ project records and a combined dataset of 200,000+ attempted repairs. Stated license: CC BY-SA 4.0.
   Vidya use: repair taxonomy and repairability research.
   Limit: inspect exact download/version and data dictionary before merging; check overlap with ORA.
   Priority: P1.

### Predictive maintenance and condition monitoring
3. UCI AI4I 2020 Predictive Maintenance Dataset
   Source: https://archive.ics.uci.edu/dataset/601/ai4i
   10,000 rows; multivariate/tabular predictive-maintenance data. Stated license: CC BY 4.0.
   Important: UCI describes it as synthetic data reflecting industrial predictive-maintenance patterns, not real field measurements.
   Vidya use: pipeline tests, baseline classifier experiments, explainability and failure-mode handling.
   Never present it as real-world Vidya accuracy or validation.
   Priority: P0 for controlled experiments only.

4. NASA Prognostics Center of Excellence Data Repository
   Source: https://www.nasa.gov/intelligent-systems-division/discovery-and-systems-health/pcoe/pcoe-data-set-repository/
   Donated prognostics datasets include turbofan degradation simulation and battery datasets.
   Rights are dataset-specific; the repository does not establish one blanket license for every item.
   Vidya use: time-series prognostics, remaining-useful-life methods, sensor pipeline experiments.
   Limit: aircraft/turbofan or lab battery data do not transfer directly to road vehicles, farm pumps or appliances.
   Priority: HOLD each dataset until individual terms are checked.

5. Paderborn University Bearing DataCenter
   Source: https://mb.uni-paderborn.de/en/kat/research/bearing-datacenter
   Experimental bearing condition-monitoring datasets using vibration and motor-current signals.
   Stated license: CC BY-NC 4.0; non-commercial academic use; commercial use requires contacting authors.
   Vidya use: bearing fault signal-processing research and non-commercial benchmarking only unless permission is obtained.
   Priority: HOLD for commercial distribution; P1 for permitted non-commercial study.

6. Industrial dataset catalogue
   Source: https://github.com/jonathanwvd/awesome-industrial-datasets
   Discovery index covering industrial vision, audio, vibration, time-series and predictive-maintenance datasets.
   It is an index, not a blanket license. Audit every linked dataset independently.
   Priority: P0 as a discovery index; no dataset is automatically approved by appearing in it.

### Industrial visual inspection
7. MVTec Anomaly Detection (MVTec AD)
   Source: https://www.mvtec.com/research-teaching/datasets/mvtec-ad
   More than 5,000 high-resolution images across 15 object/texture categories, with anomaly annotations.
   Stated license: CC BY-NC-SA 4.0; source explicitly says commercial use is not allowed.
   Vidya use: non-commercial research into anomaly-detection methods only; do not ship trained weights or use commercially without permission/legal clearance.
   Priority: HOLD for product training; P1 for non-commercial method study.

8. MVTec AD 2
   Source: https://www.mvtec.com/research-teaching/datasets/mvtec-ad-2
   Advanced industrial anomaly-detection benchmark.
   Stated license: CC BY-NC-SA 4.0; commercial use prohibited by source terms.
   Priority: HOLD for product training; P1 for non-commercial method study.

### On-device Android and local inference projects
9. llama.cpp
   Source: https://github.com/ggml-org/llama.cpp
   Candidate local inference engine for compatible quantized open-weight language models.
   Verify current upstream license, Android build path, model format compatibility, hardware support and each model's license before adoption.
   Vidya use: local language-model runtime candidate; not itself a machine-diagnosis model.
   Priority: P1 pending device benchmarks and dependency review.

10. ONNX Runtime
    Source: https://github.com/microsoft/onnxruntime
    Candidate cross-platform inference runtime for compatible exported vision/classification models.
    Vidya use: compare Android CPU/GPU/NPU execution, APK size, latency, memory and delegate support.
    Priority: P1 pending current Android compatibility and license audit.

11. NCNN
    Source: https://github.com/Tencent/ncnn
    Candidate mobile neural-network inference framework.
    Vidya use: benchmark compact vision models on target Android devices.
    Priority: P1 pending model conversion and device tests.

12. OpenCV
    Source: https://github.com/opencv/opencv
    Candidate computer-vision and image-processing library for preprocessing, geometry and image utilities.
    Vidya use: camera preprocessing and classical vision baselines; not a substitute for trained part recognition.
    Priority: P1 pending Android footprint and module/license review.

13. LiteRT / LiteRT-LM
    Sources: https://ai.google.dev/edge/litert and https://ai.google.dev/edge/litert-lm
    Candidate inference runtimes, not hosted AI services or Gemini/Azure APIs.
    Runtime adoption is a separate decision: if independence later means excluding all Google-maintained runtime libraries too, do not use LiteRT.
    Priority: HOLD pending explicit runtime-vendor preference.

14. OlliteRT Android project
    Source: https://github.com/NightMean/OlliteRT
    Public Android local-LLM server project; project page describes Apache-2.0 licensing and LiteRT-LM use.
    Vidya use: architecture study only; do not import unrelated server features.
    Priority: P1 for code-pattern study; audit repository and dependencies before copying.

15. SmolChat Android project
    Source: https://github.com/shubham0204/SmolChat-Android
    Public Android offline GGUF chat application; project page describes Apache-2.0 and llama.cpp-based inference.
    Vidya use: study model loading, local storage and inference lifecycle; do not import unrelated chat features.
    Priority: P1 for architecture study; audit source and dependencies before reuse.

16. Pocket LLM Android project
    Source: https://github.com/evanemran/pocket_llm
    Public local-model Android project; repository description reports Qwen3 0.6B and optional other model bundles.
    Vidya use: inspect device constraints, model download/import patterns and local inference architecture.
    Each model has its own license/model card; repository license does not grant model-weight rights.
    Priority: P1 for architecture study; model choice remains separately gated.

## Existing Vidya repository audit — baseline
Repository: https://github.com/anupamsarma997-debug/Drishti-
Branch: main
Inspected commit: 05c5093c82681150db25ac88628f743c02eaf253

Current foundations include CameraX, generic ML Kit object boxes, ML Kit Latin/Devanagari OCR, nameplate text parsing, imported-PDF OCR/search, deterministic safety rules, machine-category routing, part-failure triage, selected OBD-II code lookup, and a small Wikipedia discovery client.

Important gaps:
- Generic object detection is not machine/part identification or fault detection.
- Wikipedia discovery is not technical evidence retrieval.
- Imported PDFs are not automatically authenticated or OEM-verified.
- No specialist model has been trained and validated on Vidya-owned field data.
- No validated real-time hazard detector exists.
- No model-specific repair procedure should be generated without exact machine identity and authoritative source/page evidence.

Current dependency conflict with the desired independent-AI direction:
- app/src/main/java/com/vidya/safetyassistant/ai/OnDeviceGeminiNano.kt integrates Gemini Nano.
- app/build.gradle.kts includes com.google.mlkit:genai-prompt.
- Generic object detection and OCR also use Google ML Kit. These are not Gemini or Azure hosted APIs, but are Google-maintained components. Decide separately whether no Gemini/Azure means no model/API dependency only or no Google-maintained libraries at all.
- Do not remove working camera/OCR libraries until equivalent replacements are selected, benchmarked and tested.

## Research and engineering workstreams
1. Legal/provenance ledger: pin versions, checksums, licenses, model cards, dataset terms, attribution and redistribution permissions.
2. Machine identity: make/model/year/variant/serial/nameplate extraction; confidence and user confirmation; distinguish machine from component.
3. Technical evidence: OEM manuals, official service bulletins, government right-to-repair resources, standards and manufacturer parts catalogs; exact source, revision, page and applicability.
4. Repair knowledge schema: symptom → observations → possible causes → required evidence → safe checks → manual procedure → escalation; retain source and uncertainty.
5. Vision: machine family classification, part localization, defect/anomaly detection, OCR and nameplate parsing as separate tasks; use licensed and consented images.
6. Audio: sound event classification for bearing, belt, pump, engine and abnormal noise; never infer definitive diagnosis from sound alone.
7. Sensors: vibration/current/temperature/pressure/RPM where available; calibrated provenance and machine-specific baselines.
8. Diagnostics: OBD-II and manufacturer-specific DTCs only with documented source, vehicle applicability and revision; never guess unknown codes.
9. Local language reasoning: open-weight model with commercial-use permission if needed, quantized for supported Android hardware; deterministic safety gate and evidence retrieval remain authoritative.
10. Offline retrieval: local indexed manuals/knowledge with page-level citations, OCR confidence, exact-model matching and stale-document warnings.
11. Validation: split train/validation/test by physical machine and collection session; test unknown brands, low light, blur, occlusion, OCR digit errors, false positives, abstention and dangerous advice.
12. Field dataset: consent-based capture with machine metadata, fault confirmation, repair outcome and source traceability. No fabricated labels or synthetic data represented as field results.
13. Android optimization: profile RAM, storage, battery, thermals, latency and CPU/GPU/NPU on actual devices; degrade gracefully to rules/manual lookup.
14. Safety governance: hazard-specific stop rules, human confirmation, technician escalation, audit logs and distinction between hypothesis and verified procedure.

## Non-negotiable release gates
- No unsafe instruction without exact model/source grounding.
- No safe claim from generic object detection, OCR, or an unvalidated model.
- No model or dataset enters training until license, provenance and intended-use rights are recorded.
- No accuracy claim without held-out evaluation data and reproducible metrics.
- No camera/document upload without clear user consent.
- No model-specific torque, wiring, pressure, chemical, brake or high-voltage instructions from generic internet snippets.
- When uncertain, abstain and request exact model/source or refer to a qualified technician.

## Next research passes
This register is a starting catalogue, not a claim that every public resource has been found. Continue domain-by-domain for motorcycles/cars and OBD; tractors/farm machinery; pumps/motors; appliances; industrial audio/vibration; machine-part images; public repair manuals/service bulletins; diagnostic standards; multilingual technical corpora; mobile vision models; model quantization and Android runtimes. Record each candidate and reject irrelevant, unsafe, non-reusable or duplicate resources rather than adding them merely to inflate the catalogue.
