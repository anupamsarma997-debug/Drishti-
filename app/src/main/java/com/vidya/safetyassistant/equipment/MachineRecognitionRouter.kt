package com.vidya.safetyassistant.equipment

/**
 * Conservative OCR-to-expert routing. The built-in ML Kit object detector only
 * provides coarse categories, so it must not be treated as a vehicle/machine classifier.
 * These are text clues from visible labels/decals and always require user confirmation.
 */
data class MachineRouteSuggestion(
    val machineId: String,
    val machineName: String,
    val expertName: String,
    val confidence: Float? = null,
    val needsUserConfirmation: Boolean = true,
    val reason: String
)

object MachineRecognitionRouter {
    private data class Rule(
        val terms: List<String>,
        val machineId: String,
        val machineName: String
    )

    private val rules = listOf(
        Rule(listOf("motorcycle", "motorbike", "two wheeler", "two-wheeler"), "bike", "Motorcycle"),
        Rule(listOf("scooter"), "scooter", "Scooter"),
        Rule(listOf("tractor"), "tractor", "Tractor"),
        Rule(listOf("power tiller", "power weeder"), "power_tiller", "Power tiller / weeder"),
        Rule(listOf("rotavator", "rotary tiller"), "rotavator", "Rotavator"),
        Rule(listOf("combine harvester", "combine"), "combine", "Combine harvester"),
        Rule(listOf("thresher"), "thresher", "Thresher"),
        Rule(listOf("sprayer"), "sprayer", "Agricultural sprayer"),
        Rule(listOf("water pump", "irrigation pump", "pump"), "motor", "Pump / motor"),
        Rule(listOf("diesel engine"), "diesel_engine", "Diesel engine"),
        Rule(listOf("generator"), "generator", "Generator"),
        Rule(listOf("car", "automobile"), "car", "Car"),
        Rule(listOf("truck", "lorry"), "truck", "Truck"),
        Rule(listOf("electric vehicle", "ev"), "ev", "Electric vehicle"),
        Rule(listOf("air conditioner", "air conditioning"), "ac", "Air conditioner"),
        Rule(listOf("washing machine", "washer"), "washer", "Washing machine"),
        Rule(listOf("refrigerator", "fridge"), "fridge", "Refrigerator"),
        Rule(listOf("microwave oven", "microwave"), "microwave", "Microwave oven"),
        Rule(listOf("television", "smart tv", "tv"), "tv", "Television")
    )

    fun suggestFromOcr(ocrText: String): MachineRouteSuggestion? {
        val normalized = ocrText.lowercase()
        if (normalized.isBlank()) return null
        val rule = rules
            .sortedByDescending { candidate -> candidate.terms.maxOf { it.length } }
            .firstOrNull { candidate ->
                candidate.terms.any { term ->
                    Regex("(?<![a-z0-9])" + Regex.escape(term) + "(?![a-z0-9])")
                        .containsMatchIn(normalized)
                }
            } ?: return null
        val expert = MachineExpertSystem.expertFor(rule.machineId) ?: return null
        return MachineRouteSuggestion(
            machineId = rule.machineId,
            machineName = rule.machineName,
            expertName = expert.name,
            reason = "OCR found a possible machine-name clue. This is not visual recognition; confirm the machine and exact model."
        )
    }
}
