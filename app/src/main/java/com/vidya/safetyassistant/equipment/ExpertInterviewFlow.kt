package com.vidya.safetyassistant.equipment

/**
 * Small offline follow-up interviewer. Questions collect observations only;
 * they do not authorise dismantling, live testing or repair.
 */
object ExpertInterviewFlow {
    fun nextQuestion(machineId: String, complaint: String, round: Int): String? {
        val text = complaint.trim().lowercase()
        if (text.isBlank() || round !in 0..2) return null

        val urgent = listOf(
            "smoke", "fire", "spark", "burning smell", "electric shock", "shock",
            "fuel leak", "gas leak", "brake fail", "brake failure", "battery swelling",
            "battery hot", "orange cable"
        )
        if (urgent.any { it in text }) return null

        if (round == 0) {
            return when {
                listOf("noise", "sound", "rattle", "grinding", "vibration").any { it in text } ->
                    "When does the noise happen: at startup, while running, or only under load? Observe from a safe distance."
                listOf("start", "starting", "crank", "click", "not start").any { it in text } ->
                    "When you try to start it, do you hear a click, cranking, humming, or nothing? Do not bridge terminals."
                listOf("leak", "drip", "oil", "fuel", "water").any { it in text } ->
                    "Where is the leak visibly located? Do not touch the fluid or loosen any pipe."
                listOf("error", "code", "warning", "light", "blink").any { it in text } ->
                    "What is the exact displayed error code or warning-light symbol? Please copy it exactly."
                else ->
                    "When does the problem occur, and what is the first unusual thing you notice?"
            }
        }
        if (round == 1) {
            return "Does it happen continuously, only after warming up, or only when the machine is under load?"
        }
        return "Did this begin after recent maintenance, a repair, a fall/impact, water exposure, or a change in fuel/power supply?"
    }
}
