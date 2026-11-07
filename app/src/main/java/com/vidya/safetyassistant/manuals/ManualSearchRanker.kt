package com.vidya.safetyassistant.manuals

/**
 * Lightweight, fully offline ranking for OCR-indexed manual pages.
 * This is lexical retrieval, not semantic understanding or proof that a
 * retrieved instruction applies to the selected machine.
 */
object ManualSearchRanker {
    fun rank(query: String, pages: List<ManualPageText>, limit: Int = 8): List<ManualPageText> {
        if (limit <= 0) return emptyList()
        val terms = tokens(query).distinct()
        if (terms.isEmpty()) return emptyList()

        return pages.mapNotNull { page ->
            val body = normalize(page.text)
            val title = normalize(page.manualName)
            if (body.isBlank()) return@mapNotNull null

            val matched = terms.count { term -> containsToken(body, term) }
            if (matched == 0) return@mapNotNull null

            val frequencyScore = terms.sumOf { term ->
                val count = tokenFrequency(body, term)
                if (count == 0) 0.0 else 1.0 + (count - 1).coerceAtMost(3) * 0.15
            }
            val coverage = matched.toDouble() / terms.size
            val phrase = normalize(query).takeIf { it.length >= 3 }?.let { body.contains(it) } == true
            val titleMatches = terms.count { containsToken(title, it) }

            val score = frequencyScore + (coverage * 3.0) +
                (if (phrase) 2.5 else 0.0) + (titleMatches * 0.6)

            RankedPage(page, score, matched, phrase)
        }.sortedWith(
            compareByDescending<RankedPage> { it.score }
                .thenByDescending { it.matchedTerms }
                .thenByDescending { it.phraseMatch }
                .thenBy { it.page.pageNumber }
        ).take(limit).map { it.page }
    }

    private data class RankedPage(
        val page: ManualPageText,
        val score: Double,
        val matchedTerms: Int,
        val phraseMatch: Boolean
    )

    private fun tokens(value: String): List<String> =
        normalize(value).split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 2 }

    private fun normalize(value: String): String =
        value.lowercase().replace(Regex("(?<=[\\p{L}\\p{N}])[-._/](?=[\\p{L}\\p{N}])"), "").replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()

    private fun containsToken(text: String, term: String): Boolean =
        text.split(' ').any { it == term }

    private fun tokenFrequency(text: String, term: String): Int =
        text.split(' ').count { it == term }
}
