package com.vidya.safetyassistant.equipment

import org.junit.Assert.*
import org.junit.Test

class RuralMachineGuideTest {
    @Test fun coversMajorVillageAndFarmMachines() {
        val labels = RuralMachineGuide.categories.map { it.label }
        listOf("Tractor", "Power tiller / walking tractor", "Irrigation / water pump", "Paddy / multi-crop thresher",
            "Chaff / fodder cutter", "Rice mill / huller / polisher", "Brush cutter / grass trimmer",
            "Solar water pump controller", "Dairy milking machine / milk chiller").forEach { expected ->
            assertTrue("Missing $expected", labels.contains(expected))
        }
    }

    @Test fun everyMachineHasSafeSymptomGuidance() {
        assertTrue(RuralMachineGuide.categories.all { it.symptoms.isNotEmpty() && it.hazards.isNotBlank() })
        assertTrue(RuralMachineGuide.categories.flatMap { it.symptoms }.all { it.safeObservations.isNotEmpty() })
    }

    @Test fun dangerousBlockagesAreStopWork() {
        val thresher = RuralMachineGuide.advice("thresher", "blockage")
        val chaff = RuralMachineGuide.advice("chaff_cutter", "jam")
        assertTrue(thresher!!.stopWork)
        assertTrue(chaff!!.stopWork)
    }

    @Test fun solarDcFaultWarnsOfEnergisedPanels() {
        val advice = RuralMachineGuide.advice("solar_pump", "no_output")
        assertTrue(advice!!.stopWork)
        assertTrue(advice.warning.contains("DC circuits"))
    }

    @Test fun unknownMachineOrSymptomIsNotGuessed() {
        assertNull(RuralMachineGuide.advice("unknown", "unknown"))
    }
}
