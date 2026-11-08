package com.vidya.safetyassistant.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeSourceCatalogTest {
    @Test fun sourceIdsAreUniqueAndTraceable() {
        assertEquals(KnowledgeSourceCatalog.sources.size,
            KnowledgeSourceCatalog.sources.map { it.sourceId }.toSet().size)
        assertTrue(KnowledgeSourceCatalog.sources.all { it.hasTraceableIdentity })
    }

    @Test fun nonCommercialSoundDatasetIsNotCommerciallyCleared() {
        val mimii = KnowledgeSourceCatalog.find("mimii-dg")
        assertNotNull(mimii)
        assertFalse(mimii!!.commercialUseAllowed)
    }

    @Test fun obdexDataHasSeparateCc0DataAndMitToolingNotice() {
        val obdex = KnowledgeSourceCatalog.find("obdex-generic-obd")
        assertNotNull(obdex)
        assertTrue(obdex!!.commercialUseAllowed)
        assertTrue(obdex.redistributionAllowed)
        assertTrue(obdex.license.contains("CC0"))
        assertTrue(obdex.license.contains("MIT"))
    }

    @Test fun unknownSourceDoesNotResolve() {
        assertEquals(null, KnowledgeSourceCatalog.find("made-up-source"))
    }
}
