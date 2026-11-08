package com.vidya.safetyassistant.equipment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MachineRecognitionRouterTest {
    @Test
    fun routesMotorcycleNameplateClueToVehicleExpertAndRequiresConfirmation() {
        val result = MachineRecognitionRouter.suggestFromOcr("Honda motorcycle model CB")
        assertEquals("bike", result?.machineId)
        assertEquals("Bike & Car Expert", result?.expertName)
        assertTrue(result?.needsUserConfirmation == true)
        assertNull(result?.confidence)
    }

    @Test
    fun routesTractorTextToFarmExpert() {
        val result = MachineRecognitionRouter.suggestFromOcr("TRACTOR diesel engine")
        assertEquals("tractor", result?.machineId)
        assertEquals("Tractor & Farm Machine Expert", result?.expertName)
    }

    @Test
    fun doesNotInferMachineFromGenericMlKitLabels() {
        assertNull(MachineRecognitionRouter.suggestFromOcr("home goods"))
        assertNull(MachineRecognitionRouter.suggestFromOcr("fashion goods"))
        assertNull(MachineRecognitionRouter.suggestFromOcr("unknown object"))
    }

    @Test
    fun rejectsBlankOcr() {
        assertNull(MachineRecognitionRouter.suggestFromOcr("   "))
    }
}
