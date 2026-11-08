package com.vidya.safetyassistant.equipment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdexDtcEntryTest {
    @Test fun mapsGenericCodeAsReferenceNotConfirmedDiagnosis() {
        val entry = ObdexDtcEntry(
            code = "P0420",
            title = "Catalyst System Efficiency Below Threshold (Bank 1)",
            description = "The monitored catalyst system is below its expected threshold.",
            affectedComponents = listOf("catalytic_converter", "oxygen_sensor_downstream"),
            commonCauses = listOf("Catalyst aged or contaminated", "Sensor signal issue"),
            symptoms = listOf("Check engine light on"),
            sources = listOf("https://example.org/reference")
        )
        val result = entry.toDiagnosticResult()
        assertEquals("P0420", result.code)
        assertEquals(2, result.possibleCauses.size)
        assertFalse(result.manufacturerSpecific)
        assertEquals("Reference only — severity needs vehicle context", result.severity)
        assertTrue(result.limitation.contains("compatibility is not verified"))
        assertEquals(listOf("https://example.org/reference"), result.sourceReferences)
        assertTrue(result.safeChecks.any { it.contains("not a confirmed diagnosis") })
    }

    @Test fun emptyDescriptionFallsBackToTitle() {
        val entry = ObdexDtcEntry("P0101", "Air flow range", "", emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals("Air flow range", entry.toDiagnosticResult().meaning)
    }
}
