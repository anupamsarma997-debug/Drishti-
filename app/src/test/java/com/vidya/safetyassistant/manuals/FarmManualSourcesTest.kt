package com.vidya.safetyassistant.manuals

import org.junit.Assert.*
import org.junit.Test

class FarmManualSourcesTest {
    @Test fun allGovernmentSourcesUseHttps() {
        assertTrue(FarmManualSources.entries.size >= 4)
        assertTrue(FarmManualSources.entries.all { it.url.startsWith("https://") && it.name.isNotBlank() })
    }

    @Test fun includesNorthEastFarmMachineryInstitute() {
        assertTrue(FarmManualSources.entries.any { it.name.contains("North Eastern Region") })
    }
}
