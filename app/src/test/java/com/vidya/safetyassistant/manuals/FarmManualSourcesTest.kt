package com.vidya.safetyassistant.manuals

import org.junit.Assert.*
import org.junit.Test

class FarmManualSourcesTest {
    @Test fun allGovernmentSourcesUseHttps() {
        assertTrue(FarmManualSources.entries.size >= 4)
        assertTrue(FarmManualSources.entries.all { it.url.startsWith("https://") && it.name.isNotBlank() })
    }

    @Test fun includesOfficialAgriculturalMachineryTrainingReferences() {
        assertTrue(FarmManualSources.entries.any { it.name.contains("Directorate General of Training") })
        assertTrue(FarmManualSources.entries.any { it.name.contains("ICAR — Agricultural Engineering machinery catalogue") })
    }

    @Test fun includesOfficialIndianTractorSupportPortals() {
        assertTrue(FarmManualSources.entries.any { it.name.contains("Mahindra TIS") })
        assertTrue(FarmManualSources.entries.any { it.name.contains("Mahindra Tractor Service") })
        assertTrue(FarmManualSources.entries.any { it.name.contains("Swaraj Tractors") })
        assertTrue(FarmManualSources.entries.any { it.name.contains("John Deere Asia") })
    }

    @Test fun includesNorthEastFarmMachineryInstitute() {
        assertTrue(FarmManualSources.entries.any { it.name.contains("North Eastern Region") })
    }
}
