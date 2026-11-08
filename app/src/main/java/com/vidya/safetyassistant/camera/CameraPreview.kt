package com.vidya.safetyassistant.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onDetections: (CameraDetections) -> Unit,
    onDetectionError: (String) -> Unit,
    onTextRecognized: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    // Keep callbacks current without tearing down/restarting the camera on recomposition.
    val latestOnDetections = rememberUpdatedState(onDetections)
    val latestOnDetectionError = rememberUpdatedState(onDetectionError)
    val latestOnTextRecognized = rememberUpdatedState(onTextRecognized)
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            // TextureView is more stable than SurfaceView inside a vertically scrolling Compose screen.
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val analysisExecutor = Executors.newSingleThreadExecutor()
        val analyzer = OnDeviceObjectAnalyzer(
            onDetections = { latestOnDetections.value(it) },
            onError = { latestOnDetectionError.value(it) },
            onTextRecognized = { latestOnTextRecognized.value(it) }
        )
        var provider: ProcessCameraProvider? = null
        var boundPreview: Preview? = null
        var boundAnalysis: ImageAnalysis? = null
        var disposed = false

        cameraProviderFuture.addListener({
            if (disposed) return@addListener
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(android.util.Size(640, 480))
                    .build()
                    .also { it.setAnalyzer(analysisExecutor, analyzer) }

                provider = cameraProvider
                boundPreview = preview
                boundAnalysis = analysis
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (error: Exception) {
                latestOnDetectionError.value(error.localizedMessage ?: "Camera could not be started.")
            }
        }, mainExecutor)

        onDispose {
            disposed = true
            try {
                val activeProvider = provider
                val activePreview = boundPreview
                val activeAnalysis = boundAnalysis
                if (activeProvider != null && activePreview != null && activeAnalysis != null) {
                    activeProvider.unbind(activePreview, activeAnalysis)
                }
            } catch (_: Exception) {
                // The lifecycle may already have released the camera.
            }
            analyzer.close()
            analysisExecutor.shutdownNow()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
