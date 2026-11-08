package com.vidya.safetyassistant.ai

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation

/**
 * Optional Gemini Nano adapter.
 *
 * The app must check runtime availability before inference. Unsupported devices
 * continue using Vidya's deterministic offline expert system.
 */
class OnDeviceGeminiNano {
    private val model = Generation.getClient()

    suspend fun availabilityMessage(): String {
        return try {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> "Gemini Nano is available on this phone."
                FeatureStatus.DOWNLOADABLE -> "Gemini Nano is supported but its model needs to be downloaded first."
                FeatureStatus.DOWNLOADING -> "Gemini Nano model is currently downloading."
                FeatureStatus.UNAVAILABLE -> "Gemini Nano is not available on this phone. Vidya offline expert remains active."
                else -> "Gemini Nano availability could not be determined. Vidya offline expert remains active."
            }
        } catch (error: Exception) {
            "Gemini Nano could not be checked: ${error.message ?: "device service unavailable"}. Vidya offline expert remains active."
        }
    }

    suspend fun askMachineExpert(
        machine: String,
        brand: String,
        modelName: String,
        year: String,
        complaint: String,
        previousAnswers: String
    ): String {
        val status = model.checkStatus()
        if (status != FeatureStatus.AVAILABLE) {
            return "Gemini Nano is not ready on this phone. Use Vidya's offline expert guidance instead."
        }

        val prompt = """
            You are Vidya, a cautious machine-troubleshooting assistant.
            Treat all output as preliminary triage, never as a confirmed diagnosis.
            Do not instruct the user to open electrical equipment, touch wiring,
            bypass guards/interlocks, handle fuel/gas/refrigerant/pressure systems,
            work on brakes, EV high-voltage systems, or approach moving/hot parts.
            If smoke, fire, shock, fuel/gas leak, brake failure, or battery swelling
            is reported, tell the user to stop, move away, and contact a qualified technician.
            Ask at most one clear follow-up question if essential information is missing.
            Give only safe external observations. Never invent a model-specific manual,
            error-code meaning, part identity, or repair procedure. Say when uncertain.
            Answer in simple language matching the user's complaint where possible.

            Machine category: $machine
            Brand: $brand
            Exact model: $modelName
            Year: $year
            User complaint: $complaint
            Previous user answers: $previousAnswers

            Return concise sections: What may be happening; one next question OR safe observations;
            stop conditions; what exact manual/source information is still needed.
            """.trimIndent()

        return try {
            model.generateContent(prompt).text?.takeIf { it.isNotBlank() }
                ?: "Gemini Nano returned no text. Continue with the offline expert."
        } catch (error: Exception) {
            "On-device AI request failed: ${error.message ?: "unknown error"}. Continue with the offline expert."
        }
    }

    fun close() {
        runCatching { model.close() }
    }
}
