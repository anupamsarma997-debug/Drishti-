# AR research pack for Vidya

Research date: 2026-11-07

This document records useful public Android AR repositories inspected for Vidya. It is a technical selection guide, not a claim that any third-party code has already been integrated or device-tested.

## Recommended foundation

**Long-term direction: ARCore + SceneView + Jetpack Compose. Current integration: SceneView 2.3.0 View API hosted in Compose `AndroidView`.**

Why:
- Vidya already uses Kotlin, Compose, CameraX and minSdk 26.
- SceneView provides ARCore session/rendering and 3D nodes without Vidya needing to write an OpenGL renderer first.
- Compose can keep Vidya's instruction, warning, voice and step controls as normal UI around the AR surface.
- SceneView 4.x is Compose-native, but its current published toolchain requires a newer Kotlin/AGP/JDK baseline than this repository. Vidya currently uses Kotlin 2.0.21, AGP 8.8.0 and Java 17, so the implementation deliberately uses the 2.3.0 View API rather than forcing a risky toolchain upgrade.
- ARCore plane hit-testing can anchor a visual marker to a detected physical surface; anchors track the physical world as the camera moves.

### Repositories inspected

| Repository | Useful parts | License / caution |
|---|---|---|
| [rahulmasal/ARWifiScanner](https://github.com/rahulmasal/ARWifiScanner) | Compose + AndroidView + ARSceneView, plane hit-test, AnchorNode, SphereNode, Compose HUD and clear-marker flow. Its ARScreen is the closest small implementation pattern for Vidya. | README says MIT, but a root LICENSE file was not found in this inspection. Verify license with owner before copying source. |
| [Qu4X/ReMeasure](https://github.com/Qu4X/ReMeasure) | Compose navigation, ViewModel/StateFlow, measurement history, AR surface placement and polished Material 3 structure. | Apache-2.0. Its inspected dependency catalog uses SceneView 2.3.0 and ARCore 1.47.0; project uses Java 21, so do not copy its entire build configuration into Vidya. |
| [PauGuillamon/sample_compose_arcore](https://github.com/PauGuillamon/sample_compose_arcore) | Compose + OpenGL ES + ARCore, renderer separated through ViewModel, avoids unnecessary Compose recompositions, hitTest/anchors, Depth API occlusion and EIS. Best advanced reference for a future performance/depth phase. | MIT. Preserve copyright/license notice if adapting code. |
| [SceneView/sceneform-android](https://github.com/SceneView/sceneform-android) | Explains Sceneform lineage, glTF/GLB model loading, animated models, depth occlusion and ARCore features. | README explicitly points new projects to SceneView Kotlin successor; this repository is the Java continuation, not the preferred new Kotlin foundation. |
| [JN0V/TraceGlass](https://github.com/JN0V/TraceGlass) | CameraX + OpenCV ArUco marker tracking, perspective correction, homography, smoothing, camera lifecycle, modular MVVM and session persistence. Useful if Vidya later needs to align instructions to a printed manual diagram or a physical marker. | GPL-3.0. Do not copy its source into Vidya without accepting GPL obligations and reviewing distribution implications. Also, this is camera overlay tracking, not ARCore world-space anchoring. |
| [google-ar/arcore-android-sdk](https://github.com/google-ar/arcore-android-sdk) | Official ARCore Android samples, including augmented image tracking and session handling. Use as authoritative API reference. | Apache-2.0 in the inspected sample. Prefer official current sample/API guidance for lifecycle and platform compatibility. |

## Small AR placement pattern inspected

The ARWifiScanner sample demonstrates the following flow (described here, not copied wholesale):

1. Host an `ARSceneView` inside Compose using `AndroidView`.
2. On a screen touch, get the current ARCore frame.
3. Run `frame.hitTest(x, y)`.
4. Accept a hit only when its trackable is a detected `Plane` and the hit pose lies inside that plane polygon.
5. Create an ARCore `Anchor` from the hit.
6. Attach an `AnchorNode` to the scene and put a 3D node under it.
7. Keep Compose controls (step title, safety warning, voice replay, next/back) as a normal overlay.

This is a good starting pattern for placing a neutral AR step marker. It does **not** identify a wire, terminal, machine part, or prove that the chosen physical location is correct.

## Dependency versions and compatibility decision

The ARWifiScanner app uses:
```kotlin
implementation("com.google.ar:core:1.46.0")
implementation("io.github.sceneview:arsceneview:2.2.1")
implementation("io.github.sceneview:sceneview:2.2.1")
```

Vidya currently pins `io.github.sceneview:arsceneview:2.3.0`, which is compiled with Kotlin 2.0.21 according to the SceneView release notes. This is a View-based API and is hosted in Compose with `AndroidView`. The 2.3.0 AAR brings the corresponding `sceneview` module transitively; no separate `sceneview` dependency is required. The app uses Java 17 and compileSdk 35.

SceneView 4.x is the Compose-native successor, but current 4.x releases require a newer toolchain (Kotlin 2.4, AGP 9.x, Java 21 and newer compile SDK). Do not upgrade Vidya's build toolchain just to use the newest API without a separate migration and CI validation.

Before choosing exact dependencies for Vidya:
- Check the current SceneView release notes and Maven Central metadata.
- Check the supported ARCore version and device compatibility.
- Keep AR optional: devices without Google Play Services for AR must still use Vidya's ordinary camera/manual mode.
- Build and run on a real ARCore-supported phone before enabling the feature by default.

## What Vidya should implement (safe order)

### Phase A — AR foundation (partially implemented)
- Implemented: optional AR mode, separate from CameraX inspection mode.
- Implemented: SceneView 2.3.0 ARSceneView with ARCore session and failure callbacks.
- Implemented: user taps a detected horizontal plane; hit-test must land inside the plane polygon; a neutral 3D sphere is anchored at that point.
- Implemented: AR view is released and its scene cleared when the mode leaves composition.
- Remaining: real-device lifecycle, tracking-loss, ARCore install and low-memory validation; clear/reposition UI and vertical plane handling.

### Phase B — manual-guided visual steps
- Only show AR step markers after the user selects the exact equipment model and a verified manual section.
- Keep the written step and source/page reference visible in Compose.
- Use AR marker as a visual pointer only; show a persistent "verify location against the manual" warning.
- Do not show green "safe" confirmations from AR tracking.
- If tracking is lost, plane confidence is poor, or the anchor drifts, hide the step marker and return to a text-only instruction.

### Phase C — depth and occlusion
- Evaluate Depth API only on supported devices.
- Use occlusion to make 3D overlays appear behind real objects where appropriate.
- Depth occlusion is a rendering feature, not hazard detection or proof of physical clearance.

### Phase D — specialist alignment
- Evaluate image/marker tracking for known manufacturer diagrams, labels or fiducials.
- Keep the camera model and AR anchor model separate: object recognition alone cannot establish precise 3D part location.
- Validate each equipment family with annotated test sets and false-negative analysis before giving operational guidance.

## Current code status

`app/src/main/java/com/vidya/safetyassistant/ar/VidyaARExperience.kt` contains the optional plane-tap marker. The AR mode deliberately labels itself `AR MODE`, hides the manual SAFE/STOP test controls, and states that no machine part or hazard was identified. It does not yet render arrows, part labels, equipment models, manual page overlays, or depth occlusion.

## Safety and product constraints

- AR overlays cannot physically stop a machine, isolate electrical power, or verify a circuit is de-energized.
- Never guide a user to touch live wiring, moving parts, safety interlocks or guards.
- A misplaced arrow can be more dangerous than no arrow. Require manual/model match and explicit user confirmation.
- If the app cannot confidently localize the referenced part, show a non-spatial text instruction instead.
- Green tracking/plane status must never be labelled as equipment SAFE.

## Source links

- ARWifiScanner AR screen: https://github.com/rahulmasal/ARWifiScanner/blob/main/app/src/main/java/com/example/arwifiscanner/ar/ARScreen.kt
- ARWifiScanner build dependencies: https://github.com/rahulmasal/ARWifiScanner/blob/main/app/build.gradle.kts
- ReMeasure app entry/navigation: https://github.com/Qu4X/ReMeasure/blob/main/app/src/main/java/org/remmeasure/MainActivity.kt
- ReMeasure versions: https://github.com/Qu4X/ReMeasure/blob/main/gradle/libs.versions.toml
- Compose + ARCore advanced sample: https://github.com/PauGuillamon/sample_compose_arcore
- TraceGlass architecture: https://github.com/JN0V/TraceGlass/blob/main/docs/architecture.md
- Official ARCore samples: https://github.com/google-ar/arcore-android-sdk
