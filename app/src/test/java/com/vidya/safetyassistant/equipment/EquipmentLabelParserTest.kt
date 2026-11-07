package com.vidya.safetyassistant.equipment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EquipmentLabelParserTest {
    @Test fun extractsExplicitNameplateFields() {
        val facts = EquipmentLabelParser.parse(
            """
            Manufacturer: Acme Motors
            Model No: AX-220
            Rated Voltage: 220-240 VAC
            Rated Current: 3.5 A
            Frequency: 50 Hz
            Rated Power: 750 W
            """.trimIndent()
        )
        assertEquals("Acme Motors", facts.manufacturer)
        assertEquals("AX-220", facts.model)
        assertEquals("220-240 VAC", facts.voltage)
        assertEquals("3.5 A", facts.current)
        assertEquals("50 Hz", facts.frequency)
        assertEquals("750 W", facts.power)
    }

    @Test fun doesNotInventFieldsFromUnlabelledNumbers() {
        val facts = EquipmentLabelParser.parse("220 50 750")
        assertFalse(facts.hasAnyFact)
    }

    @Test fun blankInputReturnsNoFacts() {
        assertTrue(!EquipmentLabelParser.parse("  ").hasAnyFact)
    }
}
