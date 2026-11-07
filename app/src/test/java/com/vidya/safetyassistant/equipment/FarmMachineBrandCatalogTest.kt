package com.vidya.safetyassistant.equipment

import org.junit.Assert.*
import org.junit.Test

class FarmMachineBrandCatalogTest {
    @Test fun tractorSuggestionsIncludeMajorIndiaMarketMakes() {
        val brands = FarmMachineBrandCatalog.brandsForCategory("tractor")
        listOf("Mahindra", "Swaraj", "Sonalika", "TAFE", "John Deere", "New Holland")
            .forEach { assertTrue("Missing $it", brands.contains(it)) }
    }

    @Test fun suggestionsAreCategorySpecificAndCaseInsensitive() {
        assertTrue(FarmMachineBrandCatalog.suggestions("tractor", "mah").contains("Mahindra"))
        assertFalse(FarmMachineBrandCatalog.suggestions("tractor", "mah").contains("Honda"))
        assertTrue(FarmMachineBrandCatalog.suggestions("pump", "KIR").contains("Kirloskar"))
    }

    @Test fun unknownCategoryDoesNotInventBrands() {
        assertTrue(FarmMachineBrandCatalog.brandsForCategory("unknown").isEmpty())
        assertTrue(FarmMachineBrandCatalog.suggestions("unknown", "").isEmpty())
    }

    @Test fun userCanStillEnterAnyCustomBrand() {
        // Suggestions are optional; the UI's text field remains free text.
        assertTrue(FarmMachineBrandCatalog.suggestions("tractor", "My local make").isEmpty())
    }
}
