package com.vidya.safetyassistant.equipment

import java.util.Locale

/**
 * Shared offline keyword triage for urgent symptoms.
 * Phrase matches trigger stop-use guidance; they are not sensor-confirmed diagnoses.
 */
internal object UrgentSymptomMatcher {
    private val urgentPhrases = listOf(
        "smoke from", "smoke coming", "smoke nikal", "smoke aa", "dhua", "dhuan", "fire from", "fire coming", "fire nikal", "aag", "sparking", "sparks", "visible spark", "spark coming", "chingaari",
        "burning smell", "burnt smell", "jalne ki boo", "jalne ki badbu",
        "electric shock", "bijli ka jhatka", "karant lag", "current aa raha",
        "current lag raha", "fuel leak", "petrol leak", "diesel leak", "tel leak",
        "petrol gir raha", "diesel gir raha", "gas leak", "gas smell", "gas ki boo",
        "brake fail", "brake failure", "brake not working", "brakes not working",
        "brake pedal soft", "brake pedal goes to floor", "brake nahi lag",
        "brake kaam nahi kar", "battery swelling", "battery swollen", "battery bulging",
        "battery phool", "battery ful", "battery hot", "overheat", "overheating",
        "high voltage", "orange cable", "wire exposed", "wire khula", "wire kata",
        "exposed wire", "blade exposed", "blade broken", "guard missing",
        "machine jammed", "hand crushed", "finger crushed"
    )

    fun isUrgent(complaint: String): Boolean {
        val padded = " ${normalize(complaint)} "
        if (padded.isBlank()) return false
        return urgentPhrases.any { phrase ->
            padded.contains(" ${normalize(phrase)} ")
        }
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
}
