# Vidya Research Landscape: Open Papers, Reusable Projects, and Research Gap

**Prepared:** 7 November 2026  
**Scope:** Camera-assisted, manual-grounded, safety-constrained technical guidance for household, vehicle, workshop, industrial, and rural/agricultural machinery on Android.

## Executive conclusion

The broad idea of an AI maintenance assistant is **not new**. Research already exists on multimodal manual RAG, knowledge-graph fault diagnosis, agricultural fault knowledge extraction, offline document QA, and agricultural machinery sensing. Vidya should not claim to be the first camera-based maintenance chatbot.

A more defensible contribution is the integration and evaluation of:

1. Android-first operation on modest devices and unreliable networks;
2. camera/nameplate OCR to identify a machine/model as a *candidate*, not as verified identity;
3. retrieval from the user's exact manufacturer manual, with page-level citations;
4. a conservative safety gate that can refuse to give repair steps when evidence is weak or the task is hazardous;
5. multilingual, voice-friendly guidance designed with rural users;
6. a reproducible evaluation set covering retrieval accuracy, model mismatch, unsafe-answer refusal, OCR noise, and low-connectivity usability.

This is a **research direction**, not a proven novelty claim. A systematic literature review and field evaluation are required before claiming novelty or effectiveness.

## 1. Papers worth reading

| Paper | What it contributes | What Vidya can learn | Important limitation |
|---|---|---|---|
| Yan & Gong (2026), [Research on Agricultural Machinery Fault Nested Entity Extraction for Low-Resource and High-Noise Scenes](https://doi.org/10.32604/cmc.2026.080178) | Extracts nested component/fault entities from noisy agricultural manuals and expert logs; reports F1 95.87% and a 19,710-entity fault knowledge graph. CC BY 4.0. | OCR-noise-aware extraction; structured entities such as machine → subsystem → part → symptom → fault; later build a verified agricultural fault knowledge base. | Results belong to that paper's dataset/task. Do not transfer its F1 score to Vidya or assume the same model works on Assamese/Hindi manuals. |
| Lu & Lee (2025), [A Multimodal RAG-based Maintenance Chatbot for Robotic Arm Manuals](https://www.iroboticsjournal.org/index.php/irobotics/article/view/192) | Separate text and visual manual pipelines; caption retrieval and image+text retrieval. CC BY 4.0. | Important for diagrams, wiring schematics, exploded views, and figure-aware manual retrieval. | Robotic-arm scope; evaluation used an expert-curated dataset and larger/cloud models in some conditions. Not proof of offline low-end Android performance. |
| Wu et al. (2025), [An automatic machine fault identification method using the knowledge graph–embedded large language model](https://doi.org/10.1007/s00170-025-15555-2) | CNC fault knowledge graph combined with LLM reasoning; reports 97.50% in its industrial evaluation. | Structured fault relationships and explicit reasoning constraints can complement free-text retrieval. | CNC domain and study-specific evaluation. The paper itself discusses knowledge coverage and hallucination as challenges. |
| Coggins et al. (2022), [How have smallholder farmers used digital extension tools?](https://doi.org/10.1016/j.gfs.2021.100577) | Interviews with 40 developers and 101 digital-tool users in Bihar; identifies 15 adoption barriers including connectivity, literacy, language, trust, and relevance. CC BY. | Strong basis for low-literacy UX, local language, voice, offline-first design, user co-design, and trust. | This is a digital-extension adoption study, not a machine-diagnosis accuracy study. |
| [ISOBlue HD: An Open-Source Platform for Collecting Context-Rich Agricultural Machinery Datasets](https://pmc.ncbi.nlm.nih.gov/articles/PMC7600794/) (2020) | Open-source sensor platform for CAN, GPS, and video context on a combine harvester. | Shows how machine context and operator actions can be collected for future research-grade datasets. | Hardware/data-collection platform, not a phone app or repair assistant. Its combine-harvester deployment is not a ready-made dataset of labelled failures. |

### Reading order

1. Lu & Lee (manual diagrams + multimodal retrieval).
2. Yan & Gong (agricultural manual OCR + fault entities).
3. Wu et al. (fault knowledge graph + reasoning).
4. Coggins et al. (rural usability and deployment).
5. ISOBlue HD (future sensor/context research).

## 2. Open-source repositories to study

These are candidate references, not code already integrated into Vidya.

| Project | Useful part | License / practical caution | Decision |
|---|---|---|---|
| [PocketSage](https://github.com/umerdilpazir/pocketsage) | Native Android Kotlin/Compose, local PDF parsing, local embeddings, Room retrieval, LiteRT-LM generation, and source snippets. | Repository README states MIT. Its Gemma 4 E2B model is about 2.58 GB and README recommends Android 12+ and 4 GB+ RAM; model weights have separate Gemma terms. | **Best architecture reference** for a future optional on-device RAG mode. Do not copy its full app or require a multi-GB model for Vidya's basic mode. |
| [O-RAG / OfflineRAG](https://github.com/yerramsettysuchita/OfflineRAG) | Hybrid BM25 + TF-IDF + semantic retrieval and offline Android packaging experiments. | Repository page reviewed did not clearly expose a software license. Model binaries and dependencies also have separate terms. | Study retrieval concepts only; do not copy code until a clear license is confirmed. |
| [Local Multimodal RAG for Technical PDFs](https://github.com/krishalivaibhav/local-multimodal-rag) | Separate extraction paths for prose, tables, and figures; page/bounding-box citations; retrieval evaluation. | Python/on-premise pipeline, not a ready Android library. Confirm repository license and each model/dependency before reuse. | Use as a **design reference** for a later manual-ingestion service and evaluation approach. |
| [ISOBlue](https://isoblue.org/) | Open-source agricultural machinery CAN/GPS/sensor collection platform. | Requires compatible hardware and machine interfaces; not a plug-in feature for an ordinary phone. Check individual component licenses. | Future research only; not part of the current no-hardware MVP. |
| [Android Document QA](https://github.com/sowjanyakch/Android-Document-QA) | Android PDF/DOCX document QA and a simple RAG pipeline. | README describes remote LLM generation in its comparison; verify license and current maintenance before reuse. | Useful for comparing lightweight Android document retrieval patterns, not a complete offline solution. |

### Reuse rules

- A public GitHub repository is not automatically open-source. Check for an explicit LICENSE file.
- Open-access paper does not mean public-domain. CC BY permits reuse with attribution and license compliance; other Creative Commons variants may impose different restrictions.
- Model weights, OCR models, datasets, and Android libraries each have their own license/terms. A repository's MIT license does not automatically cover bundled models.
- Prefer learning architecture and writing Vidya-specific code over copying whole projects.

## 3. What Vidya already has vs. what is still missing

Based on the current Vidya repository design and feature notes:

**Already represented in the codebase**
- CameraX and generic on-device object detection.
- Live OCR and nameplate clue parsing.
- Importing local PDF manuals, OCR text indexing, and exact model-string lookup.
- Manual search results that show page/snippet references.
- Deterministic household, vehicle, and rural-machine safety triage.
- Generic OBD-II code lookup and dashboard warning-light guidance.
- Official vehicle/farm-manual source directories.
- Part-candidate reference catalogue and a deterministic specialist expert system.
- Basic AR surface marker placement.

**Not yet established / should not be claimed**
- Verified machine identity from camera alone.
- Comprehensive brand/model/year-specific error-code coverage.
- Semantic RAG over all manual text, tables, and diagrams.
- A trained multimodal model that recognises machine faults from video.
- Reliable multilingual generation for Assamese and other Indian languages.
- Physical intervention, automatic power isolation, or guaranteed prevention of accidents.
- Hardware-validated accuracy, latency, battery use, or safety performance.

## 4. Recommended research contribution

Suggested working title:

**“An Offline, Manual-Grounded and Safety-Constrained Multimodal Assistant for Rural Machinery on Low-Cost Android Devices”**

A defensible contribution is not simply adding a chatbot. It is a measurable pipeline:

1. **Candidate identification:** OCR the nameplate and extract brand/model/serial clues. Display confidence and ask the user to confirm. Never treat OCR as proof of identity.
2. **Manual selection:** require the exact model/variant/year where available; allow users to import a manufacturer manual.
3. **Evidence retrieval:** retrieve text and relevant diagram/table pages; every answer must show manual title, page number, and quoted/retrieved evidence.
4. **Safety gate:** map the task to hazard classes. For live electricity, high-voltage capacitors, gas, refrigerants, rotating blades/PTO, hydraulic pressure, EV traction systems, and chemical exposure, provide stop-work guidance and professional escalation rather than repair steps.
5. **Answer policy:** if the exact manual is missing, the model mismatch is unresolved, or retrieval evidence is weak, say “I cannot verify this from the available manual” and abstain.
6. **Rural UX:** simple language, voice input/output, pictorial steps, offline access, and a clear way to contact a mechanic.

## 5. Evaluation plan before making performance claims

Build a small, permission-cleared test corpus from manuals for a limited set of machines first (for example, one pump, one tractor, one power tiller, one thresher, and one household appliance). Do not scrape or redistribute manuals without checking copyright and website terms.

| Evaluation | What to measure |
|---|---|
| Model/nameplate OCR | Exact-match accuracy for manufacturer/model fields; character error rate under blur, glare, and low light |
| Manual retrieval | Recall@5, MRR, and percentage of answers citing the correct page |
| Evidence grounding | Human review: does every factual instruction appear in the cited manual page? |
| Abstention and safety | Unsafe-answer rate, correct stop-work/refusal rate, and false reassurance rate on hazardous cases |
| Model mismatch | Whether the system detects a wrong year/variant/manual rather than returning a plausible but irrelevant answer |
| Rural usability | Task completion, time-to-find instruction, comprehension, language preference, and user trust |
| Device performance | APK size, model download/storage, peak RAM, battery, and response latency on named Android devices |

Use a held-out test set. Report denominators and confidence intervals. Do not use synthetic examples as real-world accuracy results. Do not describe the 95.87% or 97.50% figures from cited papers as Vidya results.

## 6. Immediate implementation priority

**Do not add a large LLM to the default app yet.** A 2.58 GB model and around 1.5–1.7 GB active memory footprint on a flagship reference device are not a safe assumption for low-cost Android phones. Google's LiteRT-LM supports Android/Kotlin and multiple runtimes, but published speed/memory figures are device-specific.

Recommended order:

1. Fix and stabilise the existing build and tests.
2. Improve the current manual index with better chunk metadata, exact model/year filtering, and page-grounded search.
3. Add a deterministic evidence/safety gate before any generated answer.
4. Add a separate optional AI mode only after measuring supported device RAM/storage and evaluating a small model.
5. Add diagram retrieval and multilingual voice in separate tested increments.

## References and project links

- Yan & Gong (2026): https://doi.org/10.32604/cmc.2026.080178
- Lu & Lee (2025): https://www.iroboticsjournal.org/index.php/irobotics/article/view/192
- Wu et al. (2025): https://doi.org/10.1007/s00170-025-15555-2
- Coggins et al. (2022): https://doi.org/10.1016/j.gfs.2021.100577
- ISOBlue HD (2020): https://pmc.ncbi.nlm.nih.gov/articles/PMC7600794/
- PocketSage: https://github.com/umerdilpazir/pocketsage
- O-RAG: https://github.com/yerramsettysuchita/OfflineRAG
- Local Multimodal RAG: https://github.com/krishalivaibhav/local-multimodal-rag
- Google LiteRT-LM documentation: https://developers.google.com/edge/litert-lm/overview
