package com.vidya.safetyassistant.equipment

import com.vidya.safetyassistant.manuals.ManualPageText

data class EquipmentManualMatch(val page: ManualPageText, val manufacturerMentioned: Boolean)

/** Matches explicit model text in OCR pages; it does not authenticate a manual. */
object EquipmentManualMatcher {
    fun findMatches(manufacturer: String?, model: String, pages: List<ManualPageText>, limit: Int = 8): List<EquipmentManualMatch> {
        val pattern = modelPattern(model) ?: return emptyList()
        val brandTokens = manufacturer.orEmpty().uppercase().split(Regex("[^A-Z0-9]+")).filter { it.length >= 2 }
        return pages.mapNotNull { page ->
            if (!pattern.containsMatchIn(page.text.uppercase())) return@mapNotNull null
            val text = page.text.uppercase()
            val brandFound = brandTokens.isNotEmpty() && brandTokens.all { text.contains(it) }
            EquipmentManualMatch(page, brandFound)
        }.sortedWith(compareByDescending<EquipmentManualMatch> { it.manufacturerMentioned }.thenBy { it.page.pageNumber })
            .take(limit.coerceAtLeast(1))
    }

    private fun modelPattern(model: String): Regex? {
        val parts = model.uppercase().split(Regex("[^A-Z0-9]+")).filter { it.isNotBlank() }
        // A one-character OCR fragment is too ambiguous to identify a manual page.
        if (parts.isEmpty() || parts.joinToString("").length < 3) return null
        val body = parts.joinToString("[\\s._/\\-]*") { Regex.escape(it) }
        return Regex("(?<![A-Z0-9])$body(?![A-Z0-9])")
    }
}
