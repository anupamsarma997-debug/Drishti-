package com.vidya.safetyassistant.equipment

/**
 * First-stage part-level triage for farm and rural machines.
 * It lists plausible components from reported symptoms; it never confirms a failed part
 * or instructs users to dismantle, bypass guards, or work on hazardous systems.
 */
data class MachinePartFinding(
    val partName: String,
    val failureClues: List<String>,
    val possibleCauses: List<String>,
    val safeObservations: List<String>,
    val technicianAction: String,
    val neverDo: String
)

object PartFailureGuide {
    private val catalog: Map<String, List<MachinePartFinding>> = mapOf(
        "tractor" to listOf(
            finding("Battery / starter circuit", "No crank, slow crank, clicking", "Low battery charge, loose/corroded connection, starter or interlock fault", "Record whether dash lights work and whether starter clicks; photograph warning lamps", "Test battery and starter circuit using the model service manual", "Never bridge terminals or touch exposed wiring"),
            finding("Fuel supply / filters", "Cranks but does not start, loses power", "Fuel level/quality, restricted filter, air ingress or fuel-system fault", "Record fuel type, recent refuelling and when the symptom began", "Inspect fuel supply and filters using the exact service procedure", "Never loosen high-pressure diesel injection lines"),
            finding("Cooling system", "Temperature rises, coolant warning, steam", "Low coolant, blocked airflow, belt/fan or cooling-system fault", "From a safe distance note temperature and visible leaks", "Pressure-test and inspect cooling system after it is cool", "Never open a hot radiator cap or approach a moving fan"),
            finding("Clutch / transmission / PTO", "Gear engagement, drive or PTO abnormal", "Adjustment, lubrication, clutch, gearbox or driveline damage", "Record gear, load and whether noise occurs with PTO engaged", "Inspect driveline and clutch with tractor secured per manual", "Never approach a rotating PTO shaft")
        ),
        "power_tiller" to listOf(
            finding("Engine starting system", "Hard start, no start", "Fuel, ignition, air-filter or compression issue", "Record cold/hot start behaviour and visible switch positions", "Check ignition/fuel system per exact engine manual", "Do not spray starting fluid or open fuel lines"),
            finding("Clutch / transmission", "Machine pulls, drive slips, controls abnormal", "Clutch wear, cable/linkage or transmission fault", "Note whether issue occurs in each gear without forcing controls", "Inspect clutch and transmission using model-specific service data", "Do not adjust moving linkages with engine running"),
            finding("Tines / rotor assembly", "Vibration, uneven tilling", "Damaged tine, imbalance, bent shaft or loose mounting", "Stop and photograph from a safe distance after all motion stops", "Replace/align parts with correct torque and guarding", "Never touch or clear tines until isolated and verified stopped")
        ),
        "rotavator" to listOf(
            finding("Blades / rotor", "Poor tilling, vibration, metal noise", "Worn, bent or missing blades; imbalance or rotor damage", "Record working depth and photograph blades only when fully stopped", "Inspect rotor, blades and fasteners against model parts list", "Never reach under deck or remove crop by hand"),
            finding("Gearbox / oil seals", "Oil leak, overheating, grinding noise", "Seal wear, low lubricant, bearing or gear damage", "Note leak location and noise timing from a safe distance", "Check lubricant specification and gearbox condition", "Do not open a hot or loaded gearbox"),
            finding("PTO driveline / shear protection", "PTO noise, repeated drive interruption", "Universal-joint wear, shaft alignment or overload protection issue", "Record tractor PTO speed and implement model", "Inspect shaft guards, joints and protection device", "Never approach PTO while connected or rotating")
        ),
        "pump" to listOf(
            finding("Impeller / suction path", "No water, low flow, loss of prime", "Low source level, air leak, blocked strainer or impeller wear", "Observe source level, gauge and visible external leaks from dry ground", "Check suction integrity and impeller per pump manual", "Never open a pressurised or hot pump"),
            finding("Motor / capacitor / bearings", "Humming, slow start, overheating", "Supply issue, capacitor, bearing or winding fault", "Record sound, controller indication and whether breaker trips", "Qualified technician tests motor and electrical components", "Never touch terminals or test live wiring"),
            finding("Mechanical seal / pipe joints", "Water leakage near pump", "Seal wear, gasket failure or loose/damaged pipe", "Photograph leak location after safe shutdown", "Replace seal/gasket with correct model part", "Do not tighten pressurised fittings or work in wet electrical areas")
        ),
        "diesel_engine" to listOf(
            finding("Air filter / intake", "Black smoke, low power", "Restricted air intake, overloading or combustion fault", "Record smoke colour and load; inspect only external air-filter indicator if fitted", "Service intake and diagnose combustion to engine manual", "Do not run engine indoors or remove hot covers"),
            finding("Fuel filter / injection system", "Hard start, uneven running, power loss", "Contaminated fuel, restricted filter or injection fault", "Note recent fuel source and visible leaks", "Technician checks fuel pressure and injection system", "Never loosen injection pipes"),
            finding("Cooling / exhaust", "Overheating, smoke, unusual exhaust", "Cooling restriction, lubrication or combustion fault", "Stop safely and record temperature/smoke without approaching exhaust", "Inspect cooling and exhaust system after cool-down", "Never refuel hot/running engine")
        ),
        "sprayer" to listOf(
            finding("Nozzle / filter", "Uneven spray, low output", "Blocked/worn nozzle, filter restriction or wrong nozzle", "Observe spray pattern from safe distance with PPE and label precautions", "Clean/replace only per chemical and machine manual", "Never blow through nozzle or use mouth"),
            finding("Pump / pressure regulator", "Pressure fluctuates, low pressure", "Pump diaphragm/seal wear, regulator or suction leak", "Record gauge behaviour and visible external leaks", "Technician checks pump and regulator after depressurising", "Never open a pressurised chemical circuit"),
            finding("Hose / tank seals", "Chemical leak, smell, wet patches", "Cracked hose, seal or tank damage", "Stop, move upwind and keep people/animals away", "Replace compatible chemical-rated components", "Never touch spill without label-specified protection")
        ),
        "thresher" to listOf(
            finding("Feed rollers / feed chute", "Crop feed uneven, blockage", "Worn roller, foreign object or feed overload", "Record crop and blockage location without approaching intake", "Technician inspects feed assembly after lockout procedure", "Never push crop or reach into feed chute"),
            finding("Threshing drum / concave", "Grain not separating, excess broken grain", "Wear, clearance, speed or crop moisture mismatch", "Record crop moisture, settings and output sample", "Adjust/check drum and concave to exact model manual", "Never open guards while running"),
            finding("Belts / bearings", "Squeal, vibration, hot bearing area", "Belt tension/wear, bearing damage or misalignment", "Stop and note where sound originates from safe distance", "Replace and align using parts specification", "Never touch belts or bearings while moving/hot")
        ),
        "combine" to listOf(
            finding("Header cutter / knife drive", "Uneven cutting, crop not entering", "Worn sections, guards, drive or height setting", "Photograph header from safe distance after full shutdown", "Inspect cutter bar and knife drive per model manual", "Never reach into cutter bar or work under raised header"),
            finding("Feeder house / elevator chains", "Crop flow stops, repeated blockage", "Chain wear, tension, foreign material or drive fault", "Record crop conditions and exact blockage warning", "Technician inspects chain and elevator after isolation", "Never enter feeder house or clear by hand"),
            finding("Engine / hydraulic warning system", "Warning lamp, high temperature, low pressure", "Sensor, fluid, cooling or actual system fault", "Photograph exact warning and record engine hours", "Diagnose exact code with manufacturer service information", "Never ignore oil-pressure, fire or overheat warnings")
        ),
        "reaper" to listOf(
            finding("Cutter bar / knife sections", "Uneven cut, missed crop", "Dull/broken sections, guard or alignment issue", "Observe cut pattern and crop height from safe distance", "Inspect and set knife/guards using exact manual", "Never touch cutter bar while powered"),
            finding("Drive belt / gearbox", "Noise, vibration, loss of drive", "Worn belt, bearing or gearbox issue", "Record when noise occurs and visible belt condition after shutdown", "Technician checks drive alignment and gearbox", "Never adjust a running drive")
        ),
        "chaff_cutter" to listOf(
            finding("Feed rollers", "Fodder feed slips or stops", "Roller wear, drive fault or foreign object", "Note whether feed stops before cutter; do not approach opening", "Inspect roller drive after verified isolation", "Never push fodder with hands or feet"),
            finding("Cutter blades / flywheel", "Poor cut, vibration, loud impact", "Dull/damaged blade, imbalance or loose assembly", "Stop, isolate and photograph only after complete stop", "Balance and replace blades to manufacturer specification", "Never open housing or handle blades without trained service"),
            finding("Motor / belt drive", "Motor hum, belt squeal, overheating", "Supply, overload, belt or bearing issue", "Record sound and any external indicator", "Qualified technician checks motor and drive", "Never open electrical panel")
        ),
        "rice_mill" to listOf(
            finding("Huller / rubber rolls", "Poor husking, unhulled grain", "Roll wear, gap setting or feed inconsistency", "Record paddy variety, moisture and output", "Set/replace rolls to model-specific specification", "Never open roller chamber while connected to power"),
            finding("Polisher screen / abrasive parts", "Poor polish, broken rice", "Worn screen, abrasive wear or feed setting", "Keep sample and note feed setting", "Technician checks screen and polishing assembly", "Never reach into rotating chamber"),
            finding("Bearings / dust extraction", "Heat, vibration, excess dust", "Bearing wear, misalignment or blocked extraction", "Stop if burning smell or smoke; note dust condition", "Inspect bearings and dust extraction", "Never ignore smoke or run with guards removed")
        ),
        "maize_sheller" to listOf(
            finding("Shelling rollers / concave", "Poor shelling, grain damage", "Roll wear, clearance, crop moisture or feed rate", "Record crop moisture and shelling output", "Set roller clearance per model manual", "Never reach into roller opening"),
            finding("Feed hopper / metering", "Irregular feeding, repeated jam", "Foreign material, bridging or metering wear", "Stop and note crop and blockage location", "Clear only with documented isolation by trained operator", "Never clear while powered"),
            finding("Drive belt / bearings", "Noise, heat, vibration", "Belt wear, bearing damage or misalignment", "Record symptom after stopping from safe distance", "Technician inspects drive train", "Never touch moving belts")
        ),
        "brush_cutter" to listOf(
            finding("Cutting head / blade", "Uneven cut, vibration", "Damaged blade, wrong attachment or imbalance", "Stop and photograph attachment after complete stop", "Fit only manufacturer-approved attachment and guard", "Never use cracked blade or missing guard"),
            finding("Air filter / spark plug system", "Hard start, stalls", "Dirty filter, plug wear or fuel issue", "Record cold/hot behaviour and model code", "Service ignition and intake to engine manual", "Do not touch hot plug or fuel parts"),
            finding("Clutch / drive shaft", "Head does not turn, rattling", "Clutch, shaft or gearbox fault", "Note sound and whether engine speed changes", "Technician checks clutch and shaft assembly", "Do not open clutch housing yourself")
        ),
        "power_weeder" to listOf(
            finding("Tines / rotor", "Poor weeding, vibration", "Worn/bent tines, soil wrapping or imbalance", "Observe soil and working width after complete shutdown", "Inspect rotor and tine set to parts diagram", "Never clear wrapped weeds by hand"),
            finding("Clutch / drive cable", "Machine will not move or pulls", "Cable adjustment, clutch wear or gearbox fault", "Record gear and control response without forcing lever", "Technician inspects clutch and drive", "Never adjust with engine running")
        ),
        "transplanter" to listOf(
            finding("Planting fingers / linkage", "Missing hills, uneven spacing", "Worn fingers, linkage or planting timing", "Record hill spacing and seedling condition", "Set linkage and replace parts using exact manual", "Keep hands away from moving planting arms"),
            finding("Seed/fertilizer metering unit", "Uneven seed rate, blockage", "Meter wear, bridging or incorrect setting", "Record seed type and row setting", "Clean/adjust only by manual after full isolation", "Avoid direct contact with treated seed/fertilizer")
        ),
        "solar_pump" to listOf(
            finding("PV array / DC isolator", "Controller off, intermittent pumping", "Shade, damaged cable, isolator or array fault", "Photograph display and observe panels from ground", "Solar technician verifies array and DC protection", "Never disconnect PV connectors or open live DC equipment"),
            finding("Controller / inverter", "Error code, restart, overheating", "Overload, dry-run, sensor or controller fault", "Record exact code, LED pattern and pump model", "Decode code using exact controller manual", "Never open enclosure or touch terminals"),
            finding("Pump / water source", "No water despite normal display", "Low water, dry-run protection, blockage or pump wear", "Observe water level and delivery from safe place", "Technician tests pump and bore/well condition", "Do not pull submersible pump or open pipework yourself")
        ),
        "generator" to listOf(
            finding("Battery / starter", "No crank, slow crank", "Battery, starter relay or charging fault", "Record display and click/crank behaviour", "Technician tests starter and charging system", "Never bridge starter terminals"),
            finding("Fuel / air filter", "Hard start, black smoke, power loss", "Fuel restriction, poor fuel or blocked air filter", "Record fuel type, smoke colour and recent service", "Service fuel/air system to exact engine manual", "Never open injection lines"),
            finding("Alternator / AVR / output protection", "Engine runs but no output, unstable voltage", "AVR, alternator, breaker or overload fault", "Record external meter/display reading only if already safely shown", "Qualified electrician tests generator output system", "Never open panel or touch output terminals")
        ),
        "winnower" to listOf(
            finding("Fan / air gate", "Grain not cleaned, chaff remains", "Airflow setting, fan wear or blockage", "Observe output separation and external setting", "Inspect fan and air gate after isolation", "Never reach into fan housing"),
            finding("Screen / sieve", "Grain loss, uneven grading", "Wrong screen size, blockage or screen damage", "Record crop and sample from outlet", "Fit correct screen and inspect mounts", "Never change screen while machine runs"),
            finding("Bearings / belt", "Vibration, noise, overheating", "Bearing wear, belt misalignment", "Note location of noise after shutdown", "Technician inspects bearings and belt drive", "Never touch moving belt")
        ),
        "grain_dryer" to listOf(
            finding("Burner / heater", "Low heat, flame warning, smoke", "Fuel/air mix, burner or ignition fault", "Record alarm and temperature display; stay clear of burner", "Qualified service checks burner and combustion", "Never adjust gas/fuel train or open burner"),
            finding("Fan / airflow path", "Uneven drying, weak airflow", "Fan, belt, duct blockage or airflow setting", "Record inlet/outlet readings and crop load", "Inspect fan/ducts after shutdown and cool-down", "Never reach into fan or open hot chamber"),
            finding("Temperature sensor / controller", "Wrong temperature reading, alarm", "Sensor, wiring or controller fault", "Photograph exact display and alarm code", "Verify sensor using model-specific procedure", "Never bypass thermal cut-outs")
        ),
        "feed_mixer" to listOf(
            finding("Hammers / screen", "Coarse grind, low output", "Worn hammers, screen damage or wrong screen size", "Record material and output texture", "Replace/inspect hammer and screen set after isolation", "Never open grinding chamber while connected"),
            finding("Mixer paddles / gearbox", "Uneven mix, abnormal noise", "Paddle wear, gearbox or drive fault", "Record batch size and mixing time", "Technician inspects gearbox and mixing elements", "Never reach into mixer"),
            finding("Motor / bearings", "Overheating, vibration", "Overload, bearing wear or electrical fault", "Stop on burning smell; note load and duration", "Qualified service checks motor and bearings", "Never open electrical enclosure")
        ),
        "root_digger" to listOf(
            finding("Digging blade / share", "Crop not lifted, uneven digging", "Wear, incorrect depth or soil mismatch", "Record soil condition and working depth", "Set/replace blade per exact parts diagram", "Never adjust while implement is raised or powered"),
            finding("Chain / conveyor", "Chain noise, crop flow stops", "Chain wear, tension or foreign object", "Observe only after safe shutdown", "Technician inspects chain and sprockets", "Never touch chain or work under raised implement")
        ),
        "areca_harvester" to listOf(
            finding("Pole / rope / harness assembly", "Unstable reach, cable damage", "Wear, cracked pole, rope or connector damage", "Keep unit on ground; inspect visually from safe distance", "Manufacturer-trained service inspects load-bearing components", "Never climb or use improvised ladder"),
            finding("Cutting head / power unit", "Cutting failure, abnormal vibration", "Blade wear, motor or drive fault", "Record model and visible damage from ground", "Technician services cutting head and power unit", "Keep clear of overhead lines and falling-object zone")
        ),
        "tea_harvester" to listOf(
            finding("Cutter blade / reciprocating drive", "Uneven plucking, missed leaves", "Blade wear, alignment or drive issue", "Record cut pattern and crop row", "Set cutter and replace blade using exact manual", "Never adjust cutter while powered"),
            finding("Engine / fuel / air filter", "Smoke, fuel smell, power loss", "Fuel leak, air restriction or engine fault", "Stop safely and note smoke/smell from upwind position", "Technician checks fuel and engine system", "Never refuel hot/running equipment")
        ),
        "tillage_implements" to listOf(
            finding("Plough shares / discs / bearings", "Poor soil turning, uneven tillage", "Worn shares, blunt discs, bearing or alignment issue", "Record soil type, depth and visible wear after implement is grounded", "Replace and align using implement parts list", "Never work beneath a raised implement"),
            finding("Hydraulic cylinder / hoses / couplers", "Implement will not lift or lower", "Hydraulic leak, cylinder, coupler or tractor supply fault", "Observe external leak only from a safe distance", "Technician checks pressure and cylinder after safely lowering implement", "Never search for pinhole leaks by hand or loosen pressurised hoses")
        ),
        "planting_equipment" to listOf(
            finding("Metering rollers / seed plates", "Missed seeds, uneven spacing", "Wrong plate, wear, calibration or seed bridging", "Record crop, seed size and spacing setting", "Calibrate with exact seed/planter manual", "Keep hands away from moving metering drive"),
            finding("Opener / furrow / depth wheels", "Seed depth uneven, rows inconsistent", "Wear, blockage, pressure or terrain variation", "Observe row pattern and field condition", "Inspect opener and depth setting after full shutdown", "Never clear while drive is moving")
        ),
        "fodder_harvester" to listOf(
            finding("Cutter drum / knives", "Uneven chop, impact noise", "Dull/damaged knives, drum imbalance or setting", "Record crop length and sound; stay clear of intake", "Balance and set knives to manufacturer specification", "Never open cutter housing or reach into feed path"),
            finding("Feed rollers / conveyor", "Crop feed stops, blockage", "Roller wear, foreign object or drive fault", "Note where feed stops without approaching chute", "Technician inspects roller drive after lockout", "Never pull crop from feed rollers")
        ),
        "mulcher_baler" to listOf(
            finding("Mulcher rotor / flails", "Uneven mulching, vibration", "Worn/missing flails, rotor imbalance or height setting", "Observe residue pattern and photograph only when grounded and stopped", "Inspect rotor and flails against exact parts list", "Never reach under deck or remove wrapped material by hand"),
            finding("Baler pickup / knotter / belts", "Bale not forming, tying failure", "Pickup tine, twine path, knotter or belt wear", "Record bale shape and displayed warnings", "Technician inspects pickup and knotter after isolation", "Never enter bale chamber or reach into pickup"),
            finding("Hydraulic / bale chamber", "Chamber warning, pressure or gate issue", "Hydraulic leak, latch or sensor fault", "Record display code and keep clear of chamber", "Service hydraulic/latch system with chamber secured per manual", "Never enter chamber or work beneath raised gate")
        ),
        "dairy" to listOf(
            finding("Vacuum pump / regulator", "Weak suction, unstable vacuum", "Pump wear, air leak, regulator or filter issue", "Record gauge reading and visible alarm", "Technician checks vacuum system to hygiene manual", "Never open a running pump"),
            finding("Pulsator / liners / milk tubes", "Uneven milking, air leak", "Worn liners, pulsator or tube leak", "Observe cluster operation and replace schedule in manual", "Fit correct food-grade replacement parts", "Stop if animal welfare is affected"),
            finding("Chiller compressor / temperature sensor", "Milk not cooling, temperature alarm", "Refrigeration, airflow, sensor or compressor fault", "Record temperature and alarm; protect milk using food-safety procedure", "Qualified refrigeration technician diagnoses sealed system", "Never open refrigerant or electrical compartments")
        )
    )

    private fun finding(part: String, clues: String, causes: String, observations: String,
                        technician: String, never: String) = MachinePartFinding(
        partName = part,
        failureClues = clues.split(", "),
        possibleCauses = listOf(causes),
        safeObservations = listOf(observations),
        technicianAction = technician,
        neverDo = never
    )

    fun forMachine(machineId: String): List<MachinePartFinding> = catalog[machineId].orEmpty()

    fun match(machineId: String, complaint: String): List<MachinePartFinding> {
        val entries = forMachine(machineId)
        if (entries.isEmpty()) return emptyList()
        val query = complaint.lowercase().trim()
        if (query.isBlank()) return entries.take(3)
        val matches = entries.filter { item ->
            (item.failureClues + item.partName + item.possibleCauses)
                .any { clue -> clue.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length > 2 }
                    .any { token -> query.contains(token) } }
        }
        return (matches + entries.filterNot { it in matches }).take(4)
    }
}
