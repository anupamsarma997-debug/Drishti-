package com.vidya.safetyassistant.manuals

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualSearchRankerTest {
    @Test fun exactPhraseRanksAboveSingleWordOverlap() {
        val broad = ManualPageText("Pump manual.pdf", 2, "Pump water flow and maintenance overview")
        val exact = ManualPageText("Pump manual.pdf", 8, "Error E17 low water pressure pump stops")
        val results = ManualSearchRanker.rank("E17 low water pressure", listOf(broad, exact))
        assertEquals(8, results.first().pageNumber)
    }

    @Test fun allQueryTermsRankAbovePartialMatch() {
        val partial = ManualPageText("Guide.pdf", 1, "The pump has low pressure")
        val complete = ManualPageText("Guide.pdf", 3, "Controller error E17 pump low pressure reset")
        val results = ManualSearchRanker.rank("controller E17 pump", listOf(partial, complete))
        assertEquals(3, results.first().pageNumber)
    }

    @Test fun punctuationAndCaseAreNormalized() {
        val page = ManualPageText("AX-220 manual.pdf", 4, "Model AX 220: Error P-0101")
        assertEquals(1, ManualSearchRanker.rank("ax_220 p0101", listOf(page)).size)
    }

    @Test fun emptyQueriesAndNonPositiveLimitsReturnNothing() {
        val page = ManualPageText("Guide.pdf", 1, "Pump pressure")
        assertTrue(ManualSearchRanker.rank("!!", listOf(page)).isEmpty())
        assertTrue(ManualSearchRanker.rank("pump", listOf(page), 0).isEmpty())
    }

    @Test fun noMatchingPageIsNotInvented() {
        val page = ManualPageText("Guide.pdf", 1, "Pump pressure")
        assertTrue(ManualSearchRanker.rank("refrigerant compressor", listOf(page)).isEmpty())
    }
}
