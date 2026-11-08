package com.vidya.safetyassistant.equipment

data class MachineExpert(
    val id: String,
    val name: String,
    val machineIds: Set<String>,
    val focus: String,
    val knowledgeBoundary: String
)

data class ExpertAssessment(
    val expert: MachineExpert,
    val summary: String,
    val possibleAreas: List<String>,
    val safeChecks: List<String>,
    val stopConditions: List<String>,
    val escalation: String,
    val confidenceNote: String
)

/**
 * Offline specialist router and conservative triage engine.
 * This is a deterministic knowledge-based system, not a trained generative model.
 * It never confirms a failed part or supplies hazardous dismantling instructions.
 */
object MachineExpertSystem {
    val experts = listOf(
        MachineExpert("vehicle", "Bike & Car Expert",
            setOf("bike", "scooter", "car", "ev", "truck"),
            "Starting, engine, drivetrain, tyres, brakes, dashboard codes and vehicle electrical symptoms",
            "Exact model/year/OEM service data is required for model-specific diagnosis."),
        MachineExpert("farm", "Tractor & Farm Machine Expert",
            setOf("tractor", "power_tiller", "rotavator", "pump", "diesel_engine", "sprayer",
                "thresher", "combine", "reaper", "chaff_cutter", "rice_mill", "maize_sheller",
                "brush_cutter", "power_weeder", "transplanter", "solar_pump", "generator",
                "winnower", "grain_dryer", "feed_mixer", "root_digger", "areca_harvester",
                "tea_harvester", "tillage_implements", "planting_equipment", "fodder_harvester",
                "mulcher_baler", "dairy"),
            "Farm engines, tractors, irrigation, harvesting, crop processing and agricultural implements",
            "Guarded moving parts, PTO, blades, hydraulics and raised implements require trained service personnel."),
        MachineExpert("home", "Home Appliance Expert",
            setOf("ac", "tv", "washer", "fridge", "microwave", "water_heater"),
            "Cooling, heating, water flow, noise, display and appliance fault symptoms",
            "Mains electricity, capacitors, microwave high voltage and refrigerant circuits are technician-only."),
        MachineExpert("motor", "Motor & Pump Expert",
            setOf("motor", "pump", "solar_pump"),
            "Motor vibration, pump flow, overheating and nameplate clues",
            "Electrical isolation must be verified by a qualified person; never assume a switch is safe isolation."),
        MachineExpert("tools", "Workshop & Power Tool Expert",
            setOf("power_tools", "compressor", "welder", "drill", "lathe", "cnc"),
            "Tool operation, abnormal noise, visible damage, air and workshop equipment",
            "Pressure vessels, welding current, rotating tools and machine guards require qualified inspection."),
        MachineExpert("industrial", "Industrial Machine Expert",
            setOf("conveyor", "hydraulic_press", "forklift", "excavator", "loader", "concrete_mixer",
                "crusher", "boiler", "compressor"),
            "Industrial equipment symptoms, control indicators and safe shutdown escalation",
            "Stored energy, hydraulics, boilers, lifting equipment and interlocks are not user-repair targets.")
    )

    fun expertFor(machineId: String): MachineExpert? =
        experts.firstOrNull { machineId in it.machineIds }

    fun assess(
        machineId: String,
        machineName: String,
        brand: String,
        model: String,
        year: String,
        complaint: String
    ): ExpertAssessment? {
        val expert = expertFor(machineId) ?: return null
        val text = complaint.trim().lowercase()
        val dangerous = UrgentSymptomMatcher.isUrgent(text)

        val areas = mutableListOf<String>()
        val checks = mutableListOf<String>()
        val stops = mutableListOf<String>()

        fun addIf(keys: List<String>, area: String, check: String) {
            if (keys.any { SymptomPhraseMatcher.matches(text, it) }) {
                areas += area
                checks += check
            }
        }

        addIf(listOf("start", "starting", "crank", "click", "no start", "won't start", "not start"),
            "Starting / low-voltage supply", "Record the exact starting sound and dashboard or indicator behaviour. Do not bridge terminals.")
        addIf(listOf("noise", "sound", "vibration", "rattle", "grinding"),
            "Mechanical noise / vibration", "Stop the machine before approaching. From a safe distance, note when the noise occurs.")
        addIf(listOf("heat", "hot", "overheat", "temperature", "steam"),
            "Cooling / temperature", "Stop operation if temperature warning, steam or abnormal heat is present. Let it cool; do not open hot caps.")
        addIf(listOf("leak", "water", "oil", "fuel", "coolant", "drip"),
            "Fluid path / leakage", "Observe the fluid location from a safe distance. Do not touch unknown fluid or open pressurised lines.")
        addIf(listOf("brake", "steering", "wobble", "tyre", "tire"),
            "Braking / steering / wheel safety", "Do not operate or ride if braking, steering, tyre or wheel safety is uncertain.")
        addIf(listOf("smoke", "spark", "burn", "electrical", "trip", "electric shock", "bijli ka jhatka", "karant"),
            "Electrical / fire risk", "Switch off only if safe, keep clear and arrange qualified inspection. Do not open electrical covers.")
        addIf(listOf("error", "code", "warning", "light", "blink"),
            "Warning indicator / fault code", "Photograph the exact warning or code and record machine make, full model and year.")
        addIf(listOf("cool", "cooling", "heat nahi", "not cooling", "warm"),
            "Cooling / airflow / refrigeration", "Record the symptom and display code. Do not open refrigerant or sealed cooling circuits.")
        addIf(listOf("water not", "no water", "flow", "pressure", "pump"),
            "Pump / water-flow system", "Observe external water supply, visible hose condition and whether flow changed; do not open a pressurised system.")

        if (areas.isEmpty()) {
            areas += "Symptom needs clarification"
            checks += "Describe exactly what happens, when it happens, any sound/smell/leak, and the visible warning or error code."
        }

        if (dangerous) {
            stops += "STOP: Do not continue operating or testing while this warning is present."
            stops += "Keep people away from moving, hot, smoking, leaking or electrically damaged equipment."
        }
        stops += "Never bypass guards, interlocks, fuses, brakes or safety devices."
        stops += "Do not touch live wiring, rotating parts, hot surfaces, pressurised lines or high-voltage assemblies."

        val identity = listOf(brand, model, year).filter { it.isNotBlank() }.joinToString(" ")
        val identityText = if (identity.isBlank()) "Machine identity not supplied" else identity
        val summary = if (dangerous) {
            "Potential safety hazard reported for $machineName. Stop-use guidance takes priority over troubleshooting."
        } else {
            "$machineName symptom review started. This is a shortlist of areas to inspect, not a confirmed diagnosis."
        }

        return ExpertAssessment(
            expert = expert,
            summary = summary,
            possibleAreas = areas.distinct().take(5),
            safeChecks = checks.distinct().take(5),
            stopConditions = stops.distinct(),
            escalation = "Use the exact manufacturer manual for $identityText. If the manual does not cover this symptom, contact an authorised technician; do not guess a repair.",
            confidenceNote = "Offline rule-based triage only. Camera/OCR clues and symptom keywords cannot prove a fault or verify that a machine is safe."
        )
    }
}
