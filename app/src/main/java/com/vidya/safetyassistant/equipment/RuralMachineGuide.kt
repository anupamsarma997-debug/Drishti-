package com.vidya.safetyassistant.equipment

data class FarmMachineSymptom(
    val id: String,
    val label: String,
    val safeObservations: List<String>,
    val stopWork: Boolean = false,
    val stopReason: String? = null
)

data class FarmMachineCategory(
    val id: String,
    val label: String,
    val examples: String,
    val hazards: String,
    val symptoms: List<FarmMachineSymptom>
)

/**
 * Conservative rural/agricultural machine triage.
 * Provides external observations and escalation only, never disassembly or repair steps.
 */
object RuralMachineGuide {
    private fun symptom(
        id: String,
        label: String,
        vararg observations: String,
        stop: Boolean = false,
        reason: String? = null
    ) = FarmMachineSymptom(id, label, observations.toList(), stop, reason)

    private fun machine(
        id: String,
        label: String,
        examples: String,
        hazards: String,
        vararg symptoms: FarmMachineSymptom
    ) = FarmMachineCategory(id, label, examples, hazards, symptoms.toList())

    val categories = listOf(
        machine("tractor", "Tractor", "Mahindra, Swaraj, Sonalika, John Deere, New Holland, Massey Ferguson, Kubota", "Vehicle movement, PTO, hydraulics, roll-over and exhaust",
            symptom("no_start", "Engine will not start", "Record dashboard lamps and displayed code before repeated attempts.", "From the operator seat, check only gear/neutral and parking-brake indicators described in the manual.", "Note whether starter is silent, clicks or cranks slowly; never bridge terminals."),
            symptom("overheat", "Engine temperature high", "Stop in a safe open area and follow the operator manual shutdown guidance.", "Keep clear of steam, radiator cap, fan and hot engine parts.", "Never open a hot radiator cap; arrange service.", stop = true, reason = "Overheating and hot coolant can cause severe injury and engine damage."),
            symptom("pto", "PTO or implement issue", "Disengage PTO from the operator position if safe and stop the engine before leaving the seat.", "Keep everyone clear of the PTO shaft and implement.", "Never approach, step over or adjust a rotating PTO shaft.", stop = true, reason = "PTO entanglement can cause fatal injuries.")
        ),
        machine("power_tiller", "Power tiller / walking tractor", "VST, KAMCO, Kubota, Shrachi, Honda", "Rotating blades, transmission, vibration and loss of control",
            symptom("hard_start", "Difficult to start", "Record engine model and visible switch positions.", "Check fuel only when engine is cool and according to its manual.", "Do not use starting fluid or open fuel/injection parts without trained service."),
            symptom("vibration", "Strong vibration", "Stop engine and wait for every moving part to stop.", "Keep clear of blades and tines; do not touch to check looseness.", "Have mounting, fasteners and rotating assembly inspected.", stop = true, reason = "Unexpected vibration may indicate a damaged rotating component."),
            symptom("control", "Machine pulls or control feels abnormal", "Release controls and stop in a clear area.", "Do not continue on slopes or near people.", "Arrange inspection of steering, clutch and drive system.")
        ),
        machine("rotavator", "Rotavator / rotary tiller", "Tractor or power-tiller mounted", "PTO entanglement, blades, thrown stones and stored energy",
            symptom("poor_tilling", "Poor soil cutting", "Stop PTO, switch off engine, remove key and wait for all motion to stop.", "Observe working depth and soil condition from a safe distance.", "Never clear wrapped grass or debris by hand.", stop = true, reason = "Rotating blades may continue moving after power is disengaged."),
            symptom("noise", "Metal noise or vibration", "Disengage PTO and stop the tractor safely.", "Keep people away from the implement.", "Do not operate until a technician checks blades, gearbox and driveline.", stop = true, reason = "Damaged blades or driveline components may be ejected.")
        ),
        machine("pump", "Irrigation / water pump", "Electric, diesel, petrol, solar and submersible pumps", "Electric shock, dry running, pressure, fuel and water",
            symptom("no_water", "No water / low flow", "Record pump type, brand, model and controller code.", "Observe source water level and visible valve positions from a dry safe place.", "Do not loosen pipes, prime a hot pump or touch electrical terminals."),
            symptom("overheat", "Pump or motor gets hot", "Stop using the normal safe control if dry and accessible.", "Keep away from hot surfaces and wet electrical areas.", "Do not restart until a qualified technician checks it.", stop = true, reason = "Heat, burning smell or smoke is a stop-work condition."),
            symptom("leak", "Pipe or pump leakage", "Keep clear if water is near wiring or the motor.", "Do not tighten pressurised fittings or open a pump casing.", "Record the visible leak location for the technician.", stop = true, reason = "Water around powered electrical equipment can cause fatal shock.")
        ),
        machine("diesel_engine", "Diesel / petrol agricultural engine", "Portable irrigation engines and stationary engines", "Hot exhaust, fuel fire, carbon monoxide, belts and moving parts",
            symptom("no_start", "Engine not starting", "Operate outdoors only, away from doors and windows.", "Record warning lamps and whether it cranks.", "Do not open injection lines, carburettors or fuel-system parts."),
            symptom("smoke", "Heavy smoke or fuel smell", "Stop if safe, move people away and keep clear of hot exhaust.", "Do not refuel while running or hot.", "Do not restart until inspected by a qualified mechanic.", stop = true, reason = "Fuel leak, smoke or exhaust exposure may cause fire or poisoning.")
        ),
        machine("sprayer", "Knapsack / power / tractor sprayer", "Manual, battery, petrol and PTO-driven sprayers", "Chemical exposure, high pressure, drift and rotating pump parts",
            symptom("low_pressure", "Low spray pressure", "Stop spraying and keep PPE on as required by the chemical label.", "Record pressure gauge reading and nozzle pattern from a safe distance.", "Never put your mouth to a nozzle or open a pressurised system."),
            symptom("leak", "Chemical leakage", "Stop work, move upwind and prevent people and animals entering the area.", "Follow the pesticide label and local emergency advice for exposure.", "Do not touch or wash chemical spills into drains without label guidance.", stop = true, reason = "Chemical contact or inhalation can cause serious poisoning.")
        ),
        machine("thresher", "Paddy / multi-crop thresher", "Engine, motor, tractor or power-tiller driven", "Feed rollers, rotating drum, belts, dust and entanglement",
            symptom("poor_thresh", "Grain not separating properly", "Stop power using the safe control and wait for all movement to stop.", "Record crop type, moisture condition and model.", "Never push crop by hand or open guards while running.", stop = true, reason = "Feed rollers and threshing drums can cause fatal entanglement."),
            symptom("blockage", "Choking / blockage", "Stop engine/PTO, remove key and wait for all moving parts to stop.", "Do not reach into the feed path.", "Use the exact manual and trained service for clearing; never restart with guards removed.", stop = true, reason = "Stored rotation and unexpected restart can cause severe injury.")
        ),
        machine("combine", "Combine harvester", "Paddy, wheat and multi-crop harvesters", "Header, auger, threshing drum, straw chopper and hydraulics",
            symptom("header", "Header not cutting or feeding", "Stop on level ground, disengage header and follow full shutdown/isolation procedure.", "Keep clear of cutter bar and auger.", "Do not clear crop wrapping by hand or work beneath raised equipment.", stop = true, reason = "Header and raised equipment can move or fall unexpectedly."),
            symptom("warning", "Warning lamp / display code", "Photograph exact display and record model, year and engine hours.", "Stop if temperature, oil pressure, fire or hydraulic warnings appear.", "Do not clear codes before recording them.")
        ),
        machine("reaper", "Reaper / reaper-binder", "Self-propelled, power-tiller or tractor mounted", "Cutter bar, belts, blades and crop conveyors",
            symptom("not_cutting", "Not cutting evenly", "Stop drive and engine and wait for cutter bar motion to stop.", "Observe crop condition and cutting height from a safe distance.", "Do not touch blades or adjust while powered.", stop = true, reason = "Cutter bar blades can cause severe cuts."),
            symptom("noise", "Unusual noise", "Stop operation and keep bystanders away.", "Record when noise starts and any visible vibration.", "Have belts, guards and drive parts inspected.")
        ),
        machine("chaff_cutter", "Chaff / fodder cutter", "Manual, electric motor, engine or tractor PTO driven", "Sharp blades, feed rollers and entanglement",
            symptom("not_cutting", "Fodder not cutting", "Stop and isolate power; wait for all parts to stop.", "Never push fodder with hands or feet.", "Use only the supplied feed tool and exact manual procedure.", stop = true, reason = "Feed rollers can pull hands or clothing into the cutter."),
            symptom("jam", "Machine jammed", "Stop power, isolate and prevent accidental restart.", "Do not reach into feed chute or blade housing.", "Call a trained operator or technician for clearing.", stop = true, reason = "A jam can retain stored mechanical energy in the cutter and feed mechanism.")
        ),
        machine("rice_mill", "Rice mill / huller / polisher", "Mini rice mill, huller and polishing units", "Belts, rollers, dust, motors and hot bearings",
            symptom("poor_polish", "Poor milling / polishing", "Stop machine before inspecting external feed settings.", "Record crop moisture, feed-rate setting and model.", "Do not open roller or huller housing while connected to power."),
            symptom("dust", "Excess dust or overheating", "Stop if smoke, burning smell or excessive heat occurs.", "Keep ventilation clear and avoid inhaling dust.", "Have bearings, belts and dust extraction checked.", stop = true, reason = "Dust exposure and overheated components can create respiratory and fire hazards.")
        ),
        machine("maize_sheller", "Maize sheller / groundnut decorticator", "Manual and powered shellers/decorticators", "Rotating rollers, belts, pinch points and thrown material",
            symptom("poor_shell", "Poor shelling", "Stop and isolate power before observing settings.", "Record crop moisture and model.", "Do not reach into rollers or feed opening."),
            symptom("jam", "Jammed feed", "Stop power and wait for all moving parts to stop.", "Keep hands and clothing away from rollers.", "Use exact manual clearing procedure or call a technician.", stop = true, reason = "Roller pinch points can crush fingers.")
        ),
        machine("brush_cutter", "Brush cutter / grass trimmer", "Petrol, battery and electric types", "High-speed blade, thrown stones, fuel and noise",
            symptom("not_start", "Not starting", "Keep cutting head on the ground and away from people.", "Record model and visible switch position.", "Do not touch spark plug, fuel system or blade while hot."),
            symptom("vibration", "Strong vibration / damaged head", "Stop engine and wait for the head to stop completely.", "Do not use a cracked blade, damaged guard or loose cutting head.", "Have the unit inspected before reuse.", stop = true, reason = "A damaged high-speed cutting attachment can break apart.")
        ),
        machine("power_weeder", "Power weeder", "Walk-behind and mini power weeders", "Rotating tines, hot engine and loss of control",
            symptom("poor_weeding", "Not removing weeds", "Stop engine and wait for tines to stop.", "Record soil condition, working width and model.", "Do not remove weeds wrapped around tines by hand.", stop = true, reason = "Tines can rotate unexpectedly and cause severe injury."),
            symptom("control", "Unstable movement", "Stop in a clear area and release drive controls.", "Do not operate on steep or slippery ground.", "Arrange inspection of clutch, steering and wheels.")
        ),
        machine("transplanter", "Paddy transplanter / seed drill", "Walk-behind and riding transplanters; seed/fertilizer drills", "Moving planting arms, pinch points, PTO and chemical contact",
            symptom("uneven", "Uneven planting / seed spacing", "Stop drive and engine before inspecting visible settings.", "Record crop, row setting and model.", "Keep hands away from planting arms and metering mechanisms."),
            symptom("jam", "Seedling or seed blockage", "Stop and isolate all power; wait for moving parts to stop.", "Do not clear a blockage while the mechanism is moving.", "Follow exact manual or call trained service.", stop = true, reason = "Planting mechanisms can trap fingers.")
        ),
        machine("solar_pump", "Solar water pump controller", "Solar irrigation pumps and inverter/controller units", "DC electrical shock, arc, water and PV string voltage",
            symptom("no_output", "Pump not running / controller error", "Photograph exact controller display and record pump/controller model.", "Observe only external status LEDs and water-source level.", "Do not open controller, disconnect PV wires or measure live DC terminals.", stop = true, reason = "Solar DC circuits can remain energised in daylight even when a switch is off."),
            symptom("overheat", "Controller hot / burning smell", "Keep clear and use only a safe external isolator if dry and accessible.", "Keep water away from electrical equipment.", "Call a solar pump technician; do not open enclosure.", stop = true, reason = "Burning smell or heat at electrical equipment requires stop-work.")
        ),
        machine("generator", "Farm generator / genset", "Diesel, petrol and LPG generators", "Carbon monoxide, fuel, exhaust, hot surfaces and electricity",
            symptom("no_start", "Not starting", "Operate outdoors only, far from doors, windows and people.", "Record model, fuel type, display and indicators.", "Never refuel while hot or running."),
            symptom("no_power", "No electrical output", "Use safe external controls as directed by the manual.", "Do not touch sockets, terminals or internal wiring.", "Arrange qualified electrical/generator service.", stop = true, reason = "Generator output can cause lethal electric shock.")
        ),
        machine("winnower", "Winnower / grain cleaner / grader", "Manual, electric and engine-driven grain cleaning units", "Fans, belts, dust and rotating screens",
            symptom("poor_cleaning", "Grain not cleaning evenly", "Stop before observing external airflow or feed settings.", "Record crop type, moisture and model.", "Keep hands away from fan, belts and screens while powered."),
            symptom("dust", "Excessive dust or vibration", "Stop if dust becomes unsafe or vibration is unusual.", "Keep bystanders away and use suitable dust protection.", "Have fan, screen and bearings inspected.", stop = true, reason = "Dust exposure and rotating parts can injure operators.")
        ),
        machine("grain_dryer", "Grain dryer / seed dryer", "Batch, flat-bed and mechanical grain dryers", "Hot air, burners, fuel, electricity and fire",
            symptom("not_drying", "Grain not drying evenly", "Record crop, moisture readings and controller display.", "Observe only external air inlet/outlet and alarms.", "Do not open burner, heater or electrical compartments."),
            symptom("overheat", "High temperature / smoke", "Stop using safe external control and move people away.", "Do not open hot chambers or add water to electrical/burner equipment.", "Call trained service before restarting.", stop = true, reason = "Smoke, flame or overheating is an immediate fire risk.")
        ),
        machine("feed_mixer", "Feed grinder / hammer mill / mixer", "Small farm feed mills and grinder-cum-mixer units", "High-speed hammers, belts, dust and motors",
            symptom("poor_grind", "Feed not grinding properly", "Stop and isolate power before checking external feed settings.", "Record material type and machine model.", "Never reach into hopper, grinder or discharge chute."),
            symptom("jam", "Machine jam or loud impact", "Stop power and wait for every moving part to stop.", "Do not open grinding chamber or remove guards.", "Arrange inspection by trained service.", stop = true, reason = "Hammer mills can retain dangerous rotational energy.")
        ),
        machine("root_digger", "Potato / ginger / turmeric digger", "Tractor, power-tiller and animal-drawn diggers", "PTO, rotating chains, digging blades and raised implements",
            symptom("poor_digging", "Crop not lifting / digging unevenly", "Stop drive and PTO before observing working depth or soil.", "Record crop, soil condition and implement model.", "Do not adjust blades or chains while connected to power."),
            symptom("noise", "Chain noise or vibration", "Stop tractor/engine, remove key and wait for all movement to stop.", "Keep clear of chains, blades and raised parts.", "Have driveline and digging assembly inspected.", stop = true, reason = "Chains and blades can trap or cut hands.")
        ),
        machine("areca_harvester", "Areca nut / orchard harvester", "Pole-mounted and powered climbing/harvesting equipment", "Falls, falling fruit, blades and electrical equipment",
            symptom("not_working", "Harvester not working", "Keep equipment on the ground and away from overhead power lines.", "Record model and visible fault indicators.", "Do not climb or use improvised ladders to inspect."),
            symptom("damage", "Cable, pole or cutting head damage", "Stop use and keep people away from the drop zone.", "Do not operate with cracked poles, damaged ropes or exposed conductors.", "Arrange trained inspection before use.", stop = true, reason = "Falls and falling objects can cause fatal injury.")
        ),
        machine("tea_harvester", "Tea leaf plucker / tea garden machine", "Handheld and ride-on tea pruning/harvesting equipment", "Rotating blades, sharp cutters, fuel and uneven terrain",
            symptom("uneven_cut", "Uneven plucking / cutting", "Stop machine and wait for all cutting parts to stop.", "Record crop row, cutting-height setting and model.", "Do not adjust cutter blades while powered."),
            symptom("fuel_smell", "Fuel smell / smoke / overheating", "Stop if safe, move away from ignition sources and hot parts.", "Do not refuel while running or hot.", "Arrange qualified service before restart.", stop = true, reason = "Fuel leaks and hot engines can cause fire.")
        ),
        machine("tillage_implements", "Plough / harrow / cultivator / land leveller", "Disc plough, MB plough, disc harrow, subsoiler and leveller", "Tractor movement, PTO, hydraulics and heavy raised implements",
            symptom("poor_tillage", "Soil not turning or levelling evenly", "Stop tractor and disengage PTO before observing alignment.", "Record soil condition, working depth and implement model.", "Never work beneath raised equipment or adjust linkages while hydraulics are active.", stop = true, reason = "Raised implements can fall suddenly; PTO and tractor movement can be fatal."),
            symptom("hydraulic", "Implement not lifting / lowering normally", "Stop in a level safe area and lower implement only if the manual permits.", "Keep clear of hydraulic arms and suspended loads.", "Do not loosen hydraulic hoses or fittings.", stop = true, reason = "Hydraulic systems retain pressure and suspended equipment can drop.")
        ),
        machine("planting_equipment", "Seed drill / planter / potato planter", "Seed-cum-fertilizer drill, pneumatic and crop planters", "Moving metering rollers, PTO, fertilizer and pinch points",
            symptom("uneven_seeding", "Uneven seed spacing / missed rows", "Stop tractor and machine before observing external settings.", "Record crop, seed type, row spacing and model.", "Keep hands away from metering rollers and moving chains."),
            symptom("blockage", "Seed or fertilizer blockage", "Stop engine/PTO, lower equipment and wait for all movement to stop.", "Avoid direct contact with fertilizer and treated seeds.", "Do not clear blockage while rollers or chains are moving.", stop = true, reason = "Metering mechanisms and rotating drive parts can trap fingers.")
        ),
        machine("fodder_harvester", "Fodder harvester / forage chopper", "Tractor PTO and self-propelled fodder harvesters", "PTO entanglement, cutter drums, feed rollers and thrown material",
            symptom("poor_cut", "Fodder not cutting evenly", "Stop engine/PTO and wait for cutter drum and feed rollers to stop.", "Record crop type, feed condition and model.", "Never push crop by hand or open cutter housing while powered.", stop = true, reason = "Feed rollers and cutter drums can cause fatal entanglement."),
            symptom("jam", "Chute or cutter blockage", "Stop power, remove key and prevent accidental restart.", "Keep clear of feed chute and stored rotating parts.", "Use exact manufacturer isolation procedure through trained service.", stop = true, reason = "Stored mechanical energy can cause unexpected movement.")
        ),
        machine("mulcher_baler", "Mulcher / straw baler", "Rotary mulcher, round straw baler and residue machines", "PTO, pickup tines, belts, hydraulic compaction and fire",
            symptom("poor_mulching", "Residue not mulching evenly", "Disengage PTO, stop engine and wait for rotor motion to stop.", "Observe residue and working height from a safe distance.", "Do not reach under deck or remove wrapped crop by hand.", stop = true, reason = "Rotor blades and pickup tines can cause severe injury."),
            symptom("baler_blockage", "Baler blockage / knotting issue", "Stop tractor and baler, remove key and follow model-specific isolation procedure.", "Stay clear of bale chamber, belts and hydraulics.", "Never enter or reach into a baler chamber.", stop = true, reason = "Bale chambers and hydraulic mechanisms can crush or trap a person.")
        ),
        machine("dairy", "Dairy milking machine / milk chiller", "Vacuum pump, milking cluster and bulk milk cooler", "Electricity near water, vacuum, rotating pumps and hygiene",
            symptom("low_vacuum", "Low vacuum / weak suction", "Stop milking if animal welfare or equipment safety is affected.", "Record gauge reading and visible alarms.", "Do not open vacuum pump or electrical panels; clean only parts specified by hygiene manual."),
            symptom("not_cooling", "Milk chiller not cooling", "Maintain hygiene and follow food-safety procedures.", "Record temperature display and alarm code.", "Do not open refrigerant circuits or electrical covers; arrange service.", stop = true, reason = "Milk temperature failure can create food-safety risks.")
        )
    )

    fun category(id: String): FarmMachineCategory? = categories.firstOrNull { it.id == id }

    fun advice(categoryId: String, symptomId: String): TroubleshootingAdvice? {
        val category = category(categoryId) ?: return null
        val selected = category.symptoms.firstOrNull { it.id == symptomId } ?: return null
        return TroubleshootingAdvice(
            title = "${category.label}: ${selected.label}",
            steps = selected.safeObservations,
            stopWork = selected.stopWork,
            warning = selected.stopReason
                ?: "Observation only, not a diagnosis. Confirm the exact manufacturer manual or contact a trained agricultural machinery technician."
        )
    }

    fun universalSafetyNote(): String =
        "Farm machinery stop rule: switch off from the operator's safe control, remove the key and wait for every moving part to stop before leaving the operating position. Never clear jams by hand, reach into feed chutes, approach PTO shafts, remove guards while running, work beneath raised equipment, open live electrical panels, or operate engines indoors. Use trained service for fuel, hydraulic, chemical, blade, PTO and electrical faults."
}
