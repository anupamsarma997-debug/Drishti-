package com.vidya.safetyassistant.safety

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SafetyController(context: Context, private val engine: SafetyEngine) {
    private val appContext = context.applicationContext
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var assessment by mutableStateOf(
        SafetyAssessment(
            status = SafetyStatus.CAUTION,
            stopRequired = false,
            voiceWarning = null,
            reason = "Point the camera at equipment. No automated AI detector is connected yet.",
            evaluatedAtMillis = System.currentTimeMillis()
        )
    )
        private set

    fun evaluate(frame: FrameClassification): SafetyAssessment {
        assessment = engine.evaluate(frame)
        if (assessment.stopRequired) triggerStopAlert()
        return assessment
    }

    fun setManualTestState(safe: Boolean) {
        val now = System.currentTimeMillis()
        val hazards = if (safe) emptySet() else setOf(HazardType.EXPOSED_CONDUCTOR)
        val frame = FrameClassification(
            timestampMillis = now,
            equipmentLabel = "Manual test",
            hazards = hazards,
            confidence = 0.99f,
            context = Context6D(
                timeValid = true,
                roiValid = true,
                contentValid = true,
                actionPermitted = safe,
                logicConsistent = true,
                predictiveSafetyValid = safe
            )
        )
        evaluate(frame)
    }

    private fun triggerStopAlert() {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 250, 120, 450), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 250, 120, 450), -1)
                }
            }
        } catch (_: SecurityException) {
            // Vibration is an optional alert; the visual and spoken warning remain active.
        }
    }
}
