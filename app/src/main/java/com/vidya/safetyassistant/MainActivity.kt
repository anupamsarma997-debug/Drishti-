package com.vidya.safetyassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vidya.safetyassistant.camera.CameraDetections
import com.vidya.safetyassistant.camera.CameraPreview
import com.vidya.safetyassistant.safety.SafetyController
import com.vidya.safetyassistant.safety.SafetyEngine
import com.vidya.safetyassistant.safety.SafetyStatus
import com.vidya.safetyassistant.ui.SafetyOverlay
import com.vidya.safetyassistant.voice.OfflineSafetyGuide
import com.vidya.safetyassistant.voice.VoiceAssistant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VidyaApp() }
    }
}

@androidx.compose.runtime.Composable
private fun VidyaApp() {
    val context = LocalContext.current
    val safetyController = remember { SafetyController(context, SafetyEngine()) }
    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var microphoneGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var detections by remember { mutableStateOf<CameraDetections?>(null) }
    var detectorStatus by remember { mutableStateOf("Starting on-device object detector…") }
    var voiceStatus by remember { mutableStateOf("Voice assistant is ready when you tap the microphone.") }
    var heardQuestion by remember { mutableStateOf("") }
    var answerToSpeak by remember { mutableStateOf("") }

    val voiceAssistant = remember {
        VoiceAssistant(
            context = context,
            onRecognizedText = { text ->
                heardQuestion = text
                val answer = OfflineSafetyGuide.answer(text, safetyController.assessment)
                voiceStatus = "Offline safety guide replied. This is general guidance, not equipment-specific repair advice."
                answerToSpeak = answer
            },
            onError = { message -> voiceStatus = message }
        )
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraGranted = granted }
    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        microphoneGranted = granted
        if (granted) voiceAssistant.startListening()
    }

    DisposableEffect(voiceAssistant) {
        onDispose { voiceAssistant.release() }
    }

    val assessment = safetyController.assessment
    LaunchedEffect(answerToSpeak) {
        if (answerToSpeak.isNotBlank()) voiceAssistant.speak(answerToSpeak)
    }
    LaunchedEffect(assessment.evaluatedAtMillis) {
        assessment.voiceWarning?.let(voiceAssistant::speak)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF25C7F7),
            secondary = Color(0xFF68E0FF),
            background = Color(0xFF080D14),
            surface = Color(0xFF111A25)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080D14)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("VIDYA", color = Color.White, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall)
                        Text("Offline Technical Safety Assistant", color = Color(0xFF9BAFC2),
                            style = MaterialTheme.typography.labelMedium)
                    }
                    Text(
                        text = assessment.status.name,
                        color = when (assessment.status) {
                            SafetyStatus.SAFE -> Color(0xFF20D879)
                            SafetyStatus.CAUTION -> Color(0xFFFFC247)
                            SafetyStatus.STOP -> Color(0xFFFF5555)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .background(Color(0xFF101923), RoundedCornerShape(20.dp))
                ) {
                    if (cameraGranted) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            onDetections = { frame ->
                                detections = frame
                                detectorStatus = if (frame.objects.isEmpty()) {
                                    "No generic objects recognized in the latest frame."
                                } else {
                                    "On-device objects: " + frame.objects.take(3).joinToString { it.label }
                                }
                            },
                            onDetectionError = { detectorStatus = it }
                        )
                        SafetyOverlay(
                            status = assessment.status,
                            detections = detections,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Camera access is needed to show the live inspection view.",
                                color = Color.White)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = {
                                cameraPermission.launch(Manifest.permission.CAMERA)
                            }) { Text("Allow camera") }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    assessment.reason,
                    color = Color(0xFFD2DCE7),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "On-device generic object recognition is active. It does not detect electrical faults or certify safety.",
                    color = Color(0xFFFFC247),
                    style = MaterialTheme.typography.labelSmall
                )

                Text(detectorStatus, color = Color(0xFF68E0FF),
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { safetyController.setManualTestState(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF167B50))
                    ) { Text("Test SAFE") }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { safetyController.setManualTestState(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                    ) { Text("Test STOP") }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (microphoneGranted) {
                            voiceAssistant.startListening()
                            voiceStatus = "Listening…"
                        } else {
                            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                ) { Text("🎙 Ask by voice") }
                if (heardQuestion.isNotBlank()) {
                    Text("You said: $heardQuestion", color = Color.White,
                        style = MaterialTheme.typography.bodySmall)
                }
                Text(voiceStatus, color = Color(0xFF9BAFC2),
                    style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "A green result is not proof of safety. Never touch live wiring or moving machinery. For suspected danger, keep away and contact a qualified professional.",
                    color = Color(0xFFFFC247),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
