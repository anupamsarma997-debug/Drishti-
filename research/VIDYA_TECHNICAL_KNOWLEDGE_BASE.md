# Vidya Technical Knowledge Base — Data Contract & Build Plan

Status: schema and admission rules; not a claim of complete data coverage.
Scope: motorcycles, cars, trucks, tractors, farm machinery, pumps, motors, generators, home appliances, workshop tools and industrial machines.

## Six linked knowledge collections

1. **Machine identity / model catalogue** — domain, manufacturer, model, year, variant, engine or powertrain, market, serial range, nameplate aliases, identity source and confidence. Never merge variants merely because model names look similar.
2. **Parts catalogue** — stable part ID, OEM and alternate names, function, location description, compatible machine identities, symptoms, possible causes, safety hazards, source and verification status. Generic part descriptions must not be treated as exact fitment.
3. **Diagnostic codes** — code, protocol/system, exact meaning in original words, generic/OEM scope, supported makes/models/years/variants, possible causes, required tests, source/revision and verification status. Unknown/OEM-specific codes must remain unresolved until an authoritative match is found.
4. **Manual and technical-document index** — title, publisher, official URL, revision/date, language, exact applicability, document type, page/section, licence, redistribution rights and verification status. Store links and page citations unless redistribution rights explicitly permit local copies.
5. **Visual/audio/sensor dataset** — asset hash, modality, machine identity, part and defect labels, capture session, physical asset ID, annotator, annotation schema, source, licence, consent, commercial-training and redistribution permissions. Keep splits grouped by physical asset and session to prevent leakage.
6. **Troubleshooting graph** — symptom → observations → possible causes → safe external checks → exact source-backed procedure → stop conditions → technician escalation. Preserve uncertainty; symptoms are not confirmed faults.

## Data status vocabulary

- `UNREVIEWED`: candidate only; never used to justify a repair instruction.
- `SOURCE_CHECKED`: provenance and source have been inspected; may support carefully qualified reference/triage, not a model-specific repair procedure by itself.
- `EXPERT_VERIFIED`: exact applicability and procedure reviewed by a qualified domain expert against the cited source; eligible for the repair-procedure gate.
- `REJECTED`: invalid, unsafe, duplicate, irrelevant or rights-incompatible.

The Android domain model is in `app/src/main/java/com/vidya/safetyassistant/knowledge/TechnicalKnowledgeModels.kt`. Its procedure gate requires exact manufacturer + model + year + variant, at least one traceable HTTPS source, and expert verification of both record and sources.

## Initial source candidates — scope and restrictions

| Collection | Candidate | What it can contribute | Restrictions / limitation | Initial disposition |
|---|---|---|---|---|
| Repair history | Open Repair Alliance ORDS v0.3 — https://openrepair.org/open-data/downloads/ | Product category, brand/year, problem and repair outcome taxonomy | CC BY-SA 4.0; community repair logs, not verified repair instructions; download/version and attribution must be handled | Candidate for analysis/import after download audit |
| Generic vehicle identity | NHTSA vPIC — https://vpic.nhtsa.dot.gov/api/ | Manufacturer-submitted VIN decoding and vehicle descriptors | US-market reporting system; coverage cannot be assumed for Indian-market two-wheelers/tractors; API rate control | Reference only; market coverage gate |
| Generic OBD-II | OBDex — https://github.com/foerbsnavi/obdex | Generic DTC and PID data | Data CC0-1.0, tooling MIT according to project; generic only; independently validate each row and source | Candidate for schema-validated import |
| Industrial audio | MIMII DG — https://zenodo.org/records/6529888 | Normal/abnormal audio for fans, gearboxes, bearings, slide rails and valves | CC BY-NC-SA 4.0; 4.4 GB; no commercial training/use without permission; laboratory domain shift | Research-only / HOLD for commercial product |
| Indian agricultural machine curriculum | DGT Mechanic Agricultural Machinery CTS 2.0 — https://www.dgt.gov.in/sites/default/files/2023-12/Mech.%20Agricultural%20Machinery_CTS2.0_NSQF-4.pdf | Training topics and machine-category vocabulary | Curriculum, not OEM service manual; check reuse terms | Link-out / taxonomy reference |
| Indian farm machinery catalogue | ICAR Agricultural Engineering catalogue — https://icar.org.in/sites/default/files/inline-files/AgriculturalEngineering_1.pdf | Indian machine and implement category discovery | Catalogue, not repair procedure; no blanket permission to reproduce | Link-out / taxonomy reference |
| Industrial dataset discovery | Awesome Industrial Datasets — https://github.com/jonathanwvd/awesome-industrial-datasets | Candidate discovery across image, audio, vibration and time series | Index only; each dataset needs its own licence audit | Discovery index only |

## Machine-family coverage matrix

For every family below, fill identity → parts → codes → manuals → media → troubleshooting, with a separate coverage count for each layer:

- Road: motorcycles, scooters, cars, EVs, trucks, buses, trailers.
- Agriculture: tractors, power tillers, rotavators, seed drills, planters, reapers, harvesters, threshers, chaff cutters, shellers, sprayers, transplanters, winnowers, dryers, balers, mulchers, dairy equipment.
- Water and power: centrifugal/submersible pumps, irrigation systems, diesel/petrol engines, generators, electric motors, compressors.
- Home: refrigerators, washing machines, air conditioners, fans, mixers, water purifiers and other appliances.
- Workshop: drills, grinders, welders, compressors, lathes and cutting tools.
- Industrial: conveyors, gearboxes, bearings, valves, hydraulic/pneumatic systems, CNC machines, forklifts, excavators and process equipment.

## Ingestion pipeline (required for every new source)

1. Register source URL, publisher, version/date, licence, commercial use, redistribution and training permissions.
2. Download only after terms permit; calculate SHA-256; retain original untouched.
3. Parse to a staging area; validate schema, units, duplicates, language and encoding.
4. Attach each row to exact machine/market/variant scope; unknown applicability stays unknown.
5. Preserve original citation and page/section for every fact; separate verbatim text from independently authored summaries.
6. Review safety-critical data with a qualified expert; record reviewer and date.
7. Run unit, contradiction, stale-version and regression tests.
8. Export an offline bundle only for approved records; support versioning and revocation.
9. Publish a coverage report that distinguishes candidates, downloaded, parsed, verified and shipped counts.

## Visual dataset policy

A dataset of industrial defects is not automatically suitable for vehicle/farm-machine parts. Keep separate labels for object class, component, visible condition, fault hypothesis and confirmed fault. A photograph alone rarely proves internal failure. Require capture consent, machine identity, context, annotation guide, independent label review and physical-machine/session split. Do not use non-commercial datasets to train a commercial app without permission.

## Release and safety gates

- Generic OCR/object boxes never count as exact model or part recognition.
- No torque, wiring, pressure, brake, fuel-injection, refrigerant, gas, chemical or high-voltage procedure without exact authoritative documentation and qualified review.
- No instructions to defeat guards/interlocks or work on moving/live machinery.
- If identity, source or applicability is incomplete, show only safe observations and stop/technician guidance.
- No fabricated dataset labels, synthetic performance claims or unverified accuracy statements.


## Official Indian two-wheeler manual portals — source index

These are discovery links to manufacturers' own portals. The app should deep-link users to the official page or accept a user-provided manual; do not mirror or redistribute documents unless the relevant licence explicitly permits it.

| Manufacturer | Official source | What it offers | Integration boundary |
|---|---|---|---|
| Hero MotoCorp | https://www.heromotocorp.com/en-in/services/owner-manual.html | Owner manuals selected by vehicle; separate maintenance schedule pages | Owner information; not automatically workshop service procedures |
| Honda 2Wheelers India | https://www.honda2wheelersindia.com/resources/owners-manual | Segment/model/year/month/manual-type and VIN/frame-number search | Exact document selection and site terms apply |
| Honda MotoPub India | https://www.hondamotopub.com/HMSI | Honda service publications by model and year; licence agreement before download | Read and comply with the download licence; no redistribution assumed |
| TVS Motor | https://www.tvsmotor.com/our-service/user-manual | Vehicle user manuals with model and manufacturing-date distinctions | Date/variant matters; portal distinguishes OBD II and OBD IIB production periods |
| Bajaj Auto | https://www.bajajauto.com/customer-service/owners-manual | Owner manuals for listed models | Owner documentation, not automatically a workshop service manual |

**Coverage warning:** these portals do not constitute a complete make/model/year/variant database. A portal entry is not a parsed manual, verified parts catalogue, or approved repair instruction. Store selected document title, exact applicability, revision and page citation only after the individual document is inspected.
