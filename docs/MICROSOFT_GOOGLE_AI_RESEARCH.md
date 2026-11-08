# Microsoft + Google AI Research for Vidya

Research snapshot: 8 November 2026  
Project: Vidya / Drishti AI (Android, Kotlin, Jetpack Compose)  
Decision rule: prefer offline-first, low-cost, source-grounded components. Do not add cloud dependencies until a real account, endpoint, budget limit, privacy policy and test plan exist.

## Executive decision

**Adopt now (already present or low-risk design patterns):**
1. Keep Google ML Kit OCR and CameraX on-device for the first pass. It is free to use as an SDK, works offline, and is suitable for reading nameplates and manual pages.
2. Keep a local manual index as the offline fallback. Preserve manual name, page number and the exact retrieved passage.
3. Use a custom LiteRT/TensorFlow Lite object-detection model for future machine/part bounding boxes. ML Kit's built-in coarse classifier is not a machine classifier.
4. Use the Google Gemini File Search approach as the first candidate for a future manual-grounded RAG service, but call it from a trusted backend—not with a secret API key embedded in the APK.
5. Require source citations, make/model/year matching, uncertainty and technician escalation in every generated answer.

**Keep optional (do not enable or incur charges now):**
- Microsoft Document Intelligence for high-quality extraction from scanned manuals, tables, diagrams and layout.
- Azure AI Search for a large, centrally managed manual/document library with hybrid keyword + vector retrieval.
- Microsoft Foundry Agent Service for managed agent orchestration, web search, file search, function tools and operational tracing.
- Google Firebase AI Logic for mobile-safe access to Gemini models with App Check and per-user limits. It is not a drop-in replacement for the Gemini File Search ingestion API; use a backend for File Search operations.

No Azure or Google cloud resource, paid service, API key, billing account or model deployment was created as part of this research.

## 1. Google technologies

### A. ML Kit Vision — keep as the offline camera foundation

What it does:
- On-device text recognition, including the Latin and Devanagari recognizers already used in Vidya.
- On-device object detection/tracking and bounding boxes.
- Custom image-classification models can be integrated with ML Kit.

Important limitation:
- The built-in object classifier only returns broad classes such as fashion goods, food, home goods, places and plants. It cannot reliably say "Honda Activa", "Mahindra tractor", "AC compressor" or "broken brake hose".
- A custom *classifier* can identify a crop/whole object class, but precise part localization requires an object-detection model trained with bounding boxes (for example, a compatible LiteRT/TFLite detector), not just the default ML Kit detector.
- A detector's bounding box only says where a model believes an object is; it does not establish that the part is faulty or dangerous.

Vidya decision:
- Retain CameraX + ML Kit OCR for label clues and local PDF OCR.
- Do not present generic ML Kit labels as machine identification.
- Future custom detector should return class, bounding box, confidence and model version; require user confirmation before routing to an expert.
- Do not download or bundle an untrained/unlicensed third-party model.

Official docs:
- https://developers.google.com/ml-kit/vision/object-detection/android
- https://developers.google.com/ml-kit/vision/object-detection/custom-models/android
- https://developers.google.com/ml-kit/guides

### B. Gemini File Search — best first cloud RAG candidate for manuals

What it offers:
- Imports, chunks and indexes documents for semantic retrieval.
- Supports multimodal retrieval for text and images; supports PDF manuals.
- Custom metadata can carry source URL, manufacturer, model, year, market/region, page and document version.
- File citation annotations can return custom metadata and page-level citations.
- Storage and query-time embeddings are free; indexing embeddings and normal model input/output tokens can be charged. The published File Search page lists embedding creation at the embedding model's rate (currently shown as USD $0.15 per million tokens for gemini-embedding-001); confirm live pricing before use.
- Current documented limits include 100 MB per document and a free-tier total store limit of 1 GB. Check current quota before uploading a large manual collection.
- Uploaded file content is documented as being deleted after 48 hours; the embeddings persist until deleted or the model is deprecated. This matters for privacy and manual lifecycle.

Vidya decision:
- Strong candidate for a small pilot using a few public/OEM manuals and synthetic test questions—not a whole internet crawl.
- Put File Search API calls and secret credentials behind a server-side endpoint (Firebase Cloud Functions or Cloud Run). Never put a Gemini API key in Android source, BuildConfig, resources or the APK.
- Store metadata for OEM, exact model, year, market, manual revision, page and official source URL.
- Ask retrieval first; generate only from returned passages; display citations/pages; if no relevant source is found, say so and ask for the exact model/manual.
- Google Search grounding and File Search cannot be combined as tools in the same request according to the current File Search documentation. Use two explicit stages: search for an official manual, then index/retrieve that manual, rather than pretending both happened in one call.

Official docs:
- https://ai.google.dev/gemini-api/docs/file-search
- https://ai.google.dev/gemini-api/docs/pricing
- https://blog.google/innovation-and-ai/technology/developers-tools/expanded-gemini-api-file-search-multimodal-rag/

### C. Firebase AI Logic — secure Android access to Gemini

What it offers:
- Kotlin/Java Android SDKs for Gemini Developer API or Agent Platform (formerly Vertex AI).
- Multimodal prompts with text, images, audio, video and PDFs depending on the selected model.
- Function calling, streaming, Google Search/Maps grounding capabilities, App Check protection and configurable per-user rate limits.
- Gemini Developer API has a no-cost starting tier; actual quota depends on model, account and project status. Vertex/Agent Platform requires billing.

Vidya decision:
- Prefer Firebase AI Logic over a raw Gemini REST call from Android if/when a conversational cloud expert is enabled.
- Enable App Check and per-user quotas before public release.
- Keep a strict system instruction: only cite supplied retrieved evidence for machine-specific repair procedures; otherwise abstain.
- Do not assume Firebase AI Logic SDK directly exposes every Gemini API tool. File Search ingestion and store management should be done in a trusted backend unless the SDK explicitly supports the needed API.
- No dependency is added until Firebase project configuration and App Check setup are available and verified.

Official docs:
- https://firebase.google.com/docs/ai-logic
- https://firebase.google.com/docs/ai-logic/quotas
- https://developer.android.com/ai/gemini

### D. ML Kit GenAI / Gemini Nano — optional, not a required feature

ML Kit GenAI provides on-device summarization, image description, embeddings and agent-style generation on supported devices through Android AICore. It can be private, offline and without per-call server charges. But support is restricted to a published device list, and AICore may return quota or availability errors.

Vidya decision:
- Do not make it a mandatory path. Many Android phones do not support Gemini Nano. Keep existing local rules/OCR working without it.
- Only add as an optional capability after checking the exact device at runtime and testing fallback behaviour.

Official docs:
- https://developers.google.com/ml-kit/genai
- https://developers.google.com/ml-kit/genai/prompt/android/get-started

## 2. Microsoft Azure technologies

### A. Azure AI Document Intelligence — useful for difficult manuals

What it offers:
- OCR and structured layout extraction from PDF and image documents.
- Extracts pages, paragraphs, lines, words, tables, section headings, selection marks and geometry/polygons.
- Layout results can be returned in Markdown, which is useful for preserving manual structure and tables.

Free-tier caution:
- F0 analysis is limited to the first 2 pages per request and 4 MB per document; Standard tier supports much larger documents but is paid.
- A scanned manual may still need image quality and page-by-page validation. OCR is not a guarantee that wiring symbols or safety tables were interpreted correctly.

Vidya decision:
- Useful optional server-side OCR fallback for scanned manuals or complex tables after a small measured trial.
- Keep ML Kit local OCR for offline use and privacy.
- Do not send a user's manual or camera image to Azure without explicit disclosure/consent and a documented retention policy.

Official docs:
- https://learn.microsoft.com/azure/ai-services/document-intelligence/prebuilt/layout
- https://learn.microsoft.com/azure/ai-services/document-intelligence/service-limits

### B. Azure AI Search — powerful document retrieval

What it offers:
- Keyword search, vector similarity search and hybrid retrieval (keyword + vector combined).
- Can use integrated chunking/vectorization pipelines or accept precomputed embeddings.
- Can serve as the knowledge retrieval layer for a RAG assistant.

Free-tier caution:
- The Free tier is limited (documented 50 MB storage in the free-trial guide), one free search service per subscription, no scaling, and some features such as semantic ranking/managed identity are unavailable.
- A free Azure account currently advertises USD $200 credits for 30 days, subject to sign-up terms and eligibility. This is a trial credit, not an ongoing free production backend.
- Vector operations may have no extra feature surcharge, but the Search service itself and model/embedding resources can still cost money.

Vidya decision:
- Consider only when Vidya has a substantial, legally usable and curated manual collection. It is unnecessary for the current small local manual library.
- If adopted, index manufacturer/model/year/market/manual revision/page/section and source URL as filterable fields.
- Use hybrid retrieval so exact model and DTC strings are not lost by semantic search.

Official docs:
- https://learn.microsoft.com/en-us/azure/search/vector-search-overview
- https://learn.microsoft.com/en-us/azure/search/search-try-for-free
- https://learn.microsoft.com/en-us/azure/search/search-sku-tier

### C. Microsoft Foundry Agent Service — managed orchestration

What it offers:
- Agent runtime, toolboxes, model catalog, observability/tracing, evaluations and identity/security controls.
- Tools include file search, web search/grounding, Azure AI Search, functions, OpenAPI and MCP connections.
- Web grounding can return current public-web information with source attribution.

Vidya decision:
- Architecturally useful if a future hosted Vidya expert needs several tools and centralized monitoring.
- It is a cloud service with project/model/resource configuration and billing considerations. Do not use it as a prerequisite for the offline Android app.
- If used, restrict web search to official OEM/manufacturer and government domains for repair evidence; keep community forums as unverified leads only.
- Do not confuse the current Foundry Agent Service with the older classic Agents API; Microsoft documentation says classic agents are scheduled for retirement on 31 March 2027.

Official docs:
- https://learn.microsoft.com/en-us/azure/ai-services/agents/overview
- https://learn.microsoft.com/en-us/azure/foundry/agents/how-to/tools/web-search
- https://learn.microsoft.com/en-us/azure/foundry/agents/concepts/tool-best-practice

## 3. Open-source references worth reusing

These are useful reference implementations, not code to copy blindly. Review each repo's current LICENSE and dependencies before copying any file.

| Repository | License shown by GitHub | Reusable idea for Vidya |
|---|---|---|
| https://github.com/googlesamples/mlkit | Apache-2.0 | Camera image processing, ML Kit setup, lifecycle and sample patterns |
| https://github.com/Azure-Samples/azure-search-openai-demo | MIT | RAG separation: retrieval, citations, chat UI, configuration and deployment |
| https://github.com/Azure-Samples/azure-ai-search-multimodal-sample | MIT | Multimodal document retrieval architecture and image/text indexing |

Do not import their entire cloud infrastructure into this Android project. Their sample apps can require Azure resources, model deployments, keys, containers and ongoing charges. Reuse only small, relevant patterns with attribution and license notices.

## 4. Recommended Vidya architecture

1. **Camera (offline):** CameraX frame → ML Kit OCR + generic object boxes → optional custom LiteRT part detector when a properly trained and licensed model exists.
2. **Identity confirmation:** OCR/nameplate clues + user confirmation for machine type, manufacturer, exact model, year and market. Never silently infer a model from a logo or generic object label.
3. **Local retrieval:** Search user-imported PDF pages first. Show manual title, page and exact excerpt.
4. **Official source discovery:** Search only manufacturer/government sources; save canonical URL, title, publication/revision date and retrieval date. Do not scrape or redistribute copyrighted manuals without permission.
5. **Cloud RAG (optional):** Server-side Gemini File Search first pilot; Azure AI Search is an alternative if the document collection becomes large. Keep provider interfaces replaceable.
6. **Expert dialogue:** Ask one targeted question at a time. Gather symptoms, warning lights/error code, when it occurs and recent changes. Send the structured answers back to retrieval and assessment.
7. **Grounded answer:** Return only steps supported by a cited, exact-model source. Show source title, URL, page/section, model/variant match and uncertainty.
8. **Safety gate:** No generated instructions for live electrical work, EV high voltage, airbags/brakes, gas, refrigerant, microwave capacitors, rotating/PTO machinery, fuel leaks or other hazardous internal repairs. Stop and escalate to a qualified technician.

### Mandatory evidence record

Every future retrieved passage should carry:
- Source type: OEM / government / standards body / third-party / community.
- Manufacturer, machine type, model, year, market and variant (known/unknown).
- Manual title, revision/date, page and section.
- Original URL and retrieval timestamp.
- Exact retrieved text or image/page reference.
- Match status: exact / partial / unknown.
- OCR/retrieval confidence where available.
- Whether a human reviewer checked the source.

A model must never invent a manual page, source URL, torque value, wiring pinout, fuse rating, lubricant grade, service interval or repair sequence.

## 5. Implementation order and acceptance tests

**Phase 0 — already present:** CameraX, ML Kit OCR, local PDF OCR, local keyword retrieval and conservative rule-based experts.

**Phase 1 — safe local improvements:** improve model/year/variant metadata, exact-code retrieval, source-page display and regression tests. No cloud bill.

**Phase 2 — small Google RAG pilot:** one backend endpoint, Firebase App Check/authentication, strict request limits, a few permitted OEM manuals, Gemini File Search and page-level citations. No API secrets in APK.

**Phase 3 — custom visual model:** collect licensed/consented images of machine and component classes, label bounding boxes, split train/validation/test by physical machine (avoid near-duplicate leakage), train/evaluate a compact LiteRT detector, test on low-end Android hardware and ship only if false positives are acceptably low.

**Phase 4 — optional Azure comparison:** evaluate Document Intelligence on the same sample manuals and compare OCR/layout accuracy, latency and cost against ML Kit. Only consider Azure AI Search/Foundry if the Google pilot or scale requirements justify it.

Acceptance tests before any repair guidance is released:
- Exact make/model/year/variant mismatch must cause abstention.
- Unknown DTC or absent manual must not be guessed.
- Every nontrivial repair claim must link to the exact supporting source/page.
- OCR-confused digits, units, decimal points and negative signs must be flagged for user confirmation.
- Dangerous task classes must stop the workflow and escalate.
- Network failure, quota exhaustion and model unavailability must fall back to local search or a clear "no verified source found" response.
- No green/safe status may be inferred from an object detector or language model output.

## Research conclusion

For Vidya's current stage, **Google's on-device ML Kit + local manual library + a carefully protected Gemini File Search backend** is the most practical path to validate first. Microsoft's Document Intelligence and Azure AI Search are strong alternatives for later document processing and larger RAG workloads, but introduce more cloud setup and cost. The two ecosystems should not both be wired into the app immediately; compare them on the same small test set before choosing.
