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
        val result = MachineRecognitionRouter.suggestFromOcr("TRACTOR 45 hp")
        assertEquals("tractor", result?.machineId)
        assertEquals("Tractor & Farm Machine Expert", result?.expertName)
    }

    @Test
    fun routesHindiMachineNamesFromOcr() {
        assertEquals("tractor", MachineRecognitionRouter.suggestFromOcr("महिंद्रा ट्रैक्टर")?.machineId)
        assertEquals("motor", MachineRecognitionRouter.suggestFromOcr("कृषि पानी का पंप")?.machineId)
        assertEquals("rotavator", MachineRecognitionRouter.suggestFromOcr("रोटावेटर")?.machineId)
    }

    @Test
    fun routesAdditionalRuralAndWorkshopMachinesToExistingExperts() {
        assertEquals("chaff_cutter", MachineRecognitionRouter.suggestFromOcr("Chaff cutter 2 HP")?.machineId)
        assertEquals("compressor", MachineRecognitionRouter.suggestFromOcr("Air compressor")?.machineId)
        assertEquals("forklift", MachineRecognitionRouter.suggestFromOcr("Forklift")?.machineId)
    }

    @Test
    fun routesAdditionalRuralMachinesToTheirExistingSpecialists() {
        assertEquals("winnower", MachineRecognitionRouter.suggestFromOcr("Grain winnower")?.machineId)
        assertEquals("grain_dryer", MachineRecognitionRouter.suggestFromOcr("Paddy grain dryer")?.machineId)
        assertEquals("feed_mixer", MachineRecognitionRouter.suggestFromOcr("Hammer mill feed grinder")?.machineId)
        assertEquals("root_digger", MachineRecognitionRouter.suggestFromOcr("Potato digger")?.machineId)
        assertEquals("areca_harvester", MachineRecognitionRouter.suggestFromOcr("Areca nut harvester")?.machineId)
        assertEquals("tea_harvester", MachineRecognitionRouter.suggestFromOcr("Tea leaf plucker")?.machineId)
        assertEquals("tillage_implements", MachineRecognitionRouter.suggestFromOcr("Tractor cultivator")?.machineId)
        assertEquals("planting_equipment", MachineRecognitionRouter.suggestFromOcr("Seed drill")?.machineId)
        assertEquals("mulcher_baler", MachineRecognitionRouter.suggestFromOcr("Straw baler")?.machineId)
        assertEquals("dairy", MachineRecognitionRouter.suggestFromOcr("Milking machine")?.machineId)
        assertEquals("power_tools", MachineRecognitionRouter.suggestFromOcr("Power tools")?.machineId)
    }

    @Test
    fun distinguishesTeaAndArecaHarvestersFromGenericReaper() {
        assertEquals("tea_harvester", MachineRecognitionRouter.suggestFromOcr("Tea harvester")?.machineId)
        assertEquals("areca_harvester", MachineRecognitionRouter.suggestFromOcr("Areca harvester")?.machineId)
        assertEquals("reaper", MachineRecognitionRouter.suggestFromOcr("Reaper machine")?.machineId)
    }

    @Test
    fun doesNotMatchMachineCluesInsideUnrelatedWords() {
        assertNull(MachineRecognitionRouter.suggestFromOcr("carburetor cleaner"))
        assertNull(MachineRecognitionRouter.suggestFromOcr("service department"))
        assertNull(MachineRecognitionRouter.suggestFromOcr("TVS Motor Company"))
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
