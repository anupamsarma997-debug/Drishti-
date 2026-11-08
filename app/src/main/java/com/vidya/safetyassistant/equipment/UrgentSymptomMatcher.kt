package com.vidya.safetyassistant.equipment

import java.util.Locale

/**
 * Shared offline keyword triage for urgent symptoms.
 * Phrase matches trigger stop-use guidance; they are not sensor-confirmed diagnoses.
 */
internal object UrgentSymptomMatcher {
    private val urgentPhrases = listOf(
        "smoke", "dhua", "dhuan", "fire", "aag", "spark", "sparking", "chingaari",
        "burning smell", "burnt smell", "jalne ki boo", "jalne ki badbu",
        "electric shock", "shock", "bijli ka jhatka", "karant lag", "current aa raha",
        "current lag raha", "fuel leak", "petrol leak", "diesel leak", "tel leak",
        "petrol gir raha", "diesel gir raha", "gas leak", "gas smell", "gas ki boo",
        "brake fail", "brake failure", "brake not working", "brakes not working",
        "brake pedal soft", "brake pedal goes to floor", "brake nahi lag",
        "brake kaam nahi kar", "battery swelling", "battery swollen", "battery bulging",
        "battery phool", "battery ful", "battery hot", "overheat", "overheating",
        "high voltage", "orange cable", "wire exposed", "wire khula", "wire kata",
        "exposed wire", "blade", "jammed", "crushed"
    )

    fun isUrgent(complaint: String): Boolean {
        val normalized = complaint.trim().lowercase(Locale.ROOT)
        return normalized.isNotEmpty() && urgentPhrases.any { it in normalized }
    }
}
