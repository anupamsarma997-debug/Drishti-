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

Current code is still a single Gradle :app module with packages for camera, safety, UI, and voice. CameraX preview and a bundled ML Kit generic object detector are implemented in the app. The generic detector is not a specialist hazard model. The proposed feature-module split remains future refactoring.

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
6. Next: validate Gradle build and device camera behavior; add automated tests for camera lifecycle and permissions.
7. Next: PDF text extraction, local indexing, and manual-grounded answers with source/page references.
8. Next: specialist equipment-specific vision models and verified hazard datasets; never infer safety from generic labels.
9. Next: step-by-step manual-grounded guidance, then ARCore on supported devices.
10. Required before safety claims: controlled field testing, false-negative analysis, low-end device profiling, and independent safety review.
