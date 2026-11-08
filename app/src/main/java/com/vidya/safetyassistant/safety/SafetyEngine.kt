package com.vidya.safetyassistant.safety

/**
 * Deterministic safety policy layer. It consumes classifications from a future on-device
 * model or from the manual test control; it does not perform computer vision itself.
 */
enum class SafetyStatus { SAFE, CAUTION, STOP }

enum class HazardType {
    INCORRECT_WIRE_CONNECTION,
    HIGH_VOLTAGE_PROXIMITY,
    DANGEROUS_TOOL_ANGLE,
    EXPOSED_CONDUCTOR,
    SMOKE_OR_FIRE,
    MOVING_MACHINERY,
    UNKNOWN_HIGH_RISK
}

data class Context6D(
    val timeValid: Boolean,
    val roiValid: Boolean,
    val contentValid: Boolean,
    val actionPermitted: Boolean,
    val logicConsistent: Boolean,
    val predictiveSafetyValid: Boolean
)

data class FrameClassification(
    val timestampMillis: Long,
    val equipmentLabel: String,
    val hazards: Set<HazardType> = emptySet(),
    val confidence: Float = 0f,
    val context: Context6D
)

data class SafetyAssessment(
    val status: SafetyStatus,
    val stopRequired: Boolean,
    val voiceWarning: String?,
    val reason: String,
    val evaluatedAtMillis: Long
)

class SafetyEngine(
    private val maxFrameAgeMillis: Long = 2_000L,
    private val minimumConfidence: Float = 0.70f
) {
    fun evaluate(frame: FrameClassification, nowMillis: Long = System.currentTimeMillis()): SafetyAssessment {
        val age = nowMillis - frame.timestampMillis
        val stale = age < 0L || age > maxFrameAgeMillis
        val confidenceInvalid = !frame.confidence.isFinite() ||
            frame.confidence !in minimumConfidence..1.0f
        val c = frame.context
        val equipmentLabel = frame.equipmentLabel.trim()
        val equipmentIdentityInvalid = equipmentLabel.isEmpty() ||
            equipmentLabel.equals("unknown", ignoreCase = true) ||
            equipmentLabel.equals("unidentified", ignoreCase = true)

        val hardStopHazard = frame.hazards.firstOrNull { it in HARD_STOP_HAZARDS }
        if (hardStopHazard != null) {
            return stopAssessment(hardStopHazard, nowMillis)
        }

        if (stale || confidenceInvalid || equipmentIdentityInvalid || !c.timeValid || !c.roiValid ||
            !c.contentValid || !c.actionPermitted || !c.logicConsistent ||
            !c.predictiveSafetyValid
        ) {
            return SafetyAssessment(
                status = SafetyStatus.CAUTION,
                stopRequired = false,
                voiceWarning = null,
                reason = "Verification required. Camera analysis cannot confirm that this equipment is safe.",
                evaluatedAtMillis = nowMillis
            )
        }

        // An empty hazard list means only that this frame produced no configured flag.
        // It is not proof that the machine or work area is safe. Keep the user in CAUTION
        // until a validated, equipment-specific hazard assessment exists.
        return SafetyAssessment(
            status = SafetyStatus.CAUTION,
            stopRequired = false,
            voiceWarning = null,
            reason = "No configured hazard was flagged, but camera analysis cannot prove safety. Verify the machine is isolated and consult its exact manual.",
            evaluatedAtMillis = nowMillis
        )
    }

    private fun stopAssessment(hazard: HazardType, now: Long) = SafetyAssessment(
        status = SafetyStatus.STOP,
        stopRequired = true,
        voiceWarning = when (hazard) {
            HazardType.INCORRECT_WIRE_CONNECTION -> "Ruko! Yeh terminal galat ho sakta hai. Wiring ko touch na karein; qualified electrician ko bulayein."
            HazardType.HIGH_VOLTAGE_PROXIMITY -> "Ruko! High voltage ka khatra ho sakta hai. Door rahein aur qualified electrician ko bulayein."
            HazardType.DANGEROUS_TOOL_ANGLE -> "Ruko! Tool ki position unsafe ho sakti hai. Machine band hone tak door rahein."
            HazardType.EXPOSED_CONDUCTOR -> "Ruko! Exposed wire ka khatra hai. Use touch na karein aur qualified electrician ko bulayein."
            HazardType.SMOKE_OR_FIRE -> "Ruko! Smoke ya fire ka risk hai. Area se door ho jaayein aur emergency help lein."
            HazardType.MOVING_MACHINERY -> "Ruko! Machine chal rahi ho sakti hai. Moving parts se door rahein."
            HazardType.UNKNOWN_HIGH_RISK -> "Ruko! Risk clear nahi hai. Kaam band karein aur qualified technician se jaanch karayein."
        },
        reason = "Potential hazard flagged: $hazard. The app has stopped its guidance workflow.",
        evaluatedAtMillis = now
    )

    companion object {
        private val HARD_STOP_HAZARDS = setOf(
            HazardType.INCORRECT_WIRE_CONNECTION,
            HazardType.HIGH_VOLTAGE_PROXIMITY,
            HazardType.DANGEROUS_TOOL_ANGLE,
            HazardType.EXPOSED_CONDUCTOR,
            HazardType.SMOKE_OR_FIRE,
            HazardType.MOVING_MACHINERY,
            HazardType.UNKNOWN_HIGH_RISK
        )
    }
}
