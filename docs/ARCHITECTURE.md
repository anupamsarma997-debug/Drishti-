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

Modules are a proposed structure, not yet implemented.

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

## Build sequencing
1. Empty Compose app, navigation, permission flows.
2. Camera preview and local capture.
3. Safety state machine and refusal tests.
4. PDF import and manual retrieval.
5. Text-based grounded guidance.
6. Voice input/output.
7. Image understanding integration.
8. Visual overlays, then optional ARCore.
9. Controlled usability and safety evaluation.
