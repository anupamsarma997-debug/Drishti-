# Drishti AI — Initial Architecture

## Proposed client
- Kotlin
- Jetpack Compose for UI
- CameraX for camera lifecycle, preview, and capture
- Android SpeechRecognizer or supported on-device speech services for voice input
- Android TextToSpeech for spoken output
- ARCore only on supported devices; Compose/Canvas camera overlays as fallback
- Room/DataStore for local preferences and offline manual metadata

## Proposed application modules
- app: navigation, screens, permissions, and app lifecycle
- core-camera: CameraX preview and capture
- core-safety: hazard states, stop conditions, and policy enforcement
- core-manuals: PDF import, metadata, indexing, and citations
- core-voice: speech input/output and language selection
- feature-inspection: capture and visible-observation workflow
- feature-guidance: one-step-at-a-time instruction UI
- feature-visualization: labels, arrows, and optional AR presentation
- data: repositories and network/local data sources

Current code is still a single Gradle :app module with packages for camera, safety, UI, and voice. CameraX preview and a bundled ML Kit generic object detector are implemented in the app. An optional ARCore/SceneView 2.3.0 view can place a neutral sphere marker on a user-tapped tracked horizontal plane. The generic detector is not a specialist hazard model, and AR plane tracking does not identify machine components. The proposed feature-module split remains future refactoring.

## AI request pipeline
1. User explicitly captures an image and selects an equipment/task context.
2. Client performs basic quality checks and removes unnecessary metadata.
3. Safety pre-check screens obvious high-risk cases and limits the workflow.
4. Vision service returns structured visible observations and uncertainty.
5. Manual retrieval searches only the identified model's trusted documents.
6. Guidance generation uses retrieved manual passages and policy constraints.
7. Safety post-check blocks unsupported or hazardous procedural output.
8. UI displays observations, manual citations, uncertainty, and safe next steps.

## Trust boundaries
- Android client is untrusted; never embed privileged service credentials.
- AI-generated output is untrusted until checked against safety policy and retrieved sources.
- Uploaded manuals are untrusted input until parsed and validated.
- A model's confidence score is not proof of correctness.
- Remote services must enforce authentication, rate limits, access controls, and retention limits.

## Privacy
- Request camera/microphone permissions only when needed.
- Explain when images/audio leave the device.
- Prefer on-device processing for supported low-risk tasks.
- Do not retain raw media by default.
- Provide deletion controls and avoid collecting precise location unless a feature requires it.

## Implementation status and next sequence
1. Implemented: Compose app shell, camera permission, CameraX preview, low-resolution throttled analysis.
2. Implemented: bundled ML Kit generic object detection with Canvas bounding boxes; no camera frame upload.
3. Implemented: conservative safety state machine with manual test classifications, vibration, and TTS warnings.
4. Implemented: Android speech adapter, 12-option language selector, and limited rule-based offline safety FAQ (Hindi/English answers only).
5. Implemented: local PDF picker/library with persisted URI access, page-count metadata, and open-in-viewer action; no text extraction or RAG yet.
6. Implemented: optional ARCore plane tracking and tap-to-place neutral marker using SceneView 2.3.0 View API; devices without ARCore retain CameraX mode.
7. Next: validate Gradle build and device camera/AR lifecycle on a physical supported phone; add automated tests for camera lifecycle and permissions.
8. Next: PDF text extraction, local indexing, and manual-grounded answers with source/page references.
9. Next: specialist equipment-specific vision models and verified hazard datasets; never infer safety from generic labels.
10. Next: step-by-step manual-grounded AR arrows only after exact model/part localization is validated.
11. Required before safety claims: controlled field testing, false-negative analysis, low-end device profiling, and independent safety review.


## Home-appliance model lookup (current implementation)

The camera OCR nameplate parser can extract explicitly labelled model/manufacturer clues. When a model clue exists, the UI can search the local OCR index of imported PDFs for an exact model string. The matcher tolerates common separators (space, dot, underscore, slash, hyphen), rejects longer alphanumeric substring matches, and ranks pages that also contain manufacturer text.

This is a text-presence lookup only. It does not prove that the imported PDF is genuine, that the physical appliance is the same model, or that an instruction is safe/applicable. The user must verify the manual cover and complete model against the physical nameplate. No automatic repair instructions are generated from these matches.
