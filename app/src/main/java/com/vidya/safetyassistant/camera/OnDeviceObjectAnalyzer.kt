package com.vidya.safetyassistant.camera

import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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
 * Generic object labels and throttled OCR only. Neither detects hazards or certifies safety.
 * Only one frame is processed at a time; each ImageProxy is closed after all submitted ML Kit
 * tasks complete. OCR uses the Latin recognizer first and Hindi only as a fallback.
 */
class OnDeviceObjectAnalyzer(
    private val onDetections: (CameraDetections) -> Unit,
    private val onError: (String) -> Unit,
    private val onTextRecognized: (String) -> Unit = {}
) : ImageAnalysis.Analyzer, AutoCloseable {

    private val detector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )
    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val devanagariRecognizer =
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())

    private val frameInFlight = AtomicBoolean(false)
    private var lastSubmittedAt = 0L
    private var lastOcrSubmittedAt = 0L
    @Volatile private var closed = false

    override fun analyze(imageProxy: ImageProxy) {
        if (closed || !frameInFlight.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastSubmittedAt < MIN_ANALYSIS_INTERVAL_MS) {
            frameInFlight.set(false)
            imageProxy.close()
            return
        }
        lastSubmittedAt = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            frameInFlight.set(false)
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val uprightWidth = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val uprightHeight = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height
        val input = try {
            InputImage.fromMediaImage(mediaImage, rotation)
        } catch (error: Exception) {
            frameInFlight.set(false)
            imageProxy.close()
            if (!closed) onError(error.localizedMessage ?: "Could not prepare camera frame.")
            return
        }

        val runOcr = now - lastOcrSubmittedAt >= OCR_INTERVAL_MS
        if (runOcr) lastOcrSubmittedAt = now
        val pendingTasks = AtomicInteger(if (runOcr) 2 else 1)

        fun finishFrame() {
            if (pendingTasks.decrementAndGet() == 0) {
                imageProxy.close()
                frameInFlight.set(false)
            }
        }

        try {
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
                .addOnCompleteListener { finishFrame() }
        } catch (error: Exception) {
            if (!closed) onError(error.localizedMessage ?: "Object detection could not start.")
            finishFrame()
        }

        if (runOcr) {
            try {
                latinRecognizer.process(input)
                    .addOnSuccessListener { result ->
                        if (!closed && result.text.isNotBlank()) {
                            onTextRecognized(result.text.take(MAX_OCR_CHARS))
                        }
                    }
                    .addOnCompleteListener { latinTask ->
                        if (closed || latinTask.result?.text?.isNotBlank() == true) {
                            finishFrame()
                        } else {
                            try {
                                devanagariRecognizer.process(input)
                                    .addOnSuccessListener { result ->
                                        if (!closed && result.text.isNotBlank()) {
                                            onTextRecognized(result.text.take(MAX_OCR_CHARS))
                                        }
                                    }
                                    .addOnFailureListener { error ->
                                        if (!closed) onError(error.localizedMessage ?: "Hindi text recognition failed.")
                                    }
                                    .addOnCompleteListener { finishFrame() }
                            } catch (error: Exception) {
                                if (!closed) onError(error.localizedMessage ?: "Hindi text recognition could not start.")
                                finishFrame()
                            }
                        }
                    }
                    .addOnFailureListener { error ->
                        if (!closed) onError(error.localizedMessage ?: "Live text recognition failed.")
                    }
            } catch (error: Exception) {
                if (!closed) onError(error.localizedMessage ?: "Live text recognition could not start.")
                finishFrame()
            }
        }
    }

    override fun close() {
        closed = true
        detector.close()
        latinRecognizer.close()
        devanagariRecognizer.close()
    }

    private companion object {
        const val MIN_ANALYSIS_INTERVAL_MS = 500L
        const val OCR_INTERVAL_MS = 2_500L
        const val MAX_OCR_CHARS = 600
    }
}
