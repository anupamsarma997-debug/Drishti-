package com.vidya.safetyassistant.manuals

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualIndexStorageKeyTest {
    @Test
    fun sameUriProducesStableKey() {
        assertEquals(manualIndexStorageKey("content://provider/manual.pdf"),
            manualIndexStorageKey("content://provider/manual.pdf"))
    }

    @Test
    fun differentUrisProduceDifferentKeys() {
        assertNotEquals(manualIndexStorageKey("content://provider/a.pdf"),
            manualIndexStorageKey("content://provider/b.pdf"))
    }

    @Test
    fun keyUsesVersionedSha256Namespace() {
        assertTrue(manualIndexStorageKey("content://provider/manual.pdf").startsWith("manual_v2_"))
        assertEquals(64, manualIndexStorageKey("content://provider/manual.pdf").removePrefix("manual_v2_").length)
    }

    @Test
    fun legacyKeyRemainsAvailableForMigration() {
        assertTrue(legacyManualIndexStorageKey("content://provider/manual.pdf").startsWith("manual_"))
    }
}
