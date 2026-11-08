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


    @Test fun officialIndianOemManualPortalsAreIndexedWithoutAssumingReuseRights() {
        val ids = listOf(
            "hero-india-owner-manuals",
            "honda2wheelers-india-owner-manuals",
            "honda-motopub-india-service-publications",
            "tvs-india-user-manuals",
            "bajaj-india-owner-manuals"
        )
        ids.forEach { id ->
            val source = KnowledgeSourceCatalog.find(id)
            assertNotNull(source)
            assertFalse(source!!.commercialUseAllowed)
            assertFalse(source.redistributionAllowed)
            assertEquals(KnowledgeVerification.SOURCE_CHECKED, source.verification)
        }
    }

    @Test fun commercialImageDatasetRightsAreDistinguishedFromNonCommercialBenchmarks() {
        val cc0 = KnowledgeSourceCatalog.find("electronics-parts-cc0")
        val shareAlike = KnowledgeSourceCatalog.find("irisp-industrial-spare-parts")
        val workpieces = KnowledgeSourceCatalog.find("machined-workpieces-defect-images")
        val mvtec = KnowledgeSourceCatalog.find("mvtec-ad-industrial-anomaly")
        val autovi = KnowledgeSourceCatalog.find("autovi-renault-industrial-visual-inspection")
        assertTrue(cc0!!.commercialUseAllowed)
        assertTrue(shareAlike!!.commercialUseAllowed)
        assertTrue(shareAlike.license.contains("ShareAlike"))
        assertTrue(workpieces!!.commercialUseAllowed)
        assertFalse(mvtec!!.commercialUseAllowed)
        assertFalse(autovi!!.commercialUseAllowed)
    }

    @Test fun officialPartsPortalsDoNotClaimReuseRights() {
        listOf("honda-genuine-parts-india", "hero-genuine-parts-india", "bajaj-genuine-parts-india").forEach { id ->
            val source = KnowledgeSourceCatalog.find(id)
            assertNotNull(source)
            assertFalse(source!!.commercialUseAllowed)
            assertFalse(source.redistributionAllowed)
        }
    }

    @Test fun unknownSourceDoesNotResolve() {
        assertEquals(null, KnowledgeSourceCatalog.find("made-up-source"))
    }
}
