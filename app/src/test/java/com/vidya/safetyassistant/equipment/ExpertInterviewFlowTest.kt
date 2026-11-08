package com.vidya.safetyassistant.equipment

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpertInterviewFlowTest {
    @Test
    fun asksTargetedQuestionForNoise() {
        val question = ExpertInterviewFlow.nextQuestion("bike", "engine noise", 0)
        assertTrue(question?.contains("noise") == true)
    }

    @Test
    fun asksForExactCodeWhenWarningReported() {
        val question = ExpertInterviewFlow.nextQuestion("car", "check engine warning light", 0)
        assertTrue(question?.contains("exact displayed error code") == true)
    }

    @Test
    fun stopsInterviewForUrgentHazard() {
        assertNull(ExpertInterviewFlow.nextQuestion("motor", "smoke and burning smell", 0))
    }

    @Test
    fun stopsInterviewForBrakeFailureAndFuelLeakPhrases() {
        assertNull(ExpertInterviewFlow.nextQuestion("car", "brake pedal goes to floor", 0))
        assertNull(ExpertInterviewFlow.nextQuestion("tractor", "diesel leak", 0))
        assertNull(ExpertInterviewFlow.nextQuestion("motor", "battery swollen", 0))
    }

    @Test
    fun limitsNumberOfFollowUpRounds() {
        assertNull(ExpertInterviewFlow.nextQuestion("car", "engine vibration", 3))
    }
}
