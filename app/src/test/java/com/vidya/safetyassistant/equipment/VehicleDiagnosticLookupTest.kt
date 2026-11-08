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
        assertFalse(result.manufacturerSpecific)
    }

    @Test fun standardizedP2RangeIsNotMisclassifiedAsManufacturerSpecific() {
        val result = VehicleDiagnosticLookup.lookup("Car", "Example", "Model Y", "2023", "P2A00")
        assertNotNull(result)
        assertFalse(result!!.manufacturerSpecific)
        assertEquals("Code not in Vidya's current reference", result.title)
    }

    @Test fun manufacturerControlledP3RangeRequiresExactLookup() {
        val result = VehicleDiagnosticLookup.lookup("Car", "Example", "Model Y", "2023", "P3A00")
        assertNotNull(result)
        assertTrue(result!!.manufacturerSpecific)
        assertTrue(result.title.contains("Manufacturer-specific"))
    }

    @Test fun classifiesGenericAndManufacturerControlledRangesAcrossDtcFamilies() {
        val genericCodes = listOf("P0123", "P2123", "B0123", "C0123", "U0123")
        val manufacturerCodes = listOf("P1123", "P3123", "B1123", "C1123", "U1123")

        genericCodes.forEach { code ->
            val result = VehicleDiagnosticLookup.lookup("Car", "Example", "Model", "2022", code)
            assertNotNull("Expected result for $code", result)
            assertFalse("$code should not be manufacturer-specific", result!!.manufacturerSpecific)
        }
        manufacturerCodes.forEach { code ->
            val result = VehicleDiagnosticLookup.lookup("Car", "Example", "Model", "2022", code)
            assertNotNull("Expected result for $code", result)
            assertTrue("$code should be manufacturer-specific", result!!.manufacturerSpecific)
        }
    }

    @Test fun recognizesManufacturerControlledSecondRangesForNonPowertrainFamilies() {
        val manufacturerCodes = listOf("B2123", "C2123", "U2123")
        manufacturerCodes.forEach { code ->
            val result = VehicleDiagnosticLookup.lookup("Car", "Example", "Model", "2022", code)
            assertNotNull("Expected result for $code", result)
            assertTrue("$code should be manufacturer-specific", result!!.manufacturerSpecific)
        }
    }

    @Test fun reservedThirdRangesForNonPowertrainFamiliesRemainUnverifiedNotManufacturerSpecific() {
        val reservedCodes = listOf("B3123", "C3123", "U3123")
        reservedCodes.forEach { code ->
            val result = VehicleDiagnosticLookup.lookup("Car", "Example", "Model", "2022", code)
            assertNotNull("Expected result for $code", result)
            assertFalse("$code is reserved, not classified as manufacturer-specific", result!!.manufacturerSpecific)
            assertEquals("Code not in Vidya's current reference", result.title)
        }
    }

    @Test fun doesNotClaimUnknownGenericCodeIsKnown() {
        val result = VehicleDiagnosticLookup.lookup("Car", "Brand", "Model", "2020", "P0999")
        assertNotNull(result)
        assertEquals("Code not in Vidya's current reference", result!!.title)
    }
}
