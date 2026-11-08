package com.vidya.safetyassistant.equipment

/**
 * Conservative OCR-to-expert routing. The bundled object detector is generic and
 * is not a machine classifier. Routing uses visible text clues only; users must
 * confirm the machine and exact model before any troubleshooting is shown.
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

    // Include common Indian machine names and Hindi nameplate/decal text.
    // Keep each machineId aligned with MachineExpertSystem.expertFor().
    private val rules = listOf(
        Rule(listOf("motorcycle", "motorbike", "two wheeler", "two-wheeler", "bike", "मोटरसाइकिल", "बाइक"), "bike", "Motorcycle"),
        Rule(listOf("scooter", "स्कूटर"), "scooter", "Scooter"),
        Rule(listOf("tractor", "ट्रैक्टर"), "tractor", "Tractor"),
        Rule(listOf("power tiller", "power weeder", "पावर टिलर", "पावर वीडर"), "power_tiller", "Power tiller / weeder"),
        Rule(listOf("rotavator", "rotary tiller", "रोटावेटर"), "rotavator", "Rotavator"),
        Rule(listOf("combine harvester", "combine", "कंबाइन हार्वेस्टर"), "combine", "Combine harvester"),
        Rule(listOf("fodder harvester", "चारा काटने की मशीन"), "fodder_harvester", "Fodder harvester"),
        Rule(listOf("areca harvester", "tea harvester", "reaper", "रीपर", "हार्वेस्टर"), "reaper", "Reaper / harvester"),
        Rule(listOf("chaff cutter", "कुट्टी मशीन", "चारा कटर"), "chaff_cutter", "Chaff cutter"),
        Rule(listOf("maize sheller", "corn sheller", "मक्का छीलने की मशीन"), "maize_sheller", "Maize sheller"),
        Rule(listOf("rice mill", "धान मिल", "चावल मिल"), "rice_mill", "Rice mill"),
        Rule(listOf("thresher", "थ्रेशर"), "thresher", "Thresher"),
        Rule(listOf("sprayer", "स्प्रेयर", "छिड़काव मशीन"), "sprayer", "Agricultural sprayer"),
        Rule(listOf("brush cutter", "ब्रश कटर"), "brush_cutter", "Brush cutter"),
        Rule(listOf("transplanter", "रोपाई मशीन"), "transplanter", "Transplanter"),
        Rule(listOf("water pump", "irrigation pump", "pump", "पानी का पंप", "वाटर पंप", "पंप"), "motor", "Pump / motor"),
        Rule(listOf("solar pump", "सोलर पंप"), "solar_pump", "Solar pump"),
        Rule(listOf("diesel engine", "डीजल इंजन"), "diesel_engine", "Diesel engine"),
        Rule(listOf("generator", "जनरेटर"), "generator", "Generator"),
        Rule(listOf("electric vehicle", "ev", "ईवी"), "ev", "Electric vehicle"),
        Rule(listOf("motorcycle", "motorbike", "मोटरसाइकिल"), "bike", "Motorcycle"),
        Rule(listOf("truck", "lorry", "ट्रक"), "truck", "Truck"),
        Rule(listOf("car", "automobile", "कार", "गाड़ी"), "car", "Car"),
        Rule(listOf("air conditioner", "air conditioning", "ac", "एसी"), "ac", "Air conditioner"),
        Rule(listOf("washing machine", "washer", "वॉशिंग मशीन"), "washer", "Washing machine"),
        Rule(listOf("refrigerator", "fridge", "रेफ्रिजरेटर", "फ्रिज"), "fridge", "Refrigerator"),
        Rule(listOf("microwave oven", "microwave", "माइक्रोवेव"), "microwave", "Microwave oven"),
        Rule(listOf("television", "smart tv", "tv", "टेलीविजन"), "tv", "Television"),
        Rule(listOf("water heater", "geyser", "गीजर"), "water_heater", "Water heater"),
        Rule(listOf("compressor", "कंप्रेसर"), "compressor", "Compressor"),
        Rule(listOf("welder", "welding machine", "वेल्डिंग मशीन"), "welder", "Welding machine"),
        Rule(listOf("drill machine", "power drill", "ड्रिल मशीन"), "drill", "Power drill"),
        Rule(listOf("lathe machine", "लेथ मशीन"), "lathe", "Lathe machine"),
        Rule(listOf("cnc machine", "सीएनसी मशीन"), "cnc", "CNC machine"),
        Rule(listOf("conveyor", "कन्वेयर"), "conveyor", "Conveyor"),
        Rule(listOf("hydraulic press", "हाइड्रोलिक प्रेस"), "hydraulic_press", "Hydraulic press"),
        Rule(listOf("forklift", "फोर्कलिफ्ट"), "forklift", "Forklift"),
        Rule(listOf("excavator", "खुदाई मशीन"), "excavator", "Excavator"),
        Rule(listOf("loader", "लोडर"), "loader", "Loader"),
        Rule(listOf("concrete mixer", "कंक्रीट मिक्सर"), "concrete_mixer", "Concrete mixer"),
        Rule(listOf("crusher", "क्रशर"), "crusher", "Crusher"),
        Rule(listOf("boiler", "बॉयलर"), "boiler", "Boiler")
    )

    fun suggestFromOcr(ocrText: String): MachineRouteSuggestion? {
        val normalized = ocrText.lowercase().trim()
        if (normalized.isBlank()) return null

        val rule = rules
            .sortedByDescending { candidate -> candidate.terms.maxOf { it.length } }
            .firstOrNull { candidate ->
                candidate.terms.any { term -> containsWholeClue(normalized, term) }
            } ?: return null

        val expert = MachineExpertSystem.expertFor(rule.machineId) ?: return null
        return MachineRouteSuggestion(
            machineId = rule.machineId,
            machineName = rule.machineName,
            expertName = expert.name,
            reason = "OCR found a possible machine-name clue. This is not visual recognition; confirm the machine, make and exact model."
        )
    }

    private fun containsWholeClue(text: String, clue: String): Boolean {
        // Devanagari and other non-ASCII clues are matched as literal phrases.
        if (clue.any { it.code > 127 }) return text.contains(clue)
        return Regex("(?<![a-z0-9])" + Regex.escape(clue) + "(?![a-z0-9])")
            .containsMatchIn(text)
    }
}
