# Vidya — Drishti AI

**See. Understand. Prevent.**

Vidya is an Android-first, offline-oriented technical inspection assistant.

## Implemented MVP foundation

- Kotlin + Jetpack Compose Android app.
- CameraX live preview with a 640×480 analysis stream and keep-latest backpressure.
- Bundled ML Kit on-device generic object detection; no image upload or API key is used for this detector.
- Throttled live camera OCR using bundled ML Kit Latin and Devanagari recognizers (roughly one OCR pass per 1.2 seconds while the camera analyzer is active); no image upload is used.
- Conservative nameplate field extraction for explicitly labelled manufacturer, model, voltage, current, frequency, and power text. These are OCR-derived clues only, not verified equipment identity or electrical measurements.
- Live bounding boxes and generic object labels drawn over the camera preview.
- Safety overlay states: SAFE, CAUTION, and flashing STOP.
- Manual SAFE/STOP controls for UI and policy testing.
- Deterministic six-dimension safety policy engine (Time, ROI, Content, Action, Logic, Predictive Safety).
- Stop event triggers local vibration and a Hindi TTS warning.
- Android speech recognition adapter with on-device recognition preference where available.
- UI language selector for Assamese, Hindi, English, Bengali, Tamil, Telugu, Marathi, Gujarati, Kannada, Malayalam, Punjabi, and Urdu (actual voice availability depends on installed device language packs).
- Small deterministic offline safety FAQ for common electrical and moving-machinery questions (English and Hindi response text only).
- Local PDF manual library using Android's document picker; stores document URI permission and page-count metadata, and opens the selected PDF in an installed viewer.
- On-device OCR indexing for the first 40 pages of imported PDFs, using bundled ML Kit Latin and Devanagari recognizers; extracted text is stored privately on-device and searchable by keywords with manual name and page number. The two bundled script models add roughly 8 MB per device architecture, before APK packaging effects.
- Exact model-text search across indexed manual pages, ranked with a separate manufacturer-text clue. A match means only that the model string appears in OCR text; it does not authenticate the PDF or confirm the physical appliance.
- Home troubleshooting selector for AC, TV, washing machine, electric motor, water pump, refrigerator, generator, microwave, water heater/geyser and power tools.
- Rural/farm machinery selector covering tractor, power tiller, rotavator, irrigation pump, diesel/petrol engine, sprayer, paddy/multi-crop thresher, combine harvester, reaper, chaff cutter, rice mill, maize sheller/groundnut decorticator, brush cutter, power weeder, paddy transplanter/seed drill, solar pump controller, farm generator, dairy milking/milk chilling equipment, winnower/grain grader, grain dryer, feed grinder/hammer mill, potato/ginger/turmeric digger, areca-nut/orchard harvester, tea-leaf plucker, plough/harrow/cultivator/land leveller, seed drill and planters, fodder harvester, mulcher and straw baler. It captures machine brand/model/year/fault-code clues, offers conservative observation-only checks, and searches locally imported farm manuals by the entered text and can share a text service report with a mechanic.
- Part-level rural-machine triage for 28 machine groups: likely component areas, symptom clues, possible causes, safe external observations, what a qualified technician should inspect, and explicit do-not-do boundaries. Users can describe sounds, smoke, leaks, vibration or timing; deterministic keyword matching suggests candidate parts, not a confirmed diagnosis. It does not instruct unverified dismantling or internal repair.
- Expanded component-level candidate-fault catalogue for household appliances (AC, TV, washing machine, refrigerator, microwave, water heater), electric motors and power tools, plus bike, scooter, car, EV and truck. Home-appliance and vehicle troubleshooting screens display likely components, symptom clues, possible causes, safe observations, technician inspection pointers and explicit do-not-do warnings. These are general triage candidates, not confirmed diagnosis or exact model part-fitment.
- Official North-East agricultural machinery references: NERFMTTI Assam, KIRAN farm tools and agricultural engineering guidance, SMAM guidelines and BIS farm machinery standards.
- Vehicle identity + error-code screen for bikes, scooters, cars, EVs and trucks: asks for brand, exact model, year and DTC; a small starter dictionary explains selected generic OBD-II codes with possible causes, safe next steps and explicit compatibility caveats. Unknown and manufacturer-specific codes are not guessed. It provides conservative symptom-based observation checks and stop/escalation warnings; it does not diagnose internal faults or provide component-level repair steps.
- Optional ARCore mode using SceneView 2.3.0: tap a tracked horizontal plane to place a neutral 3D marker; normal CameraX mode remains available on devices without ARCore.
- Unit tests for safety-engine policy and offline FAQ behavior.
- GitHub Actions workflow to run unit tests and assemble a debug APK.

## Critical limits — read before use

This is an early engineering MVP, **not a validated safety product**.

- ML Kit's generic object detector recognizes broad object categories. It does **not** identify incorrect wire connections, energized conductors, high-voltage proximity, unsafe tool angles, hidden defects, internal machine faults, or accident probability.
- Detected object boxes are visual annotations only. They are not hazard boxes.
- Live OCR may misread digits, decimal points, units, and labels. Nameplate parsing only extracts nearby labelled text; users must compare every value with the physical plate or official manufacturer documentation.
- Real hazard classifications currently enter the SafetyEngine only through the manual test controls or a future trusted classifier adapter.
- The green test state is simulated and must never be interpreted as a real inspection result or proof of safety.
- Voice input currently uses Android's installed speech service; offline language availability depends on device language packs. The offline FAQ is rule-based, not a generative AI assistant. The language selector changes speech locale, but answer translation is currently limited to Hindi and English.
- Live camera OCR is throttled to about one pass every 1.2 seconds and may miss small, blurred, angled, or poorly lit text. It does not identify equipment or detect faults.
- PDF OCR indexing is limited to the first 40 pages and may misread scans, low-resolution text, tables, symbols, units, or diagrams. Exact model-text search can find candidate pages, including common punctuation variations, but search results are source snippets rather than verified repair instructions. Manufacturer authenticity verification and generated manual-grounded step-by-step repair guidance are not implemented yet.
- The app can stop its own guidance workflow and alert the user. It cannot physically stop a machine, vehicle, or electrical circuit.
- AR mode currently performs plane tracking and neutral marker placement only. It does not identify equipment, localize components, show manual-grounded arrows, or detect hazards.
- Official manufacturer manual source directory for selected India-market two-wheeler and car brands, plus the Government of India Right to Repair portal. Vidya opens the official source page; it does not mirror or redistribute their manuals.
- Dashboard warning-light guide for common oil-pressure, coolant-temperature, brake, charging, check-engine, ABS, SRS/airbag, tyre-pressure, EV battery and low-fuel indicators, with model-specific caveats and safe response advice.
- Vehicle DTC coverage is intentionally incomplete. Only a small set of common generic OBD-II codes is included; manufacturer-specific codes, motorcycle blink codes, many EV codes and model/year-specific definitions are not decoded. Vehicle brand/model/year fields are captured for context but are not yet checked against a manufacturer database.
- Farm-machine catalogue coverage is a practical starter list, not every make/model or all village equipment. Farm fault-code decoding is not implemented yet; entered brand/model/year/code text is only searched in PDFs the user imports. Government reference portals provide categories/specifications, not universal repair manuals. The official Assam NERFMTTI inventory and Government of India farm mechanization portal are linked in-app.
- There is no online official-manual discovery, semantic manual retrieval, manufacturer authenticity verification, verified repair-step generation, persistent inspection history, or validated multilingual translation yet.
- A camera cannot establish whether a wire is energized or detect many hidden/internal faults.

## Build

Requirements: JDK 17, Android SDK 35, Gradle 8.10.2.

```bash
gradle testDebugUnitTest assembleDebug
```

GitHub Actions runs unit tests and builds a debug APK on pushes and pull requests, then uploads it as the `vidya-debug-apk` workflow artifact. The local APK path after a successful build is `app/build/outputs/apk/debug/app-debug.apk`.

## Safety principles

- Never touch live wiring or open an energized electrical panel.
- Keep away from smoke, fire, exposed conductors, moving machinery, fuel leaks, and damaged batteries.
- Never bypass machine guards or safety interlocks.
- Follow the manufacturer's verified manual and use qualified professionals for hazardous work.
- Do not treat AI output or a camera view as a substitute for professional inspection.

## AR research

Open-source Android AR repository comparison, licensing notes, reusable architecture patterns and Vidya integration roadmap: [docs/AR_RESEARCH_AND_INTEGRATION.md](docs/AR_RESEARCH_AND_INTEGRATION.md).

## Project structure

- `app/src/main`: Android application, CameraX preview/analyzer, optional ARCore/SceneView plane marker, safety engine/controller, Compose overlay, voice adapter, offline FAQ, local PDF library, live OCR, nameplate text parser and on-device OCR page index.
- `app/src/test`: safety-engine, offline-FAQ and equipment-nameplate parser unit tests.
- `docs/`: product requirements, architecture, and safety policy.
- `AGENTS.md`: engineering and safety instructions for coding agents.

## Third-party notices

The optional AR mode depends on SceneView 2.3.0, distributed under Apache License 2.0. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and [third_party/SceneView-LICENSE.txt](third_party/SceneView-LICENSE.txt).

## License

No project-wide license has been selected yet. Do not assume the repository is open-source licensed until a LICENSE file is added.


## Offline specialist machine experts (v1)

Vidya now includes a deterministic specialist router for motorcycle/scooter/car/EV/truck, farm machinery, home appliances, motors and pumps, workshop tools, and selected industrial equipment. The user selects a machine category, enters brand/model/year and describes the symptom. The local rule-based expert returns possible inspection areas, safe external observations, stop-use conditions and escalation guidance.

This is an offline knowledge-based first version, **not a trained generative AI model**, not a confirmed fault diagnosis, and not an equipment safety certification. Model-specific repair procedures must come from the exact verified manufacturer manual. High-voltage, electrical, gas, refrigerant, pressure, hydraulic, braking and moving-part work remains technician-only.


## Offline manual retrieval and research

Imported PDF manuals are OCR-indexed locally. Search now uses a lightweight lexical ranker that rewards complete query coverage, exact phrases, repeated terms and model/fault-code text. Results retain the manual name and page number so users can inspect the source page. This is **not semantic retrieval**, does not verify that a manual matches the exact machine variant, and must not be treated as proof that a suggested action is safe.

Research papers, candidate open-source references, licensing cautions, research gap and an evaluation plan are documented in [docs/RESEARCH_LANDSCAPE_VIDYA.md](docs/RESEARCH_LANDSCAPE_VIDYA.md).
