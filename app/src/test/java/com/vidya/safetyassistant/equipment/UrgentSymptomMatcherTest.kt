package com.vidya.safetyassistant.equipment

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrgentSymptomMatcherTest {
    @Test fun recognizesEnglishHazards() {
        assertTrue(UrgentSymptomMatcher.isUrgent("brake pedal goes to floor"))
        assertTrue(UrgentSymptomMatcher.isUrgent("diesel leak"))
        assertTrue(UrgentSymptomMatcher.isUrgent("battery swollen"))
    }

    @Test fun recognizesCommonRomanHindiHazards() {
        assertTrue(UrgentSymptomMatcher.isUrgent("brake nahi lag raha"))
        assertTrue(UrgentSymptomMatcher.isUrgent("diesel leak ho raha hai"))
        assertTrue(UrgentSymptomMatcher.isUrgent("battery phool gaya"))
        assertTrue(UrgentSymptomMatcher.isUrgent("bijli ka jhatka lag raha"))
        assertTrue(UrgentSymptomMatcher.isUrgent("machine se dhua nikal raha"))
        assertTrue(UrgentSymptomMatcher.isUrgent("jalne ki badbu aa rahi"))
    }

    @Test fun ordinaryShockAbsorberMentionIsNotMistakenForElectricShock() {
        assertFalse(UrgentSymptomMatcher.isUrgent("rear shock absorber makes a noise"))
        assertFalse(UrgentSymptomMatcher.isUrgent("technician replaced the blade yesterday"))
        assertFalse(UrgentSymptomMatcher.isUrgent("machine is jammed in the menu"))
    }

    @Test fun wordBoundariesAvoidPartNameAndTechnicalTermFalsePositives() {
        assertFalse(UrgentSymptomMatcher.isUrgent("firewall error on controller"))
        assertFalse(UrgentSymptomMatcher.isUrgent("replace spark plug during service"))
        assertTrue(UrgentSymptomMatcher.isUrgent("visible spark near exposed wire"))
        assertTrue(UrgentSymptomMatcher.isUrgent("smoke coming from engine"))
        assertTrue(UrgentSymptomMatcher.isUrgent("fire from motor"))
        assertTrue(UrgentSymptomMatcher.isUrgent("fire coming from motor"))
    }

    @Test fun punctuationAndCaseDoNotHideUrgentPhrases() {
        assertTrue(UrgentSymptomMatcher.isUrgent("BRAKE-NAHI LAG RAHA"))
        assertTrue(UrgentSymptomMatcher.isUrgent("DIESEL, LEAK!"))
    }

    @Test fun ordinaryComplaintsDoNotMatchUrgentList() {
        assertFalse(UrgentSymptomMatcher.isUrgent("engine vibration at idle"))
        assertFalse(UrgentSymptomMatcher.isUrgent("water flow is low"))
        assertFalse(UrgentSymptomMatcher.isUrgent(""))
    }
}
