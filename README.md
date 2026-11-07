# Vidya — Drishti AI

**See. Understand. Prevent.**

Vidya is an Android-first, offline-oriented technical inspection assistant.

## Implemented MVP foundation

- Kotlin + Jetpack Compose Android app.
- CameraX live preview with a 640×480 analysis stream and keep-latest backpressure.
- Bundled ML Kit on-device generic object detection; no image upload or API key is used for this detector.
- Live bounding boxes and generic object labels drawn over the camera preview.
- Safety overlay states: SAFE, CAUTION, and flashing STOP.
- Manual SAFE/STOP controls for UI and policy testing.
- Deterministic six-dimension safety policy engine (Time, ROI, Content, Action, Logic, Predictive Safety).
- Stop event triggers local vibration and a Hindi TTS warning.
- Android speech recognition adapter with on-device recognition preference where available.
- UI language selector for Assamese, Hindi, English, Bengali, Tamil, Telugu, Marathi, Gujarati, Kannada, Malayalam, Punjabi, and Urdu (actual voice availability depends on installed device language packs).
- Small deterministic offline safety FAQ for common electrical and moving-machinery questions.
- Unit tests for safety-engine policy and offline FAQ behavior.
- GitHub Actions workflow to run unit tests and assemble a debug APK.

## Critical limits — read before use

This is an early engineering MVP, **not a validated safety product**.

- ML Kit's generic object detector recognizes broad object categories. It does **not** identify incorrect wire connections, energized conductors, high-voltage proximity, unsafe tool angles, hidden defects, internal machine faults, or accident probability.
- Detected object boxes are visual annotations only. They are not hazard boxes.
- Real hazard classifications currently enter the SafetyEngine only through the manual test controls or a future trusted classifier adapter.
- The green test state is simulated and must never be interpreted as a real inspection result or proof of safety.
- Voice input currently uses Android's installed speech service; offline language availability depends on device language packs. The offline FAQ is rule-based, not a generative AI assistant.
- The app can stop its own guidance workflow and alert the user. It cannot physically stop a machine, vehicle, or electrical circuit.
- No ARCore world tracking, manufacturer-manual RAG, PDF extraction, equipment-specific model database, persistent inspection history, or validated multilingual translation is implemented yet.
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

## Project structure

- `app/src/main`: Android application, CameraX preview/analyzer, safety engine/controller, Compose overlay, voice adapter and offline FAQ.
- `app/src/test`: safety-engine and offline-FAQ unit tests.
- `docs/`: product requirements, architecture, and safety policy.
- `AGENTS.md`: engineering and safety instructions for coding agents.

## License

No project-wide license has been selected yet. Do not assume the repository is open-source licensed until a LICENSE file is added.
