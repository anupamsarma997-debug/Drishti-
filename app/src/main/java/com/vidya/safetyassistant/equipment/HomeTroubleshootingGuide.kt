package com.vidya.safetyassistant.equipment

data class ApplianceSymptom(
    val id: String,
    val label: String,
    val safeChecks: List<String>,
    val stopReason: String? = null
)

data class ApplianceCategory(
    val id: String,
    val label: String,
    val symptoms: List<ApplianceSymptom>
)

data class TroubleshootingAdvice(
    val title: String,
    val steps: List<String>,
    val stopWork: Boolean,
    val warning: String
)

/**
 * Conservative homeowner triage. It offers observation-only checks and escalation,
 * never instructions to open, probe, bypass, or repair energized equipment.
 */
object HomeTroubleshootingGuide {
    private val universalStop = listOf(
        "Smoke, sparks, burning smell, electric shock/tingling, exposed damaged wiring, or unusual heat",
        "Water touching electrical equipment or a wet area around a powered appliance"
    )

    val categories = listOf(
        ApplianceCategory("ac", "Air conditioner (AC)", listOf(
            ApplianceSymptom("no_cooling", "Not cooling", listOf("Check the displayed mode and set temperature without opening the unit.", "Check whether the remote batteries and display work.", "Look for an error code and note it exactly.", "If the user manual permits, inspect only the external air filter after the unit is switched off and safely isolated.")),
            ApplianceSymptom("leak", "Water leaking", listOf("Keep people away from wet electrical areas.", "Observe whether water is dripping from the indoor unit; do not remove covers.", "Record when the leak occurs and photograph the visible location."), "Water near wiring or a powered unit is an immediate stop condition."),
            ApplianceSymptom("noise", "Unusual noise", listOf("Switch the unit off using its normal control if safe.", "Note whether the sound comes from the indoor or outdoor unit.", "Do not reach through grilles or touch the fan."))
        )),
        ApplianceCategory("tv", "Television (TV)", listOf(
            ApplianceSymptom("no_picture", "Sound but no picture", listOf("Check input/source and brightness settings using the remote.", "Try a different known-working HDMI source or cable, if available.", "Note whether the screen briefly flashes or shows an error.")),
            ApplianceSymptom("no_power", "Not turning on", listOf("Check the wall outlet with a known-working low-risk device if the outlet is dry and undamaged.", "Check the remote batteries and the TV's standby indicator.", "Do not open the TV: internal capacitors can retain dangerous charge even after unplugging.")),
            ApplianceSymptom("lines", "Lines or flickering", listOf("Check whether the issue appears in the TV menu as well as external inputs.", "Record when flicker starts and whether it changes with the source.", "Do not press or dismantle the display panel."))
        )),
        ApplianceCategory("washer", "Washing machine", listOf(
            ApplianceSymptom("not_draining", "Not draining", listOf("Record the exact displayed error code.", "Check that the drain hose is not visibly kinked from the outside.", "Do not open a filter door if hot water may spill; follow the model manual.")),
            ApplianceSymptom("not_spinning", "Not spinning", listOf("Check whether the load is visibly unbalanced and follow the manual's redistribution guidance.", "Observe whether the door/lid lock indicator is active.", "Do not reach into a moving drum.")),
            ApplianceSymptom("leaking", "Water leaking", listOf("Stop the cycle using normal controls if safe.", "Keep water away from the plug and electrical outlet.", "Observe the leak location without moving or tilting the appliance."))
        )),
        ApplianceCategory("motor", "Electric motor", listOf(
            ApplianceSymptom("overheat", "Overheating", listOf("Stop operating and keep clear until fully cool.", "Record the motor nameplate details and operating conditions.", "Have a qualified technician inspect ventilation, load and electrical supply."), "Do not touch terminals, windings or attempt live measurements."),
            ApplianceSymptom("vibration", "Excessive vibration", listOf("Stop the motor before approaching.", "Observe whether the base or surrounding mount appears loose from a safe distance.", "Do not remove guards or touch rotating parts.")),
            ApplianceSymptom("not_start", "Not starting", listOf("Note any indicator, error code, hum or unusual smell from a safe distance.", "Check only the external control setting and visible supply switch position.", "Do not open the starter, capacitor box or terminal cover."))
        )),
        ApplianceCategory("pump", "Water pump", listOf(
            ApplianceSymptom("no_water", "No water delivery", listOf("Stop if the pump is running dry or making a harsh noise.", "Check visible water-source level and external valve position only if safe.", "Record pressure/flow symptoms; do not loosen pressurized fittings.")),
            ApplianceSymptom("noise", "Noise or vibration", listOf("Switch off using the normal control if safe.", "Keep clear of wet electrical areas.", "Note whether noise starts immediately or after running.")),
            ApplianceSymptom("leak", "Leakage", listOf("Keep clear if water is near electrical connections.", "Observe the leak location without tightening pressurized parts.", "Ask a technician to inspect seals and pipe connections."))
        )),
        ApplianceCategory("fridge", "Refrigerator", listOf(
            ApplianceSymptom("warm", "Not cooling", listOf("Check the temperature setting and door closure.", "Ensure external ventilation space is not blocked.", "Note any error code or unusual compressor noise; do not dismantle panels.")),
            ApplianceSymptom("ice", "Excess ice buildup", listOf("Record where ice forms and how quickly it returns.", "Check that the door seal appears clean and closes fully.", "Do not chip ice with sharp tools or use heat sources.")),
            ApplianceSymptom("noise", "Unusual noise", listOf("Note whether the sound comes from inside, behind or below.", "Check for objects touching the exterior cabinet.", "Stop and seek service if there is burning smell, smoke or overheating."))
        )),
        ApplianceCategory("generator", "Generator", listOf(
            ApplianceSymptom("not_start", "Not starting", listOf("Do not start it in a room, garage or enclosed space: carbon monoxide can be fatal.", "Only check visible fuel level and external control position outdoors, following the manual.", "Do not refuel while hot or running.")),
            ApplianceSymptom("power", "No output / unstable output", listOf("Disconnect sensitive loads only if it is safe and follow the manual.", "Record indicator lights and displayed warnings.", "Never touch output sockets, terminals or internal wiring for testing.")),
            ApplianceSymptom("smoke", "Smoke or exhaust issue", listOf("Stop use if safe, move people away and get to fresh air.", "Never operate indoors or near windows/doors.", "Seek qualified service before restarting."), "Exhaust exposure, smoke or fuel leak requires immediate stop.")
        )),
        ApplianceCategory("microwave", "Microwave oven", listOf(
            ApplianceSymptom("not_heat", "Runs but does not heat", listOf("Check the selected power level and timer.", "Try only a microwave-safe cup of water if the door, seal and unit are undamaged and the manual permits.", "Do not remove the cover: microwave circuits can retain lethal high voltage.")),
            ApplianceSymptom("sparks", "Sparks inside", listOf("Stop the cycle immediately.", "If safe, unplug only after stopping and with dry hands; otherwise keep clear.", "Do not restart until the cause is checked and the interior is inspected by a qualified technician."), "Sparking, burning smell or damaged door/seal means stop use."),
            ApplianceSymptom("door", "Door or latch damaged", listOf("Do not operate the microwave.", "Keep the door closed and arrange qualified service.", "Never bypass an interlock or safety switch."), "A damaged door or interlock can expose users to microwave energy.")
        )),
        ApplianceCategory("geyser", "Water heater / geyser", listOf(
            ApplianceSymptom("no_hot", "No hot water", listOf("Check the external temperature control and indicator only.", "Note any error light or unusual sound.", "Do not open the electrical cover or touch water near exposed wiring.")),
            ApplianceSymptom("leak", "Water leaking", listOf("Keep away if water is near electrical parts.", "Stop use using the normal external control if safe.", "Do not tighten fittings on a hot or pressurized tank.")),
            ApplianceSymptom("shock", "Tingling or shock from water/body", listOf("Stop using the heater immediately and keep everyone away from taps and wet surfaces.", "Do not touch the appliance, plug, switch or water to disconnect it.", "Call emergency services if anyone is injured and a qualified electrician urgently."), "Any shock or tingling is an emergency stop condition.")
        )),
        ApplianceCategory("tools", "Power tools", listOf(
            ApplianceSymptom("not_working", "Not working", listOf("Check the external switch and visible battery charge indicator.", "Inspect the outside of the cord or casing for damage without touching exposed conductors.", "Do not remove guards or open the tool.")),
            ApplianceSymptom("overheat", "Overheating or smell", listOf("Stop and disconnect only if safe, then let it cool away from flammable materials.", "Do not restart until inspected.", "Keep hands away from blades, bits and moving parts.")),
            ApplianceSymptom("vibration", "Unexpected vibration", listOf("Stop the tool and wait for all moving parts to stop.", "Check the accessory visually only after safe isolation and according to the manual.", "Do not use a damaged or unbalanced accessory."))
        ))
    )

    fun advice(categoryId: String, symptomId: String): TroubleshootingAdvice? {
        val category = categories.firstOrNull { it.id == categoryId } ?: return null
        val symptom = category.symptoms.firstOrNull { it.id == symptomId } ?: return null
        val warning = symptom.stopReason
            ?: "These are observation-only checks, not a diagnosis. If the issue continues, use the exact manufacturer's manual or call a qualified technician."
        val stop = symptom.stopReason != null
        return TroubleshootingAdvice(
            title = "${category.label}: ${symptom.label}",
            steps = symptom.safeChecks,
            stopWork = stop,
            warning = warning
        )
    }

    fun universalHazardWarning(): String =
        "STOP immediately for smoke, sparks, burning smell, shock/tingling, exposed damaged wiring, unusual heat, fuel/gas leak, or water near electrical parts. Move away. Isolate power only from a safe, dry, accessible control; never touch the appliance or wet switch. Call a qualified technician or emergency services as appropriate."
}
