package com.vidya.safetyassistant.equipment

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTroubleshootingGuideTest {
    @Test fun includesAllRequestedHomeAndTechnicalCategories() {
        val ids = HomeTroubleshootingGuide.categories.map { it.id }.toSet()
        assertTrue(ids.containsAll(setOf("ac", "tv", "washer", "motor", "pump", "fridge", "generator", "microwave", "geyser", "tools")))
    }

    @Test fun everyCategoryHasAtLeastOneSymptom() {
        assertTrue(HomeTroubleshootingGuide.categories.all { it.symptoms.isNotEmpty() })
    }

    @Test fun microwaveSparkingIsStopWork() {
        val advice = HomeTroubleshootingGuide.advice("microwave", "sparks")
        assertNotNull(advice)
        assertTrue(advice!!.stopWork)
    }

    @Test fun ordinaryObservationAdviceIsNotPresentedAsConfirmedDiagnosis() {
        val advice = HomeTroubleshootingGuide.advice("washer", "not_draining")
        assertNotNull(advice)
        assertFalse(advice!!.stopWork)
        assertTrue(advice.warning.contains("not a diagnosis"))
    }

    @Test fun unknownIdsDoNotProduceAdvice() {
        assertTrue(HomeTroubleshootingGuide.advice("unknown", "unknown") == null)
    }
}
