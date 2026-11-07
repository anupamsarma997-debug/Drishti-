package com.vidya.safetyassistant.camera

import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class CameraDetections(
    val objects: List<DetectedObject>,
    val imageWidth: Int,
    val imageHeight: Int,
    val analyzedAtMillis: Long
)

/**
 * Bundled on-device ML Kit object detector. It detects generic object categories only;
 * it is NOT a wire-fault, electrical-risk, defect, or machine-safety classifier.
 */
class OnDeviceObjectAnalyzer(
    private val onDetections: (CameraDetections) -> Unit,
    private val onError: (String) -> Unit
) : ImageAnalysis.Analyzer, AutoCloseable {

    private val detector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )
    private var lastSubmittedAt = 0L
    @Volatile private var closed = false

    override fun analyze(imageProxy: ImageProxy) {
        if (closed) {
            imageProxy.close()
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (now - lastSubmittedAt < 250L) {
            imageProxy.close()
            return
        }
        lastSubmittedAt = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val uprightWidth = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val uprightHeight = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height
        val input = InputImage.fromMediaImage(mediaImage, rotation)

        detector.process(input)
            .addOnSuccessListener { detected ->
                if (!closed) {
                    val boxes = detected.map { item ->
                        val rect = item.boundingBox
                        val label = item.labels.maxByOrNull { it.confidence }
                        DetectedObject(
                            label = label?.text ?: "Object",
                            confidence = label?.confidence ?: 0f,
                            left = (rect.left.toFloat() / uprightWidth).coerceIn(0f, 1f),
                            top = (rect.top.toFloat() / uprightHeight).coerceIn(0f, 1f),
                            right = (rect.right.toFloat() / uprightWidth).coerceIn(0f, 1f),
                            bottom = (rect.bottom.toFloat() / uprightHeight).coerceIn(0f, 1f)
                        )
                    }
                    onDetections(
                        CameraDetections(
                            objects = boxes,
                            imageWidth = uprightWidth,
                            imageHeight = uprightHeight,
                            analyzedAtMillis = now
                        )
                    )
                }
            }
            .addOnFailureListener { error ->
                if (!closed) onError(error.localizedMessage ?: "On-device object detection failed.")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun close() {
        closed = true
        detector.close()
    }
}
