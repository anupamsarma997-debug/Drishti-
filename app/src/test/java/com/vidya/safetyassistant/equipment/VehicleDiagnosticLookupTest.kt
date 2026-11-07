package com.vidya.safetyassistant.equipment

import org.junit.Assert.*
import org.junit.Test

class VehicleDiagnosticLookupTest {
    @Test fun decodesKnownGenericCodeButWarnsAboutVehicleCompatibility() {
        val result = VehicleDiagnosticLookup.lookup("Car", "Maruti Suzuki", "Swift", "2020", "p0300")
        assertNotNull(result)
        assertEquals("P0300", result!!.code)
        assertTrue(result.meaning.contains("misfires"))
        assertTrue(result.limitation.contains("compatibility has not been verified"))
    }

    @Test fun unknownManufacturerCodeIsNotGuessed() {
        val result = VehicleDiagnosticLookup.lookup("Bike", "Example", "Model X", "2024", "P1A23")
        assertNotNull(result)
        assertTrue(result!!.manufacturerSpecific)
        assertTrue(result.title.contains("Manufacturer-specific"))
        assertTrue(result.meaning.contains("different"))
    }

    @Test fun requiresVehicleIdentity() {
        assertNull(VehicleDiagnosticLookup.lookup("Car", "", "Model", "2020", "P0300"))
    }

    @Test fun invalidCodeIsRejectedSafely() {
        val result = VehicleDiagnosticLookup.lookup("Car", "Brand", "Model", "2020", "ENGINE HOT")
        assertNotNull(result)
        assertEquals("Code format not recognized", result!!.title)
    }

    @Test fun doesNotClaimUnknownGenericCodeIsKnown() {
        val result = VehicleDiagnosticLookup.lookup("Car", "Brand", "Model", "2020", "P0999")
        assertNotNull(result)
        assertEquals("Code not in Vidya's current reference", result!!.title)
    }
}
