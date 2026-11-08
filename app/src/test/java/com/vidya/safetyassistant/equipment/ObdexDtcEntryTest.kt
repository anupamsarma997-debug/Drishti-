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
            category = "powertrain",
            commonCauses = listOf(
                ObdexDtcCause("Catalyst aged or contaminated", "high"),
                ObdexDtcCause("Sensor signal issue", "medium")
            ),
            symptoms = listOf("Check engine light on"),
            relatedCodes = listOf("P0300"),
            sources = listOf("https://example.org/reference")
        )
        val result = entry.toDiagnosticResult()
        assertEquals("P0420", result.code)
        assertEquals(2, result.possibleCauses.size)
        assertFalse(result.manufacturerSpecific)
        assertEquals("Reference only — severity needs vehicle context", result.severity)
        assertTrue(result.limitation.contains("compatibility is not verified"))
        assertEquals(listOf("https://example.org/reference"), result.sourceReferences)
        assertEquals(listOf("Check engine light on"), result.symptoms)
        assertEquals(listOf("catalytic_converter", "oxygen_sensor_downstream"), result.affectedComponents)
        assertEquals(listOf("P0300"), result.relatedCodes)
        assertEquals(2, result.causeLikelihoodNotes.size)
        assertTrue(result.causeLikelihoodNotes.first().contains("not a probability"))
        assertTrue(result.safeChecks.any { it.contains("not a confirmed diagnosis") })
    }

    @Test fun emptyDescriptionFallsBackToTitle() {
        val entry = ObdexDtcEntry("P0101", "powertrain", "Air flow range", "", emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        assertEquals("Air flow range", entry.toDiagnosticResult().meaning)
    }
}
