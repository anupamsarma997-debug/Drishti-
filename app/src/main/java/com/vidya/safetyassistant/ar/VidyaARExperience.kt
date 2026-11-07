package com.vidya.safetyassistant.ar

import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.SphereNode

/**
 * Optional ARCore surface marker. A tap on a tracked plane places a neutral visual marker.
 * This is not object recognition, equipment-part localization, or hazard detection.
 */
@Composable
fun VidyaARExperience(
    modifier: Modifier = Modifier,
    onStatus: (String) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ARSceneView(context).also { sceneView ->
                onStatus("AR camera starting. Move slowly, then tap a detected flat surface.")

                sceneView.onSessionFailed = { error ->
                    onStatus("AR unavailable: ${error.localizedMessage ?: "Install/update Google Play Services for AR or use camera mode."}")
                }
                sceneView.onTrackingFailureChanged = { reason ->
                    if (reason != null) {
                        onStatus("Tracking limited: ${reason.name}. Move slowly and improve lighting.")
                    }
                }
                sceneView.onSessionUpdated = { _, frame ->
                    if (frame.camera.trackingState == TrackingState.TRACKING) {
                        onStatus("Tracking active. Tap a visible horizontal surface to place a neutral marker.")
                    }
                }
                sceneView.onTouchEvent = { event, _ ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        val frame = sceneView.frame
                        if (frame != null && frame.camera.trackingState == TrackingState.TRACKING) {
                            val hit = frame.hitTest(event.x, event.y).firstOrNull { result ->
                                val trackable = result.trackable
                                trackable is Plane &&
                                    trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                                    trackable.isPoseInPolygon(result.hitPose)
                            }
                            if (hit != null) {
                                try {
                                    val anchor = hit.createAnchor()
                                    val material = sceneView.materialLoader.createColorInstance(
                                        Color(0.12f, 0.78f, 1f, 1f)
                                    )
                                    val marker = SphereNode(
                                        engine = sceneView.engine,
                                        radius = 0.035f,
                                        materialInstance = material
                                    )
                                    val anchorNode = AnchorNode(
                                        engine = sceneView.engine,
                                        anchor = anchor
                                    )
                                    anchorNode.childNodes = setOf(marker)
                                    sceneView.addChildNode(anchorNode)
                                    onStatus("Neutral marker placed on the selected surface. No equipment part or hazard was identified.")
                                } catch (error: Exception) {
                                    onStatus(error.localizedMessage ?: "Could not place the AR marker.")
                                }
                            } else {
                                onStatus("No valid horizontal surface at that point. Scan a flat surface and try again.")
                            }
                        }
                    }
                    true
                }
            }
        },
        onRelease = { sceneView ->
            sceneView.clearChildNodes()
            sceneView.destroy()
        }
    )
}
