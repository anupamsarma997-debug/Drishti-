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


## Second research pass — vehicle diagnostics, appliances and machine sound

17. OBDex — generic OBD-II diagnostic code and PID database
   Source: https://github.com/foerbsnavi/obdex
   Source states 9,533 generic OBD-II codes in English/German; data CC0-1.0, tooling MIT. Includes 132 PIDs and source references per code.
   Vidya use: candidate expansion of generic OBD-II lookup and PID metadata, subject to schema/provenance review and unit tests.
   Limits: generic codes only; OEM-specific P1/B1/C1/U1 code meanings are deliberately outside its scope. A DTC is a symptom clue, not a confirmed failed part. Do not copy SAE J2012 copyrighted text.
   Priority: P0 after validating current dataset snapshot and each row's source.

18. Real-vehicle OBD-II dataset — AbouAbdallah-Lounis/OBD-Dataset
   Source: https://github.com/AbouAbdallah-Lounis/OBD-Dataset
   Repository describes time-stamped OBD sensor samples from real vehicles, with vehicle identifiers and powertrain parameters; dataset claims CC BY 4.0.
   Vidya use: research for time-series vehicle condition classification and DTC prediction experiments.
   Limits: limited vehicle population, diesel-only according to repository notes, sparse DTC classes and possible leakage if splits are not grouped by vehicle. Inspect actual files, labels, paper and data quality before use.
   Priority: P1; attribution required.

19. LEVIN Vehicle Telematics Data by Yun Solutions
   Source: https://github.com/YunSolutions/levin-openData
   Repository describes approximately four months of OBD data from about 30 four-wheelers, 1 Hz OBD and 25 Hz accelerometer, with GPS and engine telemetry. Stated license: CC BY-NC-SA.
   Vidya use: non-commercial research on driving/vehicle telemetry, event detection and sensor fusion only if terms fit.
   Limits: non-commercial restriction; not a repair manual or broad vehicle fault dataset.
   Priority: HOLD for any commercial release; P1 for permitted non-commercial research.

20. KIT Automotive OBD-II Dataset (RADAR4KIT)
   Source: https://radar.kit.edu/radar/en/dataset/bCtGxdTklQlfQcAq
   Cited by a 2026 open-access connected-vehicle maintenance paper as an available automotive OBD-II dataset.
   Vidya use: potential vehicle telemetry benchmark.
   Limits: fetch dataset metadata and its exact license/access conditions before any download, training or redistribution.
   Priority: HOLD pending primary metadata and license check.

21. ApplianceDB public developer sample
   Source: https://github.com/ApplianceDB/ApplianceDB-public
   Repository describes 438 appliance error codes across 13 brands and 288 ranked repair procedures; ODbL 1.0 stated, with per-row source provenance.
   Vidya use: appliance code schema research, error-code ingestion prototype, provenance-aware retrieval.
   Limits: coverage described for US/UK markets; appliance brand/model/market combinations differ from India. ODbL share-alike and database attribution obligations need review; repair procedures are not automatically applicable to Indian variants. It is a developer sample, not a complete global database.
   Priority: P1 after ODbL compatibility review.

22. MIMII industrial machine sound dataset
   Discovery record: https://www.indatlas.com/datasets/mimii-dataset-sound-dataset-for-malfunctioning-industrial-machine-investigation/
   Original dataset access: https://zenodo.org/records/3384388
   Reported scope: normal/anomalous sounds for valves, pumps, fans and slide rails, multiple models and factory background noise. Reported license: CC BY-SA 4.0.
   Vidya use: acoustic anomaly-detection research and noise-robustness experiments for industrial equipment.
   Limits: laboratory/factory recordings and fixed microphone geometry do not establish accuracy on phone recordings, Indian field conditions or different machine models. Preserve attribution and share-alike obligations; verify Zenodo metadata before use.
   Priority: P0 for research after confirming the original record's metadata and files.

23. HB-Bearing real-background-noise audio dataset
   Discovery index: https://github.com/VictorBauler/awesome-bearing-dataset
   Reported 13,500 four-second, 16 kHz clips for normal and bearing fault conditions, with machinery and human background noise. DataCite metadata reportedly lists CC BY 4.0; IEEE DataPort subscription required for download.
   Vidya use: evaluate whether acoustic classifiers remain robust in noisy environments.
   Limits: access may require subscription; verify original DataCite record and rights before use.
   Priority: HOLD until download/access and license are confirmed.

24. Rotating-machine multi-sensor dataset (vibration, acoustic, temperature, current)
   Paper/data article: https://www.sciencedirect.com/science/article/pii/S2352340923001671
   Describes sensor recordings under normal operation, bearing inner/outer race faults, shaft misalignment and rotor unbalance across torque loads. Article states open access under a Creative Commons license.
   Vidya use: multi-sensor feature engineering and controlled condition-monitoring experiments.
   Limits: confirm the exact CC license, repository download and reuse permissions from the article's data availability statement. Lab rig conditions do not transfer directly to field machines.
   Priority: P1 pending license and raw data verification.

25. MPDD — Metal Parts Defect Detection Dataset
   Discovery record: https://www.indatlas.com/datasets/mpdd/
   Reports 1,000+ industrial metal-part images with pixel-level defect masks; license reported CC BY-NC-SA 4.0.
   Vidya use: non-commercial research into defect segmentation and visual inspection.
   Limits: commercial use is restricted; industrial metal surface defects are not equivalent to machine identification or mechanical fault diagnosis.
   Priority: HOLD for product training; P1 for non-commercial method study.

## Additional cautions from this pass
- ApplianceDB and MechanicDB-style repositories may contain curated repair text, but their database licenses, per-row source rights, regional applicability and share-alike terms must be checked before inclusion.
- Open code databases do not grant rights to reproduce copyrighted standards verbatim.
- Audio anomaly datasets detect distribution changes under their recording setup; they do not prove the cause, identify every fault, or justify a repair instruction.
- Public OBD datasets often contain few vehicles. Split by vehicle, not random rows, to reduce leakage and overestimated performance.
- A 2026 connected-vehicle paper cites UCI AI4I, KIT RADAR4KIT and carOBD as benchmarks; its reported results are the paper's results, not Vidya results. Its field cohort is small and must not be treated as broad validation.

## Research status after pass 2
This is a curated shortlist, not a complete census of worldwide public resources. Some entries are P0 only for research or prototype ingestion, not automatically approved for commercial model training. No external dataset has yet been downloaded, merged into the Android app, or used to train a Vidya model. Each candidate still needs a reproducible download/version, checksum, license copy, schema validation and data-quality report.


## Third research pass — Indian repair ecosystem, standards and pump diagnostics

26. Government of India — Right to Repair India portal
   Source: https://www.righttorepairindia.gov.in/
   Official initiative of the Department of Consumer Affairs. The portal lists participating brands and repair/product-support information; sectors include farming equipment, mobile/electronic devices, consumer durables and automobile equipment. Farming categories include tractor parts, harvesters and water-pump motors; consumer durables include refrigerators, washing machines and ACs.
   Vidya use: official discovery index for brand support, warranty/service information, repair options and authorized service networks. Prefer linking users to the official brand/portal page rather than copying pages into Vidya without permission.
   Limits: participating brands and portal content are not the whole market; a portal listing does not mean full service manuals or unrestricted bulk-download rights.
   Priority: P0 as a source-discovery directory; content ingestion requires page-specific rights review.

27. ICAR — All Publications / farm machinery books
   Source: https://icar.gov.in/en/all-publications
   Lists publications including "Farm Machinery Principles and Applications" and "Farm Machinery Maintenance and Management".
   Vidya use: discover Indian agricultural machinery education and maintenance references; identify publisher, author, edition and reuse terms before indexing.
   Limits: availability on a government website does not automatically mean public-domain or freely redistributable content.
   Priority: P1 as a discovery source; each book requires rights check.

28. Bureau of Indian Standards — Important Standards on Farm Machinery
   Source: https://www.bis.gov.in/wp-content/uploads/2021/09/Indian-Standards-on-AGRICULTURE.pdf
   Index includes standards such as IS 12207:2019 agricultural tractors, IS 13539:2018 power tillers, IS 15806:2018 combine harvesters, and IS 17045:2018 tractor-driven rotary tillers.
   Vidya use: build a standards catalogue and map machine types to relevant Indian standards for safe design/performance context.
   Limits: standards documents may be copyrighted/paid; an index is not permission to reproduce or distribute full standards. Do not infer repair steps from performance standards.
   Priority: P0 for metadata discovery; HOLD for full-text ingestion until licensed.

29. University of Twente / 4TU — NLN-EMP electric motor centrifugal pump dataset
   Source: https://research.utwente.nl/en/datasets/motor-current-and-vibration-monitoring-dataset-for-various-faults/
   Dataset contains experimental vibration, motor current and voltage from an electric motor-pump setup, with faults at multiple severity levels and operating speeds; versions are hosted by 4TU Research Data.
   Vidya use: pump/motor signal-processing research, condition classification and sensor-fusion baselines.
   Limits: this is controlled experimental data, not Indian field-pump validation. Confirm exact selected version's license and data files at 4TU before training or redistribution.
   Priority: P1 pending version-specific license verification.

30. Mendeley Data — Journal Bearing Fault Detection in a Vertical Motor-Pump Testbed (2026)
   Source: https://data.mendeley.com/datasets/x2hrn4vfrt/1
   Published 14 Aug 2026; reported CC BY 4.0. 6.37 GB synchronized sensor dataset with vibration, acoustic, temperature, oil pressure, shaft displacement, three-phase motor current and RPM; healthy and journal-bearing clearance-fault conditions.
   Vidya use: research for multi-sensor fusion and bearing fault detection.
   Limits: very large download; nuclear reactor coolant pump testbed context is highly specialized and not directly representative of domestic/agricultural pumps. Need downsampling, metadata review and machine-level splits.
   Priority: P1 for research; attribution required.

31. OpenDB — open vehicle-specific OBD signal database
   Source: https://github.com/obdb/
   Community-maintained vehicle make/model/year signal sets and diagnostic parameter definitions; text is generally licensed CC BY-SA 4.0 or other listed liberal licenses.
   Vidya use: vehicle-specific PID/signal mapping discovery and future data-driven scan-tool support.
   Limits: coverage differs by vehicle; each repository and file must be checked for its exact license. Do not assume generic OBD PID support for every make/model.
   Priority: P1; ingest only pinned vehicle repos with recorded licenses.

32. OpenDTC
   Source: https://opendtc.net/about
   Free generic and manufacturer DTC lookup site with contributed diagnostic data; the site itself notes some manufacturer entries may be raw, untranslated or incomplete.
   Vidya use: discovery leads for DTCs and source cross-checking only.
   Limits: no blanket open-data license was established in this pass; do not scrape, bulk ingest or train on its content without explicit permission. Manufacturer-specific rows require independent verification.
   Priority: HOLD; reference for discovery, not approved training data.

33. MechanicDB developer sample
   Source: https://mechanicdb.dataengineered.io/
   Advertises a 90-code developer sample under ODbL 1.0, with attribution/share-alike; larger commercial datasets are paid.
   Vidya use: evaluate the schema and compare against openly licensed DTC sources; do not purchase or integrate paid data without explicit approval.
   Limits: ODbL obligations apply; sample size is small; full dataset is not free.
   Priority: P1 for schema evaluation only after obtaining and checking the actual sample and license.

## Third-pass engineering decisions
- Prioritize the official Right to Repair India portal as a link-out/source-discovery catalogue, not as permission to mirror all its content.
- Build an Indian standards metadata index, not a copied standards library.
- Keep generic OBD-II data separate from make/model-specific signal definitions and OEM DTCs.
- Pump/motor datasets are for research and controlled evaluation; no classifier is ready for field diagnosis.
- No new external dataset has been added to the Android APK or used to train a model in this pass.


## Fourth research pass — visual inspection, India vehicle support and fault-signal datasets

34. VisA (Visual Anomaly dataset)
   Official discovery/source: https://registry.opendata.aws/visa/ and https://github.com/amazon-research/spot-diff
   10,821 industrial inspection images across 12 object categories with image-level labels and pixel-level masks; reported CC BY 4.0 at the official registry.
   Vidya use: visual anomaly localization research for manufactured parts and surface defects; possible pretraining/benchmarking after original registry license confirmation.
   Limits: not a machine-repair dataset; categories are not representative of every vehicle, farm machine or domestic appliance. Must keep source attribution and original split.
   Priority: P1, verify official registry terms before download.

35. KolektorSDD2
   Discovery: https://litebites.github.io/data/
   Surface-defect segmentation dataset; third-party catalogue reports 3,336 images and CC BY-SA 4.0.
   Vidya use: experiment with fine surface defect localization.
   Limits: catalogue is not the primary license authority; obtain original publisher/repository record and verify exact terms before any use.
   Priority: HOLD pending primary-source license confirmation.

36. NEU Surface Defect Database
   Discovery: https://github.com/IEEE-IES-Industrial-AI-Lab/industrial-ai-hub/blob/main/_pages/datasets.md
   1,800 grayscale hot-rolled steel images, six defect categories; accessible through public research repositories.
   Vidya use: steel surface defect research only.
   Limits: exact original dataset license and redistribution rights were not confirmed in this pass; industrial steel images do not teach general repair diagnosis.
   Priority: HOLD pending original license.

37. MVTec LOCO AD
   Official page: https://www.mvtec.com/research-teaching/datasets/mvtec-loco
   3,644 images across five categories with structural and logical anomalies; reported CC BY-NC-SA 4.0.
   Vidya use: benchmark logical/structural anomaly localization.
   Limits: non-commercial restriction; not approved for a commercial product without written permission.
   Priority: research-only/HOLD for commercial model training.

38. AeBAD — Aero-engine Blade Anomaly Detection
   Discovery: https://openruwei.org/en/resources/
   Real blade images/videos and annotations; reported CC BY 4.0 in the catalogue.
   Vidya use: specialized blade-surface anomaly research.
   Limits: verify original dataset record, specimen-vs-in-service defect context and all subset licenses before use.
   Priority: P2 pending primary-source review.

39. Universal Machinery Fault Diagnosis Dataset (UMFDD)
   Source: https://data.mendeley.com/datasets/3bz24t6tf4/1
   Published 2 April 2026. A standardized multi-signal repository combining public and experimental signals; categories include healthy, bearing faults, gearbox faults and induction-motor faults.
   Vidya use: investigate cross-dataset unit normalization and cross-domain evaluation.
   Limits: composite datasets inherit component-source constraints; exact license and provenance of every constituent dataset must be inspected before use. Do not treat the combined dataset as one unrestricted license.
   Priority: P1 for provenance audit; no training until component rights are cleared.

40. IoT-Integrated Predictive Maintenance Dataset
   Discovery: https://www.indatlas.com/datasets/iot-integrated-predictive-maintenance-dataset/
   Third-party catalogue describes simulated vibration, acoustic, temperature and current channels, CC0 1.0.
   Vidya use: software pipeline tests, synthetic signal handling and sensor-fusion prototyping only.
   Limits: simulated records are not field evidence and cannot validate real fault detection.
   Priority: P2; original Kaggle record/license must be confirmed before download.

41. Right to Repair India — registered automobile brands directory
   Official source: https://doca.gov.in/r2r-dbim/index.php/swise-companies/4
   Lists participating automobile support brands including Honda Motorcycle & Scooter India, Hero MotoCorp, TVS, Maruti Suzuki, Suzuki Motorcycle India, Hyundai, Volkswagen, Yamaha and others.
   Vidya use: official link-out directory for model/product support and customer-care routes.
   Limits: listing does not guarantee a freely downloadable workshop manual, diagnostic code table or right to redistribute brand documents.
   Priority: P0 as official discovery links.

42. Hero MotoCorp — service and maintenance
   Official source: https://www.heromotocorp.com/en-in/services/service-and-maintenance.html
   Official service, maintenance and warranty information.
   Vidya use: link users to current official maintenance and authorized-service information; use model-specific owner manuals where published.
   Limits: service policy page is not a full workshop manual; do not derive repair procedures from warranty statements.
   Priority: P1 as official support source.

## Fourth-pass decisions
- VisA may be a stronger candidate for general industrial visual anomaly research than non-commercial MVTec/MPDD, but it is still not a general repair dataset.
- MVTec AD, LOCO AD and MPDD remain blocked for commercial training unless the rightsholder grants permission.
- UMFDD is a composite signal dataset; audit each constituent source before use.
- Simulated predictive-maintenance data is suitable for pipeline testing, not field-performance claims.
- Official Indian brand portals are link-out sources. Do not scrape or redistribute proprietary manuals without permission.
- No new dataset has been downloaded, bundled into the APK, or used to train a Vidya model in this pass.
