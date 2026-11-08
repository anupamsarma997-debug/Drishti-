package com.vidya.safetyassistant.equipment

import org.junit.Assert.*
import org.junit.Test

class MachineExpertSystemTest {
    @Test fun routesMachinesToSpecialistExperts() {
        assertEquals("Bike & Car Expert", MachineExpertSystem.expertFor("bike")?.name)
        assertEquals("Tractor & Farm Machine Expert", MachineExpertSystem.expertFor("tractor")?.name)
        assertEquals("Home Appliance Expert", MachineExpertSystem.expertFor("washer")?.name)
        assertEquals("Motor & Pump Expert", MachineExpertSystem.expertFor("motor")?.name)
        assertEquals("Workshop & Power Tool Expert", MachineExpertSystem.expertFor("power_tools")?.name)
        assertEquals("Industrial Machine Expert", MachineExpertSystem.expertFor("forklift")?.name)
    }

    @Test fun symptomAssessmentProvidesCautiousChecks() {
        val result = MachineExpertSystem.assess(
            "bike", "Motorcycle", "Honda", "Shine", "2022",
            "engine clicks and does not start"
        )!!
        assertTrue(result.possibleAreas.any { it.contains("Starting") })
        assertTrue(result.safeChecks.isNotEmpty())
        assertTrue(result.confidenceNote.contains("cannot prove"))
    }

    @Test fun hazardousComplaintTriggersStopFirstGuidance() {
        val result = MachineExpertSystem.assess(
            "tractor", "Tractor", "Brand", "Model", "2020", "smoke and burning smell"
        )!!
        assertTrue(result.summary.contains("Potential safety hazard"))
        assertTrue(result.stopConditions.any { it.startsWith("STOP") })
    }

    @Test fun brakeFailureAndFuelLeakPhrasesTriggerStopGuidance() {
        val brake = MachineExpertSystem.assess(
            "car", "Car", "Make", "Model", "2020", "brake pedal goes to floor"
        )!!
        assertTrue(brake.summary.contains("Potential safety hazard"))
        assertTrue(brake.stopConditions.any { it.startsWith("STOP") })

        val leak = MachineExpertSystem.assess(
            "tractor", "Tractor", "Make", "Model", "2020", "diesel leak"
        )!!
        assertTrue(leak.summary.contains("Potential safety hazard"))
    }

    @Test fun romanHindiHazardPhrasesTriggerStopGuidance() {
        val brake = MachineExpertSystem.assess(
            "bike", "Bike", "Make", "Model", "2021", "brake nahi lag raha"
        )!!
        assertTrue(brake.summary.contains("Potential safety hazard"))
        assertTrue(brake.stopConditions.any { it.startsWith("STOP") })

        val electric = MachineExpertSystem.assess(
            "motor", "Motor", "Make", "Model", "2021", "bijli ka jhatka lag raha"
        )!!
        assertTrue(electric.summary.contains("Potential safety hazard"))
    }

    @Test fun unknownMachineIsNotGuessed() {
        assertNull(MachineExpertSystem.expertFor("unknown_machine"))
        assertNull(MachineExpertSystem.assess("unknown_machine", "Unknown", "", "", "", "noise"))
    }
}
