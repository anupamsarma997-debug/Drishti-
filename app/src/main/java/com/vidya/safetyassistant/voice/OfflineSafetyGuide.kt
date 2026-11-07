package com.vidya.safetyassistant.voice

import com.vidya.safetyassistant.safety.SafetyAssessment
import com.vidya.safetyassistant.safety.SafetyStatus
import java.util.Locale

/**
 * Small deterministic offline safety FAQ. This is deliberately not presented as a generative
 * AI or a substitute for a manufacturer's manual or qualified technician.
 */
object OfflineSafetyGuide {
    fun answer(question: String, assessment: SafetyAssessment): String {
        val q = question.lowercase(Locale.ROOT)
        val hindi = q.any { it in "अआइईउऊएऐओऔकखगघचछजझटठडढतथदधनपफबभमयरलवशषसह" } ||
            listOf("kya", "kaise", "kyun", "kahan", "ruko", "bijli", "wire", "machine")
                .any(q::contains)

        if (assessment.status == SafetyStatus.STOP) {
            return assessment.voiceWarning
                ?: if (hindi) "Rukiye. Jagah se door rahein aur qualified technician se madad lein."
                else "Stop work. Keep away from the equipment and contact a qualified professional."
        }

        val electrical = listOf("wire", "wiring", "electric", "voltage", "bijli", "current", "terminal")
            .any(q::contains)
        val moving = listOf("machine", "motor", "moving", "engine", "tool", "bike")
            .any(q::contains)

        return when {
            electrical && hindi ->
                "Live wiring ya electrical panel ko bilkul touch na karein. Power isolation aur absence-of-voltage verification qualified electrician hi kare."
            electrical ->
                "Do not touch wiring or an electrical panel. A qualified electrician must isolate power and verify absence of voltage."
            moving && hindi ->
                "Chalti machine ya engine ke paas kaam na karein. Machine ko safe tareeke se isolate karne ke liye manufacturer manual aur trained technician ki madad lein."
            moving ->
                "Do not work near moving machinery or a running engine. Follow the manufacturer's isolation procedure and use a trained technician."
            assessment.status == SafetyStatus.CAUTION && hindi ->
                "Camera se safety confirm nahi hui hai. Kaam rok kar manual aur qualified technician se verify karein."
            assessment.status == SafetyStatus.CAUTION ->
                "The camera has not confirmed safety. Pause the task and verify with the manufacturer's manual or a qualified technician."
            hindi ->
                "Main abhi camera se technical fault confirm nahi kar sakta. Equipment ka model number aur verified manual check karein. Live electrical ya moving parts ko touch na karein."
            else ->
                "I cannot confirm a technical fault from the camera alone yet. Check the exact model and verified manual; never touch live electrical or moving parts."
        }
    }
}
