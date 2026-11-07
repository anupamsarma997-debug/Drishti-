package com.vidya.safetyassistant.manuals

import org.junit.Assert.*
import org.junit.Test

class OfficialManualSourcesTest {
    @Test fun everySourceHasValidHttpsUrlAndBrand() {
        assertTrue(OfficialManualSources.entries.isNotEmpty())
        OfficialManualSources.entries.forEach {
            assertTrue(it.brand.isNotBlank())
            assertTrue(it.url.startsWith("https://"))
            assertTrue(it.vehicleTypes.isNotEmpty())
        }
    }

    @Test fun vehicleFilterReturnsRelevantSources() {
        assertTrue(OfficialManualSources.forVehicleType("Bike").any { it.brand.contains("Honda") })
        assertTrue(OfficialManualSources.forVehicleType("Car").any { it.brand.contains("Maruti") })
    }

    @Test fun allSourcesHaveUniqueBrands() {
        val brands = OfficialManualSources.entries.map { it.brand.lowercase() }
        assertEquals(brands.size, brands.toSet().size)
    }
}
