package com.vidya.safetyassistant.equipment

data class EquipmentLabelFacts(
    val manufacturer: String? = null,
    val model: String? = null,
    val voltage: String? = null,
    val current: String? = null,
    val frequency: String? = null,
    val power: String? = null
) {
    val hasAnyFact: Boolean
        get() = listOf(manufacturer, model, voltage, current, frequency, power).any { !it.isNullOrBlank() }
}

/**
 * Extracts explicit nameplate fields from OCR text. This is parsing only, not
 * equipment identification, authenticity checking, or an electrical safety assessment.
 */
object EquipmentLabelParser {
    private fun capture(text: String, pattern: Regex): String? =
        pattern.find(text)?.groupValues?.getOrNull(1)?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.take(48)

    fun parse(text: String): EquipmentLabelFacts {
        if (text.isBlank()) return EquipmentLabelFacts()
        return EquipmentLabelFacts(
            manufacturer = capture(text, Regex("(?im)^\\s*(?:manufacturer|mfg|brand|make)\\s*[:=\\-]\\s*(.+)$")),
            model = capture(text, Regex("(?im)^\\s*(?:model(?:\\s*(?:no|number))?|type)\\s*[:=\\-]\\s*([A-Z0-9][A-Z0-9._/ -]{1,38})$")),
            voltage = capture(text, Regex("(?i)\\b(?:rated\\s*)?voltage\\b\\s*[:=]?\\s*([0-9]{1,4}(?:\\s*[-–/]\\s*[0-9]{1,4})?(?:\\.[0-9]+)?\\s*(?:V(?:AC|DC)?|VAC|VDC))")),
            current = capture(text, Regex("(?i)\\b(?:rated\\s*)?current\\b\\s*[:=]?\\s*([0-9]{1,4}(?:\\.[0-9]+)?\\s*(?:A|mA|AMP(?:S)?))")),
            frequency = capture(text, Regex("(?i)\\bfrequency\\b\\s*[:=]?\\s*([0-9]{1,3}(?:\\s*[-/]\\s*[0-9]{1,3})?\\s*Hz)")),
            power = capture(text, Regex("(?i)\\b(?:rated\\s*)?power\\b\\s*[:=]?\\s*([0-9]{1,6}(?:\\.[0-9]+)?\\s*(?:kW|W|HP))"))
        )
    }
}
