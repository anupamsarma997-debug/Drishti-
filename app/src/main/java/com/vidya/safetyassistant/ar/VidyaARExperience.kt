package com.vidya.safetyassistant.ar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size

/**
 * Optional ARCore surface-placement demonstration.
 *
 * The marker is deliberately generic: plane tracking does not identify equipment parts
 * and must never be interpreted as a safety assessment.
 */
@Composable
fun VidyaARExperience(
    modifier: Modifier = Modifier,
    onStatus: (String) -> Unit
) {
    var markerAnchor by remember { mutableStateOf<Anchor?>(null) }

    ARSceneView(
        modifier = modifier,
        planeRenderer = true,
        onSessionUpdated = { _, frame ->
            if (markerAnchor == null) {
                val plane = frame.getUpdatedTrackables(Plane::class.java)
                    .firstOrNull {
                        it.trackingState == TrackingState.TRACKING &&
                            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    }
                if (plane != null) {
                    try {
                        markerAnchor = plane.createAnchor(plane.centerPose)
                        onStatus("Surface tracked. Neutral marker placed; no equipment or hazard was identified.")
                    } catch (error: Exception) {
                        onStatus(error.localizedMessage ?: "Could not place the AR marker.")
                    }
                } else {
                    onStatus("Move the phone slowly to scan a flat horizontal surface.")
                }
            }
        },
        onSessionFailed = { error ->
            onStatus("AR unavailable: ${error.localizedMessage ?: "Check ARCore installation and camera permission."}")
        },
        onTrackingFailureChanged = { reason ->
            if (reason != null) {
                onStatus("Tracking limited: ${reason.name}. Move slowly and improve lighting.")
            } else if (markerAnchor == null) {
                onStatus("Camera tracking active. Looking for a flat surface…")
            }
        }
    ) {
        markerAnchor?.let { anchor ->
            AnchorNode(anchor = anchor) {
                CubeNode(
                    size = Size(0.06f),
                    position = Position(y = 0.04f)
                )
            }
        }
    }
}
