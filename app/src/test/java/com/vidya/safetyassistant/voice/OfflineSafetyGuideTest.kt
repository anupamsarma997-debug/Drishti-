package com.vidya.safetyassistant.voice

import com.vidya.safetyassistant.safety.SafetyAssessment
import com.vidya.safetyassistant.safety.SafetyStatus
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineSafetyGuideTest {
    private fun assessment(status: SafetyStatus) = SafetyAssessment(
        status = status,
        stopRequired = status == SafetyStatus.STOP,
        voiceWarning = if (status == SafetyStatus.STOP) "Ruko! Danger." else null,
        reason = "test",
        evaluatedAtMillis = 1L
    )

    @Test fun stopAssessmentAlwaysReturnsStopWarning() {
        assertTrue(
            OfflineSafetyGuide.answer("What should I do?", assessment(SafetyStatus.STOP))
                .startsWith("Ruko!")
        )
    }

    @Test fun electricalQuestionDoesNotGiveLiveWiringInstructions() {
        val answer = OfflineSafetyGuide.answer("How do I connect this wire?", assessment(SafetyStatus.CAUTION))
        assertTrue(answer.contains("Do not touch wiring"))
    }

    @Test fun generalQuestionStatesCurrentLimit() {
        val answer = OfflineSafetyGuide.answer("What is this?", assessment(SafetyStatus.CAUTION))
        assertTrue(answer.contains("not confirmed safety") || answer.contains("confirm safety"))
    }
}
