package com.vidya.safetyassistant.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyEngineTest {
    private val engine = SafetyEngine()

    private fun frame(hazards: Set<HazardType> = emptySet()) = FrameClassification(
        timestampMillis = 10_000L,
        equipmentLabel = "test",
        hazards = hazards,
        confidence = 0.99f,
        context = Context6D(true, true, true, true, true, true)
    )

    @Test fun exposedConductorTriggersStop() {
        val result = engine.evaluate(frame(setOf(HazardType.EXPOSED_CONDUCTOR)), 10_000L)
        assertEquals(SafetyStatus.STOP, result.status)
        assertTrue(result.stopRequired)
        assertTrue(!result.voiceWarning.isNullOrBlank())
    }

    @Test fun staleFrameCannotBeMarkedSafe() {
        val result = engine.evaluate(frame(), 20_000L)
        assertEquals(SafetyStatus.CAUTION, result.status)
    }

    @Test fun blankEquipmentIdentityCannotBeMarkedSafe() {
        val result = engine.evaluate(frame().copy(equipmentLabel = "  "), 10_000L)
        assertEquals(SafetyStatus.CAUTION, result.status)
        assertTrue(result.reason.contains("cannot confirm"))
    }

    @Test fun unknownEquipmentIdentityCannotBeMarkedSafe() {
        val result = engine.evaluate(frame().copy(equipmentLabel = "Unknown"), 10_000L)
        assertEquals(SafetyStatus.CAUTION, result.status)
    }

    @Test fun noFlaggedHazardStillCannotClaimMachineIsSafe() {
        val result = engine.evaluate(frame(), 10_000L)
        assertEquals(SafetyStatus.CAUTION, result.status)
        assertTrue(result.reason.contains("cannot prove safety"))
    }
}
