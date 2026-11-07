package com.vidya.safetyassistant.equipment

import org.junit.Assert.*
import org.junit.Test

class VehicleWarningLightGuideTest {
    @Test fun containsCriticalCommonWarnings() {
        val ids = VehicleWarningLightGuide.entries.map { it.id }.toSet()
        assertTrue(ids.containsAll(setOf("oil_pressure", "coolant_temp", "brake", "battery", "check_engine", "abs", "airbag", "tyre_pressure", "ev_battery")))
    }

    @Test fun criticalWarningsAdviseStoppingOrSpecialistHelp() {
        assertTrue(VehicleWarningLightGuide.find("oil_pressure")!!.response.contains("switch the engine off"))
        assertTrue(VehicleWarningLightGuide.find("ev_battery")!!.response.contains("orange"))
        assertTrue(VehicleWarningLightGuide.find("airbag")!!.response.contains("Do not probe"))
    }

    @Test fun unknownSymbolIsNotGuessed() {
        assertNull(VehicleWarningLightGuide.find("unknown"))
    }

    @Test fun everyEntryHasModelCaveat() {
        assertTrue(VehicleWarningLightGuide.entries.all { it.caveat.isNotBlank() && it.response.isNotBlank() })
    }
}
