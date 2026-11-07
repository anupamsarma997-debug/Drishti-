# Drishti AI — Agent Instructions

## Product goal
Build an Android-first technical safety assistant that helps users understand equipment and procedures through camera-based observations, verified manuals, voice interaction, and visual guidance.

## Non-negotiable safety requirements
- Never claim the app can guarantee accident prevention or reliably detect every fault.
- Clearly distinguish visible observations, model hypotheses, confirmed manual facts, and unknowns.
- A camera cannot establish whether a wire is energized or detect many hidden/internal defects.
- Never instruct an unqualified user to touch live electrical wiring, open energized panels, defeat guards, bypass interlocks, or perform hazardous repairs.
- For high voltage, exposed conductors, smoke, fire, fuel leaks, unstable vehicles, moving machinery, or uncertain high-risk conditions: stop the workflow, show a clear warning, and direct the user to isolate the area safely and contact a qualified professional/emergency service as appropriate.
- Never invent manufacturer instructions, torque values, wiring diagrams, part identities, or safety limits.
- Ground equipment-specific procedures in the correct model's official manual. Show document/page references where available.
- Prefer safe shutdown, isolation, and professional escalation over speculative troubleshooting.

## Engineering rules
- Android-first. Prefer Kotlin, Jetpack Compose, CameraX, Android speech APIs, and ARCore only when device support is available.
- Keep camera capture and AI processing permission-based and transparent.
- Do not upload camera frames or documents without clear user consent.
- Do not commit API keys, service-account files, passwords, signing keys, or personal data.
- Keep secrets on a trusted backend; never embed privileged keys in the Android client.
- Include loading, permission-denied, offline, low-confidence, and failure states.
- Provide accessible text alternatives for visual overlays and support TalkBack.
- Design for low-end Android devices and unreliable connectivity.
- Write tests for safety gates and uncertainty handling, not only happy paths.

## Development workflow
1. Inspect the existing project before changing files.
2. Make small, reviewable changes.
3. Run available tests/build checks when the environment supports them.
4. Report exactly what was tested and what was not.
5. Never claim a build, emulator test, device test, or deployment succeeded unless it actually did.
6. Keep documentation synchronized with implementation.

## Current status
This repository contains an early Android MVP source: CameraX preview + ML Kit generic on-device object boxes, Compose safety overlay, manual-only safety-engine inputs, vibration/TTS stop alerts, Android speech adapter, rule-based offline safety FAQ, local PDF metadata library, and optional ARCore plane tracking with tap-to-place neutral marker. Generic object recognition and AR plane tracking are NOT hazard detection or equipment-part localization. No specialized electrical/mechanical hazard model, manual-grounded AR arrows, PDF/manual RAG, or real-world safety validation is implemented. Do not mark the UI SAFE based only on generic object detection or AR tracking.
