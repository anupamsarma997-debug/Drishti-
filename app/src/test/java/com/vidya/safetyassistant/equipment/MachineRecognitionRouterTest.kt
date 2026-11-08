package com.vidya.safetyassistant.equipment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MachineRecognitionRouterTest {
    @Test
    fun routesRecognizedMotorcycleToVehicleExpertButRequiresConfirmation() {
        val result = MachineRecognitionRouter.suggest("motorcycle", 0.91f)
        assertEquals("bike", result?.machineId)
        assertEquals("Bike & Car Expert", result?.expertName)
        assertTrue(result?.needsUserConfirmation == true)
    }

    @Test
    fun routesCarToVehicleExpert() {
        val result = MachineRecognitionRouter.suggest("car", 0.88f)
        assertEquals("car", result?.machineId)
        assertEquals("Bike & Car Expert", result?.expertName)
    }

    @Test
    fun rejectsLowConfidenceOrUnknownLabels() {
        assertNull(MachineRecognitionRouter.suggest("motorcycle", 0.4f))
        assertNull(MachineRecognitionRouter.suggest("unknown machine", 0.99f))
    }

    @Test
    fun rejectsBlankLabelsAndInvalidConfidence() {
        assertNull(MachineRecognitionRouter.suggest("", 0.9f))
        assertNull(MachineRecognitionRouter.suggest("car", 1.5f))
    }
}
