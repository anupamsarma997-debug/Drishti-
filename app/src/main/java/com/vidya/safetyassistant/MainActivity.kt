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
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.vidya.safetyassistant.manuals.OfficialManualSources
import com.vidya.safetyassistant.manuals.FarmManualSources
import com.vidya.safetyassistant.camera.CameraDetections
import com.vidya.safetyassistant.camera.CameraPreview
import com.vidya.safetyassistant.equipment.EquipmentLabelFacts
import com.vidya.safetyassistant.equipment.EquipmentLabelParser
import com.vidya.safetyassistant.equipment.EquipmentManualMatcher
import com.vidya.safetyassistant.equipment.HomeTroubleshootingGuide
import com.vidya.safetyassistant.equipment.MachineExpertSystem
import com.vidya.safetyassistant.equipment.ExpertInterviewFlow
import com.vidya.safetyassistant.equipment.MachineRecognitionRouter
import com.vidya.safetyassistant.equipment.MachineRouteSuggestion
import com.vidya.safetyassistant.equipment.RuralMachineGuide
import com.vidya.safetyassistant.equipment.FarmMachineBrandCatalog
import com.vidya.safetyassistant.equipment.PartFailureGuide
import com.vidya.safetyassistant.equipment.VehicleDiagnosticLookup
import com.vidya.safetyassistant.equipment.VehicleDiagnosticResult
import com.vidya.safetyassistant.equipment.VehicleWarningLightGuide
import com.vidya.safetyassistant.safety.SafetyController
import com.vidya.safetyassistant.safety.SafetyEngine
import com.vidya.safetyassistant.safety.SafetyStatus
import com.vidya.safetyassistant.ui.SafetyOverlay
import com.vidya.safetyassistant.voice.OfflineSafetyGuide
import com.vidya.safetyassistant.voice.VoiceAssistant
import com.vidya.safetyassistant.online.OnlineMachineResearch
import com.vidya.safetyassistant.online.OnlineMachineResult
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VidyaApp() }
    }
}

@androidx.compose.runtime.Composable
@OptIn(ExperimentalFoundationApi::class)
private fun VidyaApp() {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val cameraBringIntoViewRequester = remember { BringIntoViewRequester() }
    val safetyController = remember { SafetyController(context, SafetyEngine()) }
    val manualLibrary = remember { ManualLibrary(context) }
    val manualIndexer = remember { ManualTextIndexer(context) }
    val manualExecutor = remember { Executors.newSingleThreadExecutor() }
    var manualQuery by remember { mutableStateOf("") }
    var manualResults by remember { mutableStateOf(emptyList<com.vidya.safetyassistant.manuals.ManualPageText>()) }
    var onlineMachineResults by remember { mutableStateOf(emptyList<OnlineMachineResult>()) }
    var onlineResearchStatus by remember { mutableStateOf("Online machine discovery is optional; verify all technical instructions against the exact official manual.") }
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
    var equipmentFacts by remember { mutableStateOf(EquipmentLabelFacts()) }
    var equipmentManualMatches by remember { mutableStateOf(emptyList<com.vidya.safetyassistant.equipment.EquipmentManualMatch>()) }
    var equipmentMatchStatus by remember { mutableStateOf("Read a clear model number to search your imported manuals.") }
    var applianceMenuExpanded by remember { mutableStateOf(false) }
    var expertMenuExpanded by remember { mutableStateOf(false) }
    var selectedExpertMachineId by remember { mutableStateOf("bike") }
    var expertBrand by remember { mutableStateOf("") }
    var expertModel by remember { mutableStateOf("") }
    var expertYear by remember { mutableStateOf("") }
    var expertComplaint by remember { mutableStateOf("") }
    var expertAssessment by remember { mutableStateOf<com.vidya.safetyassistant.equipment.ExpertAssessment?>(null) }
    var expertFollowUpQuestion by remember { mutableStateOf<String?>(null) }
    var expertFollowUpAnswer by remember { mutableStateOf("") }
    var expertInterviewRound by remember { mutableStateOf(0) }
    var expertManualEvidence by remember { mutableStateOf(emptyList<com.vidya.safetyassistant.manuals.ManualPageText>()) }
    var cameraMachineSuggestion by remember { mutableStateOf<MachineRouteSuggestion?>(null) }
    var symptomMenuExpanded by remember { mutableStateOf(false) }
    var selectedApplianceId by remember { mutableStateOf(HomeTroubleshootingGuide.categories.first().id) }
    var selectedSymptomId by remember { mutableStateOf(HomeTroubleshootingGuide.categories.first().symptoms.first().id) }
    var troubleshootingAdvice by remember { mutableStateOf<com.vidya.safetyassistant.equipment.TroubleshootingAdvice?>(null) }
    var farmMachineMenuExpanded by remember { mutableStateOf(false) }
    var farmBrandMenuExpanded by remember { mutableStateOf(false) }
    var farmSymptomMenuExpanded by remember { mutableStateOf(false) }
    var selectedFarmMachineId by remember { mutableStateOf(RuralMachineGuide.categories.first().id) }
    var selectedFarmSymptomId by remember { mutableStateOf(RuralMachineGuide.categories.first().symptoms.first().id) }
    var farmAdvice by remember { mutableStateOf<com.vidya.safetyassistant.equipment.TroubleshootingAdvice?>(null) }
    var farmBrand by remember { mutableStateOf("") }
    var farmModel by remember { mutableStateOf("") }
    var farmYear by remember { mutableStateOf("") }
    var farmErrorCode by remember { mutableStateOf("") }
    var farmProblemDetails by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Car") }
    var vehicleBrand by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleYear by remember { mutableStateOf("") }
    var vehicleCode by remember { mutableStateOf("") }
    var vehicleResult by remember { mutableStateOf<VehicleDiagnosticResult?>(null) }
    var warningMenuExpanded by remember { mutableStateOf(false) }
    var selectedWarningId by remember { mutableStateOf(VehicleWarningLightGuide.entries.first().id) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    fun retrieveExpertManualEvidence() {
        val query = listOf(expertBrand, expertModel, expertYear, expertComplaint)
            .filter { it.isNotBlank() }.joinToString(" ")
        if (query.isBlank()) {
            expertManualEvidence = emptyList()
            return
        }
        manualExecutor.execute {
            val matches = runCatching { manualIndexer.search(query, 6) }.getOrDefault(emptyList())
            mainHandler.post { expertManualEvidence = matches }
        }
    }
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
        if (granted) uiScope.launch { cameraBringIntoViewRequester.bringIntoView() }
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
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF111A25), RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
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
                Text("VIDYA SPECIALIST AI EXPERTS", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Text("Select a machine expert. Describe the problem to get offline, safety-first troubleshooting clues.",
                    color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                Button(
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    onClick = {
                        arMode = false
                        if (cameraGranted) {
                            uiScope.launch { cameraBringIntoViewRequester.bringIntoView() }
                        } else {
                            requestArAfterPermission = false
                            cameraPermission.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087E9B))
                ) { Text("OPEN CAMERA INSPECTION", fontWeight = FontWeight.Bold) }
                cameraMachineSuggestion?.let { suggestion ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text("OCR MACHINE CLUE: " + suggestion.machineName, color = Color(0xFFFFC247), fontWeight = FontWeight.Bold)
                        Text(suggestion.expertName + ". " + suggestion.reason, color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { selectedExpertMachineId = suggestion.machineId; expertAssessment = null }) { Text("Yes, use this expert") }
                            TextButton(onClick = { cameraMachineSuggestion = null }) { Text("Not this machine") }
                        }
                    }
                }
                val expertMachineOptions = listOf(
                    "bike" to "Motorcycle", "scooter" to "Scooter", "car" to "Car",
                    "ev" to "Electric vehicle", "truck" to "Truck", "tractor" to "Tractor",
                    "power_tiller" to "Power tiller", "rotavator" to "Rotavator",
                    "combine" to "Combine harvester", "sprayer" to "Agricultural sprayer",
                    "diesel_engine" to "Diesel engine", "pump" to "Water pump",
                    "generator" to "Generator", "thresher" to "Thresher",
                    "ac" to "Air conditioner", "tv" to "Television",
                    "washer" to "Washing machine", "fridge" to "Refrigerator",
                    "microwave" to "Microwave", "water_heater" to "Water heater",
                    "motor" to "Electric motor", "power_tools" to "Power tools",
                    "compressor" to "Air compressor", "welder" to "Welding machine",
                    "forklift" to "Forklift", "excavator" to "Excavator",
                    "conveyor" to "Conveyor", "hydraulic_press" to "Hydraulic press",
                    "boiler" to "Boiler", "concrete_mixer" to "Concrete mixer",
                    "crusher" to "Crusher", "cnc" to "CNC machine"
                )
                Box {
                    TextButton(onClick = { expertMenuExpanded = true }) {
                        Text("Machine: ${expertMachineOptions.first { it.first == selectedExpertMachineId }.second}",
                            color = Color.White)
                    }
                    DropdownMenu(expanded = expertMenuExpanded,
                        onDismissRequest = { expertMenuExpanded = false }) {
                        expertMachineOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option.second) }, onClick = {
                                selectedExpertMachineId = option.first
                                expertAssessment = null
                                expertMenuExpanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(value = expertBrand, onValueChange = {
                    expertBrand = it; expertAssessment = null
                }, label = { Text("Brand / company") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expertModel, onValueChange = {
                    expertModel = it; expertAssessment = null
                }, label = { Text("Exact model / variant") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expertYear, onValueChange = {
                    expertYear = it; expertAssessment = null
                }, label = { Text("Year (if known)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expertComplaint, onValueChange = {
                    expertComplaint = it; expertAssessment = null
                }, label = { Text("What is happening? Describe sound, smell, warning or error code") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2)
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    val machineName = expertMachineOptions.first { it.first == selectedExpertMachineId }.second
                    expertAssessment = MachineExpertSystem.assess(
                        selectedExpertMachineId, machineName, expertBrand, expertModel,
                        expertYear, expertComplaint
                    )
                    expertInterviewRound = 0
                    expertFollowUpAnswer = ""
                    expertFollowUpQuestion = ExpertInterviewFlow.nextQuestion(
                        selectedExpertMachineId, expertComplaint, expertInterviewRound
                    )
                    retrieveExpertManualEvidence()
                }, enabled = expertComplaint.isNotBlank()) { Text("Ask selected machine expert") }
                expertAssessment?.let { result ->
                    Text(result.expert.name, color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold)
                    Text(result.summary, color = if (result.stopConditions.firstOrNull()?.startsWith("STOP") == true)
                        Color(0xFFFF7777) else Color.White)
                    Text("POSSIBLE AREAS — NOT A DIAGNOSIS", color = Color(0xFFFFC247),
                        fontWeight = FontWeight.Bold)
                    result.possibleAreas.forEach { Text("• $it", color = Color.White) }
                    Text("SAFE OBSERVATIONS", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold)
                    result.safeChecks.forEachIndexed { index, item ->
                        Text("${index + 1}. $item", color = Color.White,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    result.stopConditions.forEach { Text("• $it", color = Color(0xFFFF8D70),
                        style = MaterialTheme.typography.bodySmall) }
                    Text(result.escalation, color = Color(0xFFFFC247),
                        style = MaterialTheme.typography.bodySmall)
                    Text(result.confidenceNote, color = Color(0xFF9BAFC2),
                        style = MaterialTheme.typography.labelSmall)
                    val expertParts = PartFailureGuide.match(selectedExpertMachineId, expertComplaint)
                    if (expertParts.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("POSSIBLE PARTS — VISUAL CONFIRMATION REQUIRED", color = Color(0xFFFFC247), fontWeight = FontWeight.Bold)
                        expertParts.forEach { part ->
                            Text(part.partName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Clues: " + part.failureClues.joinToString(", "), color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                            part.safeObservations.forEach { Text("Observe: " + it, color = Color.White, style = MaterialTheme.typography.bodySmall) }
                            Text("Technician: " + part.technicianAction, color = Color(0xFF9BAFC2), style = MaterialTheme.typography.bodySmall)
                            Text("Never: " + part.neverDo, color = Color(0xFFFF8D70), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text("MATCHED PAGES FROM YOUR IMPORTED MANUALS", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold)
                    if (expertManualEvidence.isEmpty()) {
                        Text("No matching indexed manual page found on this phone. Import and index the exact model manual, or search official manufacturer sources below.", color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                    } else {
                        expertManualEvidence.forEach { page ->
                            Text(page.manualName + " · page " + page.pageNumber, color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold)
                            Text(page.text.take(420), color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("These are OCR search matches from your files, not authenticated instructions. Verify the manual's make, model and year before use.", color = Color(0xFFFFC247), style = MaterialTheme.typography.labelSmall)
                    }
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        val query = Uri.encode(listOf(expertBrand, expertModel, expertYear, expertComplaint, "official manufacturer service manual fault diagnosis").filter { it.isNotBlank() }.joinToString(" "))
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + query))
                        runCatching { context.startActivity(intent) }.onFailure { onlineResearchStatus = "Could not open a browser on this device." }
                    }) { Text("Research this exact problem on the web") }
                    expertFollowUpQuestion?.let { question ->
                        Spacer(Modifier.height(8.dp))
                        Text("EXPERT FOLLOW-UP QUESTION", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold)
                        Text(question, color = Color.White, style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(value = expertFollowUpAnswer, onValueChange = { expertFollowUpAnswer = it },
                            label = { Text("Your answer") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Button(modifier = Modifier.fillMaxWidth(), enabled = expertFollowUpAnswer.isNotBlank(), onClick = {
                            expertComplaint = listOf(expertComplaint, expertFollowUpAnswer.trim()).filter { it.isNotBlank() }.joinToString(". ")
                            expertFollowUpAnswer = ""
                            expertInterviewRound += 1
                            expertAssessment = MachineExpertSystem.assess(selectedExpertMachineId,
                                expertMachineOptions.first { it.first == selectedExpertMachineId }.second,
                                expertBrand, expertModel, expertYear, expertComplaint)
                            expertFollowUpQuestion = ExpertInterviewFlow.nextQuestion(selectedExpertMachineId, expertComplaint, expertInterviewRound)
                            retrieveExpertManualEvidence()
                        }) { Text("Send answer to expert") }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("HOME MACHINE HELP", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge)
                Text("Choose a machine and describe the symptom. Vidya gives cautious observation-only checks.",
                    color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                Box {
                    TextButton(onClick = { applianceMenuExpanded = true }) {
                        Text("Machine: ${HomeTroubleshootingGuide.categories.first { it.id == selectedApplianceId }.label}", color = Color.White)
                    }
                    DropdownMenu(expanded = applianceMenuExpanded, onDismissRequest = { applianceMenuExpanded = false }) {
                        HomeTroubleshootingGuide.categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.label) }, onClick = {
                                selectedApplianceId = category.id
                                selectedSymptomId = category.symptoms.first().id
                                troubleshootingAdvice = null
                                applianceMenuExpanded = false
                            })
                        }
                    }
                }
                val activeCategory = HomeTroubleshootingGuide.categories.first { it.id == selectedApplianceId }
                Box {
                    TextButton(onClick = { symptomMenuExpanded = true }) {
                        Text("Problem: ${activeCategory.symptoms.first { it.id == selectedSymptomId }.label}", color = Color.White)
                    }
                    DropdownMenu(expanded = symptomMenuExpanded, onDismissRequest = { symptomMenuExpanded = false }) {
                        activeCategory.symptoms.forEach { symptom ->
                            DropdownMenuItem(text = { Text(symptom.label) }, onClick = {
                                selectedSymptomId = symptom.id
                                troubleshootingAdvice = null
                                symptomMenuExpanded = false
                            })
                        }
                    }
                }
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    troubleshootingAdvice = HomeTroubleshootingGuide.advice(selectedApplianceId, selectedSymptomId)
                }) { Text("Show safe checks") }
                troubleshootingAdvice?.let { advice ->
                    Text(advice.title, color = if (advice.stopWork) Color(0xFFFF5555) else Color(0xFF68E0FF),
                        fontWeight = FontWeight.Bold)
                    advice.steps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(advice.warning, color = Color(0xFFFFC247), style = MaterialTheme.typography.labelSmall)
                    val applianceParts = PartFailureGuide.match(selectedApplianceId, advice.title)
                    Text("POSSIBLE COMPONENTS — NOT A CONFIRMED DIAGNOSIS",
                        color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge)
                    applianceParts.forEachIndexed { index, part ->
                        Text("${index + 1}. ${part.partName}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Clues: ${part.failureClues.joinToString(", ")}", color = Color(0xFF9BAFC2),
                            style = MaterialTheme.typography.labelSmall)
                        part.possibleCauses.forEach { Text("Possible causes: $it", color = Color.White,
                            style = MaterialTheme.typography.bodySmall) }
                        part.safeObservations.forEach { Text("Safe observation: $it", color = Color.White,
                            style = MaterialTheme.typography.bodySmall) }
                        Text("Technician inspection: ${part.technicianAction}", color = Color(0xFF9BAFC2),
                            style = MaterialTheme.typography.bodySmall)
                        Text("Do not: ${part.neverDo}", color = Color(0xFFFF8D70),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text("Part suggestions are possibilities only. Do not dismantle electrical, gas, refrigerant or moving assemblies.",
                        color = Color(0xFFFFC247), style = MaterialTheme.typography.labelSmall)
                }
                Text(HomeTroubleshootingGuide.universalHazardWarning(), color = Color(0xFFFF8D70),
                    style = MaterialTheme.typography.labelSmall)

                Spacer(Modifier.height(16.dp))
                Text("VILLAGE & FARM MACHINE HELP", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge)
                Text("Tractors, pumps, paddy machines, threshers, rice mills, sprayers and other rural equipment. Guidance is limited to safe observations and escalation.",
                    color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                Box {
                    TextButton(onClick = { farmMachineMenuExpanded = true }) {
                        Text("Machine: ${RuralMachineGuide.category(selectedFarmMachineId)?.label ?: "Select machine"}",
                            color = Color.White)
                    }
                    DropdownMenu(expanded = farmMachineMenuExpanded, onDismissRequest = { farmMachineMenuExpanded = false }) {
                        RuralMachineGuide.categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.label) }, onClick = {
                                selectedFarmMachineId = category.id
                                selectedFarmSymptomId = category.symptoms.first().id
                                farmAdvice = null
                                farmMachineMenuExpanded = false
                            })
                        }
                    }
                }
                val activeFarmCategory = RuralMachineGuide.category(selectedFarmMachineId)!!
                Text("Examples: ${activeFarmCategory.examples}", color = Color(0xFF9BAFC2),
                    style = MaterialTheme.typography.labelSmall)
                Text("Main hazards: ${activeFarmCategory.hazards}", color = Color(0xFFFF8D70),
                    style = MaterialTheme.typography.labelSmall)
                Box {
                    TextButton(onClick = { farmSymptomMenuExpanded = true }) {
                        Text("Problem: ${activeFarmCategory.symptoms.first { it.id == selectedFarmSymptomId }.label}",
                            color = Color.White)
                    }
                    DropdownMenu(expanded = farmSymptomMenuExpanded, onDismissRequest = { farmSymptomMenuExpanded = false }) {
                        activeFarmCategory.symptoms.forEach { symptom ->
                            DropdownMenuItem(text = { Text(symptom.label) }, onClick = {
                                selectedFarmSymptomId = symptom.id
                                farmAdvice = null
                                farmSymptomMenuExpanded = false
                            })
                        }
                    }
                }
                Box {
                    OutlinedTextField(value = farmBrand, onValueChange = {
                        farmBrand = it
                        farmBrandMenuExpanded = true
                    }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Farm machine company / brand") }, singleLine = true)
                    val brandSuggestions = FarmMachineBrandCatalog.suggestions(selectedFarmMachineId, farmBrand)
                    DropdownMenu(expanded = farmBrandMenuExpanded && brandSuggestions.isNotEmpty(),
                        onDismissRequest = { farmBrandMenuExpanded = false }) {
                        brandSuggestions.take(8).forEach { brand ->
                            DropdownMenuItem(text = { Text(brand) }, onClick = {
                                farmBrand = brand
                                farmBrandMenuExpanded = false
                            })
                        }
                    }
                }
                Text("Brand suggestions are examples only. Choose the exact make from its nameplate or enter any local brand; suggestions do not confirm model compatibility.",
                    color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(value = farmModel, onValueChange = { farmModel = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Machine model / engine model") }, singleLine = true)
                OutlinedTextField(value = farmYear, onValueChange = { farmYear = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Year (if known)") }, singleLine = true)
                OutlinedTextField(value = farmErrorCode, onValueChange = { farmErrorCode = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Display error / fault code (if any)") }, singleLine = true)
                OutlinedTextField(value = farmProblemDetails, onValueChange = { farmProblemDetails = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Describe exactly what is happening (sound, smoke, leak, vibration, when it occurs)") },
                    minLines = 2, maxLines = 4)
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    farmAdvice = RuralMachineGuide.advice(selectedFarmMachineId, selectedFarmSymptomId)
                }) { Text("Analyse symptoms and show safe checks") }
                farmAdvice?.let { advice ->
                    Text(advice.title, color = if (advice.stopWork) Color(0xFFFF5555) else Color(0xFF68E0FF),
                        fontWeight = FontWeight.Bold)
                    advice.steps.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(advice.warning, color = Color(0xFFFFC247), style = MaterialTheme.typography.labelSmall)
                    val complaint = listOf(advice.title, farmProblemDetails, farmErrorCode).filter { it.isNotBlank() }.joinToString(" ")
                    val partFindings = PartFailureGuide.match(selectedFarmMachineId, complaint)
                    Text("POSSIBLE PARTS TO CHECK — NOT A CONFIRMED DIAGNOSIS",
                        color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge)
                    partFindings.forEachIndexed { index, part ->
                        Text("${index + 1}. ${part.partName}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Clues: ${part.failureClues.joinToString(", ")}", color = Color(0xFF9BAFC2),
                            style = MaterialTheme.typography.labelSmall)
                        part.possibleCauses.forEach { Text("Possible causes: $it", color = Color.White,
                            style = MaterialTheme.typography.bodySmall) }
                        part.safeObservations.forEach { Text("Safe observation: $it", color = Color.White,
                            style = MaterialTheme.typography.bodySmall) }
                        Text("Technician should: ${part.technicianAction}", color = Color(0xFF9BAFC2),
                            style = MaterialTheme.typography.bodySmall)
                        Text("Do not: ${part.neverDo}", color = Color(0xFFFF8D70),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text("Vidya cannot confirm a failed component from text alone. Use the exact make/model/year service manual; internal repair and disassembly must be done by a trained technician.",
                        color = Color(0xFFFFC247), style = MaterialTheme.typography.labelSmall)
                }
                Button(modifier = Modifier.fillMaxWidth(), enabled = farmBrand.isNotBlank() || farmModel.isNotBlank() || farmErrorCode.isNotBlank(),
                    onClick = {
                        val query = listOf(farmBrand, farmModel, farmYear, farmErrorCode)
                            .filter { it.isNotBlank() }.joinToString(" ")
                        manualQuery = query
                        manualExecutor.execute {
                            val pages = manualIndexer.search(query, 20)
                            mainHandler.post { manualResults = pages }
                        }
                    }) { Text("Search my imported farm manuals") }
                Text("OFFICIAL FARM MACHINERY REFERENCES", color = Color(0xFF68E0FF),
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                FarmManualSources.entries.forEach { source ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(source.name, color = Color.White, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall)
                        Text(source.description, color = Color(0xFF9BAFC2),
                            style = MaterialTheme.typography.labelSmall)
                        TextButton(onClick = { openOfficialManualSource(context, source.url) }) {
                            Text("Open official reference")
                        }
                    }
                }
                farmAdvice?.let { advice ->
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        val report = buildString {
                            appendLine("VIDYA — FARM MACHINE SERVICE REPORT")
                            appendLine("Machine: ${activeFarmCategory.label}")
                            appendLine("Brand: ${farmBrand.ifBlank { "Not provided" }}")
                            appendLine("Model: ${farmModel.ifBlank { "Not provided" }}")
                            appendLine("Year: ${farmYear.ifBlank { "Not provided" }}")
                            appendLine("Displayed fault code: ${farmErrorCode.ifBlank { "Not provided" }}")
                            appendLine("Reported problem: ${advice.title}")
                            appendLine()
                            appendLine("Safe observations shown by Vidya:")
                            advice.steps.forEachIndexed { index, step -> appendLine("${index + 1}. $step") }
                            appendLine()
                            appendLine("Safety note: ${advice.warning}")
                            appendLine("This is not a confirmed diagnosis or repair instruction. Technician must verify the exact make, model, variant and manufacturer manual.")
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Vidya farm machine service report")
                            putExtra(Intent.EXTRA_TEXT, report)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share report with mechanic"))
                    }) { Text("Share report with mechanic") }
                }
                Text(RuralMachineGuide.universalSafetyNote(), color = Color(0xFFFF8D70),
                    style = MaterialTheme.typography.labelSmall)

                Spacer(Modifier.height(16.dp))
                Text("BIKE & CAR ERROR CODE HELP", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge)
                Text("Enter the exact vehicle details and code. Bike, scooter, car, EV and truck fields are supported; code coverage is currently a small generic OBD-II starter dictionary.",
                    color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Bike", "Scooter", "Car", "EV", "Truck").forEach { type ->
                        TextButton(onClick = { vehicleType = type; vehicleResult = null }) {
                            Text(if (vehicleType == type) "✓ $type" else type,
                                color = if (vehicleType == type) Color(0xFF68E0FF) else Color.White)
                        }
                    }
                }
                OutlinedTextField(value = vehicleBrand, onValueChange = { vehicleBrand = it; vehicleResult = null },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Company / brand (e.g. Honda)") }, singleLine = true)
                OutlinedTextField(value = vehicleModel, onValueChange = { vehicleModel = it; vehicleResult = null },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Exact model / variant") }, singleLine = true)
                OutlinedTextField(value = vehicleYear, onValueChange = { vehicleYear = it; vehicleResult = null },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Model year") }, singleLine = true)
                OutlinedTextField(value = vehicleCode, onValueChange = { vehicleCode = it; vehicleResult = null },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Error / DTC code (e.g. P0300)") }, singleLine = true)
                Button(modifier = Modifier.fillMaxWidth(), enabled = vehicleBrand.isNotBlank() &&
                    vehicleModel.isNotBlank() && vehicleYear.isNotBlank() && vehicleCode.isNotBlank(),
                    onClick = {
                        vehicleResult = VehicleDiagnosticLookup.lookup(
                            vehicleType, vehicleBrand, vehicleModel, vehicleYear, vehicleCode
                        )
                    }) { Text("Explain vehicle error code") }
                vehicleResult?.let { result ->
                    Text("${result.code} · ${result.title}",
                        color = if (result.severity.contains("urgent", true)) Color(0xFFFF5555) else Color(0xFF68E0FF),
                        fontWeight = FontWeight.Bold)
                    Text(result.meaning, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    if (result.possibleCauses.isNotEmpty()) {
                        Text("Possible causes (not confirmed):", color = Color(0xFFFFC247), fontWeight = FontWeight.Bold)
                        result.possibleCauses.forEach { Text("• $it", color = Color.White, style = MaterialTheme.typography.bodySmall) }
                    }
                    Text("Safe next steps:", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold)
                    result.safeChecks.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Severity: ${result.severity}", color = Color(0xFFFFC247), fontWeight = FontWeight.Bold)
                    Text(result.limitation, color = Color(0xFFFF8D70), style = MaterialTheme.typography.labelSmall)
                    val vehicleMachineId = vehicleType.lowercase()
                    val vehicleParts = PartFailureGuide.match(vehicleMachineId, result.meaning + " " +
                        result.possibleCauses.joinToString(" ") + " " + result.code)
                    Text("POSSIBLE VEHICLE COMPONENTS — VERIFY BEFORE REPAIR",
                        color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold)
                    vehicleParts.forEachIndexed { index, part ->
                        Text("${index + 1}. ${part.partName}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Failure clues: ${part.failureClues.joinToString(", ")}", color = Color(0xFF9BAFC2),
                            style = MaterialTheme.typography.labelSmall)
                        part.possibleCauses.forEach { Text("Possible causes: $it", color = Color.White,
                            style = MaterialTheme.typography.bodySmall) }
                        part.safeObservations.forEach { Text("Safe observation: $it", color = Color.White,
                            style = MaterialTheme.typography.bodySmall) }
                        Text("Technician inspection: ${part.technicianAction}", color = Color(0xFF9BAFC2),
                            style = MaterialTheme.typography.bodySmall)
                        Text("Do not: ${part.neverDo}", color = Color(0xFFFF8D70),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text("Vehicle parts list is a general triage reference, not an exact fitment or diagnosis. EV high-voltage systems and brakes require qualified service.",
                        color = Color(0xFFFFC247), style = MaterialTheme.typography.labelSmall)
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        manualQuery = result.code
                        manualExecutor.execute {
                            val pages = manualIndexer.search(result.code, 20)
                            mainHandler.post { manualResults = pages }
                        }
                    }) { Text("Find this code in my imported manuals") }
                }
                Spacer(Modifier.height(12.dp))
                Text("DASHBOARD WARNING LIGHT GUIDE", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge)
                Text("Choose the closest indicator description. Confirm the exact symbol in your vehicle's owner's manual.",
                    color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                Box {
                    TextButton(onClick = { warningMenuExpanded = true }) {
                        Text(VehicleWarningLightGuide.find(selectedWarningId)?.label ?: "Select warning light",
                            color = Color.White)
                    }
                    DropdownMenu(expanded = warningMenuExpanded, onDismissRequest = { warningMenuExpanded = false }) {
                        VehicleWarningLightGuide.entries.forEach { item ->
                            DropdownMenuItem(text = { Text(item.label) }, onClick = {
                                selectedWarningId = item.id
                                warningMenuExpanded = false
                            })
                        }
                    }
                }
                VehicleWarningLightGuide.find(selectedWarningId)?.let { warning ->
                    Text(warning.meaning, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text("What to do: ${warning.urgency}", color = Color(0xFFFFC247), fontWeight = FontWeight.Bold)
                    Text(warning.response, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text(warning.caveat, color = Color(0xFFFF8D70), style = MaterialTheme.typography.labelSmall)
                }
                Text("Important: dashboard warning text, manufacturer-specific codes, motorcycle blink codes and EV high-voltage faults may not follow generic OBD-II definitions. Never probe orange high-voltage cables, bypass safety systems or work under an unsupported vehicle. A code is not a confirmed failed part.",
                    color = Color(0xFFFF8D70), style = MaterialTheme.typography.labelSmall)

                Spacer(Modifier.height(10.dp))
                Text("OFFICIAL MANUAL SOURCES", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge)
                Text("Open the manufacturer's website to select the exact model, year and variant. These are source links, not a guarantee that every model is listed.",
                    color = Color(0xFF9BAFC2), style = MaterialTheme.typography.labelSmall)
                OfficialManualSources.forVehicleType(vehicleType).forEach { source ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(source.brand, color = Color.White, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall)
                            Text(source.description, color = Color(0xFF9BAFC2),
                                style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { openOfficialManualSource(context, source.url) }) {
                            Text("Open")
                        }
                    }
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
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                        .bringIntoViewRequester(cameraBringIntoViewRequester)
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
                            onTextRecognized = { text ->
                                liveOcrText = text
                                val suggestion = MachineRecognitionRouter.suggestFromOcr(text)
                                if (suggestion != null && cameraMachineSuggestion?.machineId != suggestion.machineId) {
                                    cameraMachineSuggestion = suggestion
                                }
                                val parsed = EquipmentLabelParser.parse(text)
                                if (parsed.hasAnyFact) equipmentFacts = EquipmentLabelFacts(
                                    manufacturer = parsed.manufacturer ?: equipmentFacts.manufacturer,
                                    model = parsed.model ?: equipmentFacts.model,
                                    voltage = parsed.voltage ?: equipmentFacts.voltage,
                                    current = parsed.current ?: equipmentFacts.current,
                                    frequency = parsed.frequency ?: equipmentFacts.frequency,
                                    power = parsed.power ?: equipmentFacts.power
                                )
                            }
                        )
                        SafetyOverlay(
                            status = assessment.status,
                            detections = detections,
                            focusHint = expertAssessment?.let {
                                PartFailureGuide.match(selectedExpertMachineId, expertComplaint).firstOrNull()?.partName
                            },
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
                    if (equipmentFacts.hasAnyFact) {
                        Text("Nameplate text clues · verify against the physical label", color = Color(0xFFFFC247), style = MaterialTheme.typography.labelSmall)
                        listOfNotNull(
                            equipmentFacts.manufacturer?.let { "Manufacturer: $it" },
                            equipmentFacts.model?.let { "Model: $it" },
                            equipmentFacts.voltage?.let { "Voltage: $it" },
                            equipmentFacts.current?.let { "Current: $it" },
                            equipmentFacts.frequency?.let { "Frequency: $it" },
                            equipmentFacts.power?.let { "Power: $it" }
                        ).forEach { fact ->
                            Text(fact, color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (!equipmentFacts.model.isNullOrBlank()) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val model = equipmentFacts.model.orEmpty()
                                val manufacturer = equipmentFacts.manufacturer
                                equipmentMatchStatus = "Searching local OCR index for exact model text…"
                                manualExecutor.execute {
                                    val candidates = manualIndexer.search(model, 40)
                                    val matches = EquipmentManualMatcher.findMatches(manufacturer, model, candidates)
                                    mainHandler.post {
                                        equipmentManualMatches = matches
                                        equipmentMatchStatus = if (matches.isEmpty()) {
                                            "No exact model text found in indexed manuals. Check the model number and import the correct manufacturer's PDF."
                                        } else {
                                            "Found ${matches.size} page(s) containing this model text. Confirm the manual cover, manufacturer and full model before relying on it."
                                        }
                                    }
                                }
                            }
                        ) { Text("Find this model in my manuals") }
                        Text(equipmentMatchStatus, color = Color(0xFFFFC247), style = MaterialTheme.typography.labelSmall)
                        equipmentManualMatches.forEach { match ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(
                                    "${match.page.manualName} · Page ${match.page.pageNumber}" +
                                        if (match.manufacturerMentioned) " · brand text also found" else " · brand not confirmed",
                                    color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(match.page.text.take(360), color = Color.White,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
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
                        manualExecutor.execute {
                            val results = manualIndexer.search(query)
                            mainHandler.post { manualResults = results }
                        }
                    },
                    enabled = manualQuery.isNotBlank()
                ) { Text("Search manual pages") }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val query = manualQuery.trim()
                        onlineResearchStatus = "Searching public online reference…"
                        onlineMachineResults = emptyList()
                        manualExecutor.execute {
                            try {
                                val results = OnlineMachineResearch.search(query)
                                mainHandler.post {
                                    onlineMachineResults = results
                                    onlineResearchStatus = if (results.isEmpty())
                                        "No public overview found. Try the exact machine name or model."
                                    else "Found ${results.size} public overview result(s). These are discovery references, not verified repair instructions."
                                }
                            } catch (error: Exception) {
                                mainHandler.post {
                                    onlineResearchStatus = error.localizedMessage ?: "Online lookup failed. Check internet connection and try again."
                                }
                            }
                        }
                    },
                    enabled = manualQuery.isNotBlank()
                ) { Text("Find machine information online") }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val query = Uri.encode("${manualQuery.trim()} official manufacturer service manual troubleshooting error code")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
                        runCatching { context.startActivity(intent) }
                            .onFailure { onlineResearchStatus = "Could not open a browser on this device." }
                    },
                    enabled = manualQuery.isNotBlank()
                ) { Text("Search official manuals & error codes on web") }
                Text(onlineResearchStatus, color = Color(0xFFFFC247),
                    style = MaterialTheme.typography.labelSmall)
                onlineMachineResults.forEach { result ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Text(result.title, color = Color(0xFF68E0FF),
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text(result.summary, color = Color.White,
                            style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.url))) }
                        }) { Text("Open source: Wikipedia") }
                    }
                }
                Text(indexStatus, color = Color(0xFF68E0FF), style = MaterialTheme.typography.labelSmall)
                manualResults.forEach { result ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${result.manualName} · Page ${result.pageNumber}", color = Color(0xFF68E0FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text(result.text.take(420), color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("IMPORTED MANUALS (${manuals.size})", color = Color(0xFF68E0FF),
                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                manuals.forEach { manual ->
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
                        TextButton(onClick = {
                            manualLibrary.remove(manual.uri)
                            manualIndexer.removeIndex(manual.uri)
                            manuals = manualLibrary.list()
                            manualResults = manualResults.filterNot { it.manualName == manual.displayName }
                            manualStatus = "Removed ${manual.displayName} and its local search index."
                            indexStatus = "Manual list updated."
                        }) { Text("Remove") }
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

private fun openOfficialManualSource(context: android.content.Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        // No browser or handler is available for this link.
    }
}
