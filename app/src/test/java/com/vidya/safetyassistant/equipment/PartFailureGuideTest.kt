package com.vidya.safetyassistant.equipment

import org.junit.Assert.*
import org.junit.Test

class PartFailureGuideTest {
    @Test fun providesPartLevelCluesForMajorRuralMachines() {
        listOf(
            "tractor", "power_tiller", "rotavator", "pump", "diesel_engine", "sprayer",
            "thresher", "combine", "reaper", "chaff_cutter", "rice_mill", "maize_sheller",
            "brush_cutter", "power_weeder", "transplanter", "solar_pump", "generator",
            "winnower", "grain_dryer", "feed_mixer", "root_digger", "areca_harvester",
            "tea_harvester", "tillage_implements", "planting_equipment", "fodder_harvester",
            "mulcher_baler", "dairy"
        ).forEach { machineId ->
            assertTrue("No parts for $machineId", PartFailureGuide.forMachine(machineId).size >= 2)
        }
    }

    @Test fun matchesComplaintToPlausiblePartsWithoutClaimingDiagnosis() {
        val matches = PartFailureGuide.match("tractor", "engine cranks slowly and battery clicks")
        assertTrue(matches.isNotEmpty())
        assertTrue(matches.any { it.partName.contains("Battery") })
        assertTrue(matches.all { it.safeObservations.isNotEmpty() && it.technicianAction.isNotBlank() })
    }

    @Test fun hazardousSystemsAlwaysIncludeDoNotDoBoundary() {
        listOf("pump", "solar_pump", "sprayer", "thresher", "combine", "generator",
            "tillage_implements", "fodder_harvester", "mulcher_baler").forEach { machineId ->
            assertTrue(PartFailureGuide.forMachine(machineId).all { it.neverDo.isNotBlank() })
        }
    }

    @Test fun unknownMachineReturnsNoGuessedParts() {
        assertTrue(PartFailureGuide.forMachine("unknown").isEmpty())
        assertTrue(PartFailureGuide.match("unknown", "smoke and noise").isEmpty())
    }
}
