package com.vidya.safetyassistant

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
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
import com.vidya.safetyassistant.ar.VidyaARExperience
import com.vidya.safetyassistant.manuals.LocalManual
import com.vidya.safetyassistant.manuals.ManualLibrary
import com.vidya.safetyassistant.manuals.ManualTextIndexer
import com.vidya.safetyassistant.camera.CameraDetections
import com.vidya.safetyassistant.camera.CameraPreview
import com.vidya.safetyassistant.safety.SafetyController
import com.vidya.safetyassistant.safety.SafetyEngine
import com.vidya.safetyassistant.safety.SafetyStatus
import com.vidya.safetyassistant.ui.SafetyOverlay
import com.vidya.safetyassistant.voice.OfflineSafetyGuide
import com.vidya.safetyassistant.voice.VoiceAssistant
import java.util.concurrent.Executors

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
    val manualLibrary = remember { ManualLibrary(context) }
    val manualIndexer = remember { ManualTextIndexer(context) }
    val manualExecutor = remember { Executors.newSingleThreadExecutor() }
    var manualQuery by remember { mutableStateOf("") }
    var manualResults by remember { mutableStateOf(emptyList<com.vidya.safetyassistant.manuals.ManualPageText>()) }
    var indexStatus by remember { mutableStateOf("Imported manuals can be indexed locally for page search.") }
    var manuals by remember { mutableStateOf(manualLibrary.list()) }
    var manualStatus by remember { mutableStateOf("Manuals stay on this device.") }
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
    var liveOcrText by remember { mutableStateOf("") }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var arMode by remember { mutableStateOf(false) }
    var requestArAfterPermission by remember { mutableStateOf(false) }
    var arStatus by remember { mutableStateOf("AR mode is optional and requires an ARCore-supported phone.") }
    var voiceStatus by remember { mutableStateOf("Voice assistant is ready when you tap the microphone.") }
    var heardQuestion by remember { mutableStateOf("") }
    var answerToSpeak by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Hindi") }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val languages = listOf(
        "Assamese" to "as-IN", "Hindi" to "hi-IN", "English" to "en-IN",
        "Bengali" to "bn-IN", "Tamil" to "ta-IN", "Telugu" to "te-IN",
        "Marathi" to "mr-IN", "Gujarati" to "gu-IN", "Kannada" to "kn-IN",
        "Malayalam" to "ml-IN", "Punjabi" to "pa-IN", "Urdu" to "ur-IN"
    )

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
    ) { granted ->
        cameraGranted = granted
        if (granted && requestArAfterPermission) arMode = true
        requestArAfterPermission = false
    }
    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val imported = manualLibrary.importPdf(uri)
                manuals = manualLibrary.list()
                manualStatus = "PDF imported locally. Building on-device OCR index…"
                manualExecutor.execute {
                    try {
                        val pages = manualIndexer.index(imported)
                        mainHandler.post { indexStatus = "Indexed $pages pages with on-device OCR (maximum first 40 pages)." }
                    } catch (error: Exception) {
                        mainHandler.post { indexStatus = error.localizedMessage ?: "Could not index this manual." }
                    }
                }
            } catch (error: Exception) {
                manualStatus = error.localizedMessage ?: "Could not import this PDF."
            }
        }
    }

    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        microphoneGranted = granted
        if (granted) voiceAssistant.startListening()
    }

    DisposableEffect(voiceAssistant) {
        onDispose { voiceAssistant.release() }
    }
    DisposableEffect(manualIndexer, manualExecutor) {
        onDispose {
            manualExecutor.shutdownNow()
            manualIndexer.close()
        }
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
                        Box {
                            TextButton(onClick = { languageMenuExpanded = true }) {
                                Text("Language: $selectedLanguage", color = Color(0xFF68E0FF))
                            }
                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false }
                            ) {
                                languages.forEach { (name, tag) ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            selectedLanguage = name
                                            voiceAssistant.setLanguage(tag)
                                            languageMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = if (arMode) "AR MODE" else assessment.status.name,
                        color = if (arMode) Color(0xFF68E0FF) else when (assessment.status) {
                            SafetyStatus.SAFE -> Color(0xFF20D879)
                            SafetyStatus.CAUTION -> Color(0xFFFFC247)
                            SafetyStatus.STOP -> Color(0xFFFF5555)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { arMode = false }
                    ) { Text("Camera inspection") }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (cameraGranted) {
                                arMode = true
                            } else {
                                requestArAfterPermission = true
                                cameraPermission.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) { Text("AR guide") }
                }

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                        .background(Color(0xFF101923), RoundedCornerShape(20.dp))
                ) {
                    if (cameraGranted && arMode) {
                        VidyaARExperience(
                            modifier = Modifier.fillMaxSize(),
                            onStatus = { arStatus = it }
                        )
                    } else if (cameraGranted) {
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
                            onDetectionError = { detectorStatus = it },
                            onTextRecognized = { text -> liveOcrText = text }
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
                if (arMode) {
                    Text(arStatus, color = Color(0xFF68E0FF),
                        style = MaterialTheme.typography.labelSmall)
                    TextButton(onClick = { arMode = false }) { Text("Return to camera inspection") }
                    Text(
                        "AR plane tracking only places a neutral marker on a flat surface. It does not identify machine parts or detect hazards.",
                        color = Color(0xFFFFC247),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (!arMode) {
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
                    Text("Live camera OCR · Latin + Hindi · approximately once per second", color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                    if (liveOcrText.isNotBlank()) {
                        Text("Camera text: ${liveOcrText.take(280)}", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
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
                Spacer(Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { pdfPicker.launch(arrayOf("application/pdf")) }
                ) { Text("Import equipment PDF manual") }
                Text(manualStatus, color = Color(0xFF9BAFC2),
                    style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(
                    value = manualQuery,
                    onValueChange = { manualQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search imported manuals") },
                    singleLine = true
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val query = manualQuery
                        manualExecutor.execute {\n                            val results = manualIndexer.search(query)\n                            mainHandler.post { manualResults = results }\n                        }
                    },
                    enabled = manualQuery.isNotBlank()
                ) { Text("Search manual pages") }
                Text(indexStatus, color = Color(0xFF68E0FF), style = MaterialTheme.typography.labelSmall)
                manualResults.forEach { result ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${result.manualName} · Page ${result.pageNumber}", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text(result.text.take(420), color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
                manuals.take(3).forEach { manual ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${manual.displayName} · ${manual.pageCount} pages",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { openManual(context, manual) }) { Text("Open") }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Manual OCR is a text search aid, not a verified repair procedure. Check the exact equipment model and confirm instructions against the manufacturer source. A green result is not proof of safety. Never touch live wiring or moving machinery.",
                    color = Color(0xFFFFC247),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


private fun openManual(context: android.content.Context, manual: LocalManual) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(manual.uri), "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // A compatible PDF viewer may not be installed on the device.
    }
}
