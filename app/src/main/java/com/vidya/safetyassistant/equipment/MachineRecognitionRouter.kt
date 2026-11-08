package com.vidya.safetyassistant.equipment

import java.util.Locale

data class MachineRouteSuggestion(
    val machineId: String,
    val machineName: String,
    val expertName: String,
    val confidence: Float,
    val needsUserConfirmation: Boolean,
    val reason: String
)

/**
 * Routes only labels that the current generic ML Kit detector actually reports.
 * It does not claim fine-grained make/model recognition. Low-confidence or
 * ambiguous labels are not auto-routed.
 */
object MachineRecognitionRouter {
    private data class Rule(
        val labels: Set<String>,
        val machineId: String,
        val machineName: String,
        val minimumConfidence: Float = 0.65f
    )

    private val rules = listOf(
        Rule(setOf("motorcycle", "motorbike"), "bike", "Motorcycle"),
        Rule(setOf("bicycle", "bike"), "bike", "Bicycle / two-wheeler"),
        Rule(setOf("car", "automobile"), "car", "Car"),
        Rule(setOf("truck", "lorry"), "truck", "Truck"),
        Rule(setOf("bus"), "truck", "Bus / heavy vehicle"),
        Rule(setOf("air conditioner", "air conditioning unit"), "ac", "Air conditioner"),
        Rule(setOf("television", "tv", "monitor"), "tv", "Display / television"),
        Rule(setOf("washing machine", "washer"), "washer", "Washing machine"),
        Rule(setOf("refrigerator", "fridge"), "fridge", "Refrigerator"),
        Rule(setOf("microwave oven", "microwave"), "microwave", "Microwave oven")
    )

    fun suggest(label: String, confidence: Float): MachineRouteSuggestion? {
        val normalized = label.trim().lowercase(Locale.ROOT)
        if (normalized.isBlank() || confidence !in 0f..1f) return null
        val rule = rules.firstOrNull { normalized in it.labels } ?: return null
        if (confidence < rule.minimumConfidence) return null
        val expert = MachineExpertSystem.expertFor(rule.machineId) ?: return null
        return MachineRouteSuggestion(
            machineId = rule.machineId,
            machineName = rule.machineName,
            expertName = expert.name,
            confidence = confidence,
            needsUserConfirmation = true,
            reason = "Camera label suggests this category only. Confirm the machine before troubleshooting."
        )
    }

    fun bestSuggestion(objects: List<DetectedObjectLike>): MachineRouteSuggestion? =
        objects.mapNotNull { suggest(it.label, it.confidence) }
            .maxByOrNull { it.confidence }

    interface DetectedObjectLike {
        val label: String
        val confidence: Float
    }
}
