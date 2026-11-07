package com.vidya.safetyassistant.equipment

data class VehicleWarningGuide(
    val id: String,
    val label: String,
    val meaning: String,
    val response: String,
    val urgency: String,
    val caveat: String
)

/** General dashboard indicator explanations; symbol shape and action must be confirmed in the exact owner's manual. */
object VehicleWarningLightGuide {
    val entries = listOf(
        VehicleWarningGuide("oil_pressure", "Red oil pressure (oil can)", "The engine may not be receiving adequate oil pressure. This is not the same as a routine oil-change reminder.", "Stop in a safe place as soon as possible and switch the engine off. Do not restart to test it; arrange qualified assistance.", "STOP SAFELY", "Symbol colour and exact meaning vary by model. Check the owner's manual."),
        VehicleWarningGuide("coolant_temp", "Engine temperature / coolant", "The engine may be overheating or the cooling system may have a fault.", "Stop safely, switch off if instructed by the manual, keep clear of steam and hot parts. Never open a hot coolant cap. Arrange assistance.", "STOP SAFELY", "Do not add cold liquid to a hot system or touch hot components."),
        VehicleWarningGuide("brake", "Brake system warning", "May indicate parking brake applied, low brake fluid, or a brake-system fault depending on vehicle.", "If the warning remains after the parking brake is fully released, or braking feels abnormal, stop safely and arrange recovery. Do not continue driving.", "STOP SAFELY", "Exact indicator logic varies; ABS and brake warnings together can indicate reduced braking/stability functions."),
        VehicleWarningGuide("battery", "Battery / charging system", "The charging system may not be charging the 12-volt battery while the engine is running.", "Reduce unnecessary electrical loads and stop at a safe location. If steering, engine or other systems are affected, stop and arrange help.", "PROMPT ACTION", "Hybrid/EV high-voltage systems are different; never touch orange cables."),
        VehicleWarningGuide("check_engine", "Check engine / MIL", "The engine or emissions control system has detected a fault.", "If the light flashes, the engine runs roughly, loses power, overheats or smells of fuel, stop safely and seek assistance. If steady and the vehicle behaves normally, consult the manual and arrange prompt diagnosis.", "DEPENDS ON SYMPTOMS", "A lamp alone does not identify the failed component; read all diagnostic codes."),
        VehicleWarningGuide("abs", "ABS warning", "Anti-lock braking may be unavailable; conventional braking may remain, depending on the fault.", "Drive only if braking feels normal and the owner's manual permits it; use extra caution and arrange prompt service. If brake warning is also on or braking is abnormal, stop safely.", "PROMPT INSPECTION", "Do not assume ABS is functioning because the pedal still works."),
        VehicleWarningGuide("airbag", "Airbag / SRS warning", "The supplemental restraint system may not work as designed in a crash.", "Arrange prompt inspection by a qualified technician. Do not probe SRS wiring, connectors or airbag modules.", "PROMPT INSPECTION", "SRS components can deploy unexpectedly; do not attempt DIY repairs."),
        VehicleWarningGuide("tyre_pressure", "Tyre pressure / TPMS", "One or more tyres may be under-inflated, or the monitoring system may have a fault.", "Slow down, avoid sharp steering/braking and stop somewhere safe to visually inspect tyres. Check pressure when safe using the vehicle's specified cold pressure. Do not drive on a visibly damaged or flat tyre.", "CHECK PROMPTLY", "A TPMS warning does not reveal which tyre or prove that a tyre is safe."),
        VehicleWarningGuide("ev_battery", "EV / hybrid battery or powertrain warning", "The traction battery or electric drive system may have detected a fault.", "Follow the exact owner's manual. If there is smoke, unusual heat, hissing, impact damage or a burning smell, move away and contact emergency services. Do not charge or touch damaged high-voltage equipment.", "STOP / SPECIALIST", "Never open battery enclosures or touch orange high-voltage cables."),
        VehicleWarningGuide("fuel", "Low fuel", "The fuel level is near the vehicle's reserve threshold.", "Refuel at a suitable station. Do not rely on a fixed remaining-distance estimate.", "REFUEL SOON", "Range varies with driving conditions and vehicle calibration.")
    )

    fun find(id: String): VehicleWarningGuide? = entries.firstOrNull { it.id == id }
}
