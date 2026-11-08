package com.vidya.safetyassistant.equipment

import com.vidya.safetyassistant.manuals.ManualPageText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EquipmentManualMatcherTest {
    @Test fun findsExactModelWithPunctuationVariations() {
        val pages = listOf(
            ManualPageText("Generic.pdf", 1, "Model AX 220 installation"),
            ManualPageText("Brand.pdf", 4, "ACME MOTOR MODEL: AX-220 wiring overview"),
            ManualPageText("Other.pdf", 2, "Model AX-2200")
        )
        val matches = EquipmentManualMatcher.findMatches("ACME MOTOR", "AX-220", pages)
        assertEquals(2, matches.size)
        assertEquals(4, matches.first().page.pageNumber)
        assertTrue(matches.first().manufacturerMentioned)
    }

    @Test fun rejectsSubstringOnlyModelMatch() {
        val pages = listOf(ManualPageText("Other.pdf", 1, "Model AX-2200"))
        assertTrue(EquipmentManualMatcher.findMatches(null, "AX-220", pages).isEmpty())
    }

    @Test fun rejectsAmbiguousOneCharacterModel() {
        val pages = listOf(ManualPageText("Any.pdf", 1, "Model X and other equipment"))
        assertTrue(EquipmentManualMatcher.findMatches(null, "X", pages).isEmpty())
    }

    @Test fun emptyModelNeverMatches() {
        val pages = listOf(ManualPageText("Any.pdf", 1, "AX-220"))
        assertTrue(EquipmentManualMatcher.findMatches(null, " ", pages).isEmpty())
    }
}
