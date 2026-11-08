package com.vidya.safetyassistant.equipment

import java.util.Locale

/** Whole-word and whole-phrase matching for offline symptom keywords. */
internal object SymptomPhraseMatcher {
    fun matches(text: String, phrase: String): Boolean {
        val normalizedText = normalize(text)
        val normalizedPhrase = normalize(phrase)
        if (normalizedText.isBlank() || normalizedPhrase.isBlank()) return false
        return " $normalizedText ".contains(" $normalizedPhrase ")
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
}
