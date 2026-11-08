package com.vidya.safetyassistant.equipment

data class VehicleDiagnosticResult(
    val code: String,
    val title: String,
    val meaning: String,
    val possibleCauses: List<String>,
    val safeChecks: List<String>,
    val severity: String,
    val manufacturerSpecific: Boolean,
    val limitation: String
)

/**
 * Small, explicit starter dictionary for common generic OBD-II DTCs.
 * It is not a complete database and does not decode proprietary manufacturer codes.
 */
object VehicleDiagnosticLookup {
    private val genericCodes = mapOf(
        "P0101" to VehicleDiagnosticResult("P0101", "Mass/volume air-flow circuit range/performance",
            "The engine control unit detected an air-flow measurement outside its expected range.",
            listOf("Air filter or intake path restriction", "Air leak or sensor wiring/connector issue", "Air-flow sensor fault"),
            listOf("Park safely and switch the engine off before looking under the bonnet.", "Check the owner's manual for the exact air-filter inspection procedure.", "Do not unplug sensors or replace parts based only on this code."),
            "Service soon", false, "A code points to a monitored system; it does not identify one failed part."),
        "P0171" to VehicleDiagnosticResult("P0171", "System too lean — Bank 1",
            "The engine computer detected a lean air/fuel condition on bank 1.",
            listOf("Intake/vacuum leak", "Fuel delivery issue", "Air-flow or oxygen-sensor signal issue"),
            listOf("If the engine runs roughly, stalls, or the warning flashes, stop driving and arrange assistance.", "Record other stored codes before clearing anything.", "Have a qualified technician perform the model-specific checks."),
            "Prompt inspection", false, "Possible causes vary by engine and calibration."),
        "P0300" to VehicleDiagnosticResult("P0300", "Random/multiple-cylinder misfire detected",
            "The engine computer detected misfires on multiple cylinders or at random.",
            listOf("Ignition, fuel, or air-intake issue", "Mechanical engine issue", "Poor fuel or wiring fault"),
            listOf("If the check-engine light is flashing, stop driving when safe; catalytic-converter damage may occur.", "Do not continue if the engine shakes severely, loses power, or smells of fuel.", "Arrange professional diagnosis with freeze-frame data."),
            "Potentially urgent", false, "The code does not tell which component has failed."),
        "P0420" to VehicleDiagnosticResult("P0420", "Catalyst system efficiency below threshold — Bank 1",
            "The monitored catalyst system is not meeting its expected efficiency threshold.",
            listOf("Catalyst deterioration", "Exhaust leak", "An upstream engine/fuel/ignition issue affecting catalyst readings"),
            listOf("Check for additional engine codes and symptoms.", "Do not remove or bypass emissions equipment.", "Ask a qualified technician to verify exhaust leaks and sensor data."),
            "Service soon", false, "A catalytic converter should not be replaced from this code alone."),
        "P0128" to VehicleDiagnosticResult("P0128", "Coolant thermostat temperature below regulating threshold",
            "The engine did not reach the expected operating temperature within the monitored conditions.",
            listOf("Thermostat stuck open", "Coolant temperature sensor or wiring issue", "Low coolant or cooling-system issue"),
            listOf("Never open a hot coolant cap or touch hot cooling components.", "When fully cold, check the coolant level only as described in the owner's manual.", "Stop if there is overheating, steam, or a coolant leak."),
            "Service soon", false, "Cooling-system layout and procedures differ by model."),
        "P0455" to VehicleDiagnosticResult("P0455", "Evaporative-emissions system gross leak detected",
            "The vehicle detected a large leak in the fuel-vapour emissions system.",
            listOf("Fuel cap not sealed correctly on vehicles with a serviceable cap", "EVAP hose or seal leak", "Purge/vent system fault"),
            listOf("If there is a strong fuel smell or visible leak, stop, move away from ignition sources and seek assistance.", "Only check an accessible fuel cap if the owner's manual describes it.", "Do not inspect fuel lines or vapour components yourself."),
            "Prompt inspection", false, "Some vehicles use capless fuel systems; follow the exact manual."),
        "P0500" to VehicleDiagnosticResult("P0500", "Vehicle speed sensor malfunction",
            "The control module detected an abnormal vehicle-speed signal.",
            listOf("Wheel/vehicle speed sensor or wiring issue", "Tone ring or connector issue", "Network or control-module issue"),
            listOf("If speedometer, ABS, or stability warnings appear together, drive only if the vehicle can be stopped safely; otherwise arrange recovery.", "Do not work near wheels or underneath a vehicle supported only by a jack.", "Have the exact ABS/ECU codes read by a qualified technician."),
            "Prompt inspection", false, "The sensor location and code interpretation depend on vehicle architecture."),
        "U0100" to VehicleDiagnosticResult("U0100", "Lost communication with ECM/PCM",
            "A control module reported that it lost communication with the engine/powertrain control module.",
            listOf("Low battery voltage", "Power/ground or wiring issue", "CAN network or module issue"),
            listOf("If the vehicle stalls or cannot be controlled normally, stop safely and arrange recovery.", "Record all module codes; do not replace a control module from this code alone.", "Use a qualified technician for network and electrical diagnosis."),
            "Potentially urgent", false, "Network codes need the full vehicle scan and manufacturer service information.")
    )

    fun lookup(
        vehicleType: String,
        manufacturer: String,
        model: String,
        year: String,
        rawCode: String
    ): VehicleDiagnosticResult? {
        if (vehicleType.isBlank() || manufacturer.isBlank() || model.isBlank() || year.isBlank()) return null
        val code = rawCode.trim().uppercase().replace(" ", "")
        if (!Regex("[PBCU][0-9A-F]{4}").matches(code)) {
            return VehicleDiagnosticResult(code, "Code format not recognized",
                "Enter a five-character diagnostic trouble code such as P0300, or enter the exact dashboard message.",
                emptyList(), listOf("Recheck every character on the scan tool or dashboard.", "Do not guess a code from a warning-light symbol."),
                "Unverified", false, "This lookup accepts standard five-character OBD-style DTCs only.")
        }
        val known = genericCodes[code]
        if (known != null) return known.copy(
            limitation = "Generic OBD-II reference only. Confirm that this vehicle/year and control module support this code definition. $manufacturer $model ($year) compatibility has not been verified."
        )
        // SAE J2012 allocates the 1xxx/2xxx ranges differently by DTC family:
        // P1 and P3 are manufacturer-controlled (P3 also includes reserved space);
        // B/C/U use 1xxx and 2xxx for manufacturer-controlled codes, while 3xxx
        // is reserved. Unknown/reserved codes still remain unverified below.
        val manufacturerSpecific = when (code[0]) {
            'P' -> code[1] == '1' || code[1] == '3'
            'B', 'C', 'U' -> code[1] == '1' || code[1] == '2'
            else -> false
        }
        return VehicleDiagnosticResult(
            code,
            if (manufacturerSpecific) "Manufacturer-specific code — exact lookup required" else "Code not in Vidya's current reference",
            if (manufacturerSpecific) "This code may use a manufacturer-defined meaning. The same code can mean different things across brands, models, years, and control modules." else "Vidya does not have a verified definition for this code yet.",
            emptyList(),
            listOf("Confirm vehicle type, full model/variant, model year and the module that reported the code.", "Check the official service manual or ask an authorized/qualified technician.", "Do not clear the code or replace parts before recording all codes and freeze-frame information."),
            "Unverified — do not act on a guess",
            manufacturerSpecific,
            "No manufacturer-specific database is bundled. A brand/model match must be verified against the official manual."
        )
    }
}
