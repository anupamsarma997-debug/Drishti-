# Drishti AI

**See. Understand. Prevent.**

Drishti AI is an Android-first technical safety assistant concept. It is designed to use a phone camera, voice interaction, equipment manuals, and guided visual instructions to help people understand technical tasks and recognize visible hazards before proceeding.

## Project status

This repository starts as a planning and development foundation. It is **not a finished app**, and no real-world safety performance has been validated.

## Planned capabilities

- Camera-based inspection support for motorcycles, electrical panels, machines, pumps, generators, appliances, and tools.
- Identification of visible warning signs and potential hazards, with uncertainty clearly communicated.
- Step-by-step instructions grounded in verified equipment manuals and model-specific documentation.
- Voice questions and answers, with support planned for Assamese, Hindi, English, Bengali, and other Indian languages.
- Visual guidance with labels, arrows, and AR overlays where supported.
- Offline-friendly design, low-bandwidth operation, and privacy-conscious processing.
- Manual and QR/label scanning to identify equipment and retrieve relevant documentation.

## Safety principles

- A phone camera cannot reliably detect hidden, internal, or energized electrical faults.
- Drishti AI must never claim to guarantee accident prevention or replace a qualified technician.
- It must not guide an unqualified user to touch live electrical wiring or high-voltage equipment.
- Vehicle, battery, fuel, hot-surface, and industrial-machine hazards require conservative warnings.
- Safety interlocks must never be bypassed. Lockout/tagout and manufacturer instructions take priority.
- When risk or uncertainty is high, the app must stop the workflow and direct the user to a qualified professional.

## Planned Android technology

- Kotlin and Jetpack Compose
- CameraX for camera capture
- On-device computer vision where practical
- Multimodal AI for image understanding, with confidence and limitation handling
- Android speech recognition and text-to-speech
- ARCore for supported devices, with a camera-overlay fallback
- Firebase for authentication and carefully scoped cloud data
- Retrieval-augmented answers grounded in manuals, including page references

Technology choices will be validated during implementation; no integration is claimed complete yet.

## Development roadmap

1. Define safety requirements, supported equipment, and failure-handling rules.
2. Build the Android app shell and camera preview.
3. Add equipment identification and manual/document ingestion.
4. Add grounded step-by-step guidance and voice interaction.
5. Add visual overlays and optional ARCore support.
6. Test with safe, controlled scenarios and qualified reviewers.
7. Conduct extensive real-world validation before any public safety claims.

## Repository structure (planned)

- `AGENTS.md` — instructions for coding agents
- `docs/` — product requirements, architecture, safety policy, and testing plan
- `app/` — Android application source (to be added)

## Important disclaimer

Drishti AI is an assistive guidance project, not a certified inspection system, emergency service, or substitute for professional training. Never rely on camera-based AI alone for electrical, mechanical, vehicle, or industrial safety decisions.

## License

No open-source license has been selected yet. All rights remain with the repository owner until a license is added.
