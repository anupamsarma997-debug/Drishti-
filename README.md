# Vidya — Drishti AI

**See. Understand. Prevent.**

Vidya is the Android app for the Drishti AI technical-safety project. It is being built as an offline-first, camera-assisted inspection and guidance tool.

## Current implementation

The first Android MVP foundation is now in this repository:

- Kotlin + Jetpack Compose Android application.
- CameraX lifecycle-aware live camera preview.
- Compose Canvas overlay with SAFE, CAUTION, and flashing STOP states.
- Manual SAFE/STOP controls for exercising the UI.
- A deterministic six-dimension safety policy engine (Time, ROI, Content, Action, Logic, Predictive Safety).
- Local vibration alert and Android Text-to-Speech warning when a stop condition is raised.
- Android speech recognition adapter with a device-local recognition preference.
- Unit tests for hard-stop, stale-frame, and no-flag cases.
- GitHub Actions workflow to run unit tests and assemble a debug APK.

## Important implementation limits

This is an early MVP foundation, **not a completed or validated safety product**.

- CameraX currently displays the live preview; no object detector, NPU model, or live frame classifier is connected yet.
- The SAFE/STOP buttons simulate classifications for UI and policy testing. They do not inspect real equipment.
- Speech input can capture a spoken question, but no conversational AI or technical knowledge model is connected yet.
- ARCore, manual/PDF retrieval, equipment identification, persistent history, and multilingual answer generation are planned, not implemented.
- A camera cannot establish whether a wire is energized or detect many hidden/internal defects.
- The app's stop event stops the app's guidance workflow and alerts the user. It cannot physically stop a machine, vehicle, or electrical system.
- A green state means only that the current configured checks did not flag a hazard. It is never a guarantee of safety.

## Build

Open this repository in Android Studio, or use a GitHub Actions run:

```bash
gradle testDebugUnitTest assembleDebug
```

Requirements: JDK 17, Android SDK 35, and Gradle 8.10.2.

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk` when the CI build succeeds.

## Safety principles

- Never touch live wiring or open an energized electrical panel.
- Keep away from smoke, fire, exposed conductors, moving machinery, fuel leaks, and damaged batteries.
- Never bypass machine guards or safety interlocks.
- Follow the correct manufacturer's manual and use qualified professionals for hazardous work.
- Do not treat AI output or a camera view as a substitute for professional inspection.

## Project structure

- `app/src/main`: Android application, camera preview, UI overlay, safety controller, and voice adapter.
- `app/src/test`: safety-engine unit tests.
- `docs/`: product requirements, architecture, and safety policy.
- `AGENTS.md`: engineering and safety instructions for coding agents.

## License

No project-wide license has been selected yet. Do not assume the repository is open-source licensed until a LICENSE file is added.
