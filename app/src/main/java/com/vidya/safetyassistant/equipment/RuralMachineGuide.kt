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
 * Rural/agricultural machine triage. Only external observations and safe escalation.
 * No instructions for dismantling, live electrical testing, blade clearing or servicing.
 */
object RuralMachineGuide {
    val categories = listOf(
        FarmMachineCategory("tractor", "Tractor", "Mahindra, Swaraj, Sonalika, John Deere, New Holland, Massey Ferguson, Kubota and others", "Vehicle movement, PTO, hydraulics, roll-over and exhaust hazards", listOf(
            FarmMachineSymptom("no_start", "Engine will not start", listOf("Record dashboard lamps and any displayed code before repeated attempts.", "From the operator seat, check only the gear/neutral and parking-brake indicators described in the manual.", "Note whether the starter is silent, clicks, or cranks slowly; do not bridge terminals.")),
            FarmMachineSymptom("overheat", "Engine temperature high", listOf("Stop in a safe open area and follow the operator manual's shutdown guidance.", "Keep clear of steam, radiator cap, fan and hot engine parts.", "Do not open the radiator cap while hot; arrange service."), true, "Overheating can cause serious engine damage and hot coolant burns."),
            FarmMachineSymptom("pto", "PTO or implement issue", listOf("Disengage PTO from the operator position if safe and stop the engine before leaving the seat.", "Keep everyone clear of the PTO shaft and implement.", "Never approach, step over or adjust a rotating PTO shaft."), true, "PTO entanglement can cause fatal injuries.")
        )),
        FarmMachineCategory("power_tiller", "Power tiller / walking tractor", "VST, KAMCO, Kubota, Shrachi, Honda and others", "Rotating blades, exposed transmission, vibration and loss of control", listOf(
            FarmMachineSymptom("hard_start", "Difficult to start", listOf("Record the engine model and visible switch positions.", "Check fuel level only when engine is cool and following the manual.", "Do not spray starting fluid or open fuel/injection parts without trained service.")),
            FarmMachineSymptom("vibration", "Strong vibration", listOf("Stop engine and wait for every moving part to stop.", "Keep clear of blades and tines; do not touch to check looseness.", "Have the mounting, fasteners and rotating assembly inspected by a technician."), true, "Unexpected vibration can indicate a loose or damaged rotating part."),
            FarmMachineSymptom("control", "Machine pulls or control feels abnormal", listOf("Release the controls and stop in a clear area.", "Do not continue on slopes or near people.", "Arrange inspection of steering, clutch and drive system.")
        ))),
        FarmMachineCategory("rotavator", "Rotavator / rotary tiller", "Tractor or power-tiller mounted", "PTO entanglement, blades, thrown stones and stored mechanical energy", listOf(
            FarmMachineSymptom("poor_tilling", "Poor soil cutting", listOf("Stop PTO, switch off engine, remove key and wait for all motion to stop.", "Observe working depth and soil condition from a safe distance.", "Never clear wrapped grass or debris by hand; use the exact manual and trained service."), true, "Rotating blades can continue moving after power is disengaged."),
            FarmMachineSymptom("noise", "Metal noise or vibration", listOf("Disengage PTO and stop the tractor safely.", "Keep people away from the implement.", "Do not operate until a technician checks blades, gearbox and driveline."), true, "Damaged blades or driveline components may be ejected.")
        )),
        FarmMachineCategory("pump", "Irrigation / water pump", "Electric, diesel, petrol, solar and submersible pumps", "Electric shock, dry running, pressure, fuel and water hazards", listOf(
            FarmMachineSymptom("no_water", "No water / low flow", listOf("Record pump type, brand, model and any controller display code.", "Observe source water level and visible valve positions from a dry safe place.", "Do not loosen pipes, prime a hot pump or touch electrical terminals.")),
            FarmMachineSymptom("overheat", "Pump or motor gets hot", listOf("Stop using the normal safe control if dry and accessible.", "Keep away from hot surfaces and wet electrical areas.", "Do not restart until the cause is checked by a qualified technician."), true, "Hot motor, burning smell or smoke is a stop-work condition."),
            FarmMachineSymptom("leak", "Pipe or pump leakage", listOf("Keep clear if water is near wiring or the motor.", "Do not tighten pressurised fittings or open a pump casing.", "Record the visible leak location for the technician."), true, "Water around powered electrical equipment can cause fatal shock.")
        )),
        FarmMachineCategory("diesel_engine", "Diesel / petrol agricultural engine", "Portable irrigation engines and stationary engines", "Hot exhaust, fuel fire, carbon monoxide, belts and moving parts", listOf(
            FarmMachineSymptom("no_start", "Engine not starting", listOf("Use outdoors only, away from doors and windows.", "Record visible warning lamps and whether it cranks.", "Do not open injection lines, carburettors or fuel-system parts.")),
            FarmMachineSymptom("smoke", "Heavy smoke or fuel smell", listOf("Stop if safe, move people away and keep clear of hot exhaust.", "Do not refuel while running or hot.", "Do not restart until inspected by a qualified mechanic."), true, "Fuel leak, smoke or exhaust exposure may cause fire or poisoning.")
        )),
        FarmMachineCategory("sprayer", "Knapsack / power / tractor sprayer", "Manual, battery, petrol and PTO-driven sprayers", "Chemical exposure, high pressure, drift and rotating pump parts", listOf(
            FarmMachineSymptom("low_pressure", "Low spray pressure", listOf("Stop spraying and keep PPE on as required by the chemical label.", "Record the pressure gauge reading and nozzle pattern from a safe distance.", "Do not put mouth to a nozzle, blow through a line or open a pressurised system.")),
            FarmMachineSymptom("leak", "Chemical leakage", listOf("Stop work, move upwind and prevent people/animals entering the area.", "Follow the pesticide label and local poison-control/emergency advice for exposure.", "Do not touch or wash chemical spills into drains without label guidance."), true, "Chemical contact or inhalation can cause serious poisoning.")
        )),
        FarmMachineCategory("thresher", "Paddy / multi-crop thresher", "Engine, motor, tractor or power-tiller driven", "Feed rollers, rotating drum, belts, dust and entanglement", listOf(
            FarmMachineSymptom("poor_thresh", "Grain not separating properly", listOf("Stop power, isolate from the operator's safe control and wait for all movement to stop.", "Record crop type, moisture condition and machine model.", "Never push crop by hand or open guards while running."), true, "Feed rollers and threshing drums can cause fatal entanglement."),
            FarmMachineSymptom("blockage", "Choking / blockage", listOf("Stop engine/PTO, remove key and wait for all moving parts to stop.", "Keep hands and tools away until the manual's isolation procedure is followed by a trained operator.", "Do not restart with guards removed."), true, "Stored rotation and unexpected restart can cause severe injury.")
        )),
        FarmMachineCategory("combine", "Combine harvester", "Paddy, wheat and multi-crop harvesters", "Header, auger, threshing drum, straw chopper and hydraulic systems", listOf(
            FarmMachineSymptom("header", "Header not cutting or feeding", listOf("Stop on level ground, disengage header and follow the manual's full shutdown/isolation procedure.", "Keep clear of cutter bar and auger.", "Do not clear crop wrapping by hand or work beneath raised equipment."), true, "Header and raised equipment can move or fall unexpectedly."),
            FarmMachineSymptom("warning", "Warning lamp / display code", listOf("Photograph the exact display and record machine model, year and engine hours.", "Stop if temperature, oil pressure, fire or hydraulic warnings appear.", "Do not clear codes before recording them.")
        ))),
        FarmMachineCategory("reaper", "Reaper / reaper-binder", "Self-propelled, power-tiller or tractor mounted", "Cutter bar, belts, blades and moving crop conveyors", listOf(
            FarmMachineSymptom("not_cutting", "Not cutting evenly", listOf("Stop drive and engine and wait for cutter bar motion to stop.", "Observe crop condition and cutting height from a safe distance.", "Do not touch blades or adjust while powered."), true, "Cutter bar blades can cause severe cuts."),
            FarmMachineSymptom("noise", "Unusual noise", listOf("Stop operation and keep bystanders away.", "Record when the noise starts and any visible vibration.", "Have belts, guards and drive parts inspected.")
        ))),
        FarmMachineCategory("chaff_cutter", "Chaff / fodder cutter", "Manual, electric motor, engine or tractor PTO driven", "Sharp blades, feed rollers and entanglement", listOf(
            FarmMachineSymptom("not_cutting", "Fodder not cutting", listOf("Stop and isolate the power source; wait for all parts to stop.", "Never push fodder with hands or feet.", "Use only the supplied feed tool and procedure in the exact manual."), true, "Feed rollers can pull hands or clothing into the cutter."),
            FarmMachineSymptom("jam", "Machine jammed", listOf("Stop power, isolate and prevent accidental restart.", "Do not reach into the feed chute or blade housing.", "Call a trained operator/technician for clearing.")
        ))),
        FarmMachineCategory("rice_mill", "Rice mill / huller / polisher", "Mini rice mill, huller and polishing units", "Belts, rollers, dust, electrical motors and hot bearings", listOf(
            FarmMachineSymptom("poor_polish", "Poor milling / polishing", listOf("Stop machine before inspecting external feed settings.", "Record crop moisture, feed rate setting and model.", "Do not open roller or huller housing while connected to power.")),
            FarmMachineSymptom("dust", "Excess dust or overheating", listOf("Stop operation if smoke, burning smell or excessive heat occurs.", "Keep ventilation clear and avoid inhaling dust.", "Have bearings, belts and dust extraction checked by service staff."), true, "Dust and overheated components can create respiratory and fire hazards.")
        ))),
        FarmMachineCategory("maize_sheller", "Maize sheller / groundnut decorticator", "Manual and powered shellers/decorticators", "Rotating rollers, belts, pinch points and thrown material", listOf(
            FarmMachineSymptom("poor_shell", "Poor shelling", listOf("Stop and isolate power before observing settings.", "Record crop moisture and model/setting.", "Do not reach into rollers or feed opening.")),
            FarmMachineSymptom("jam", "Jammed feed", listOf("Stop power and wait for all moving parts to stop.", "Keep hands and clothing away from rollers.", "Use the exact manual's safe clearing method or call a technician."), true, "Roller pinch points can crush fingers.")
        ))),
        FarmMachineCategory("brush_cutter", "Brush cutter / grass trimmer", "Petrol, battery and electric types", "High-speed blade, thrown stones, fuel and noise", listOf(
            FarmMachineSymptom("not_start", "Not starting", listOf("Keep cutting head on the ground and away from people.", "Record model and visible switch position.", "Do not touch spark plug, fuel system or blade while hot.")),
            FarmMachineSymptom("vibration", "Strong vibration / damaged head", listOf("Stop engine and wait for the head to stop completely.", "Do not use a cracked blade, damaged guard or loose cutting head.", "Have the unit inspected before reuse."), true, "A damaged high-speed cutting attachment can break apart.")
        ))),
        FarmMachineCategory("power_weeder", "Power weeder", "Walk-behind and mini power weeders", "Rotating tines, hot engine, loss of control", listOf(
            FarmMachineSymptom("poor_weeding", "Not removing weeds", listOf("Stop engine and wait for tines to stop.", "Record soil condition, working width and model.", "Do not remove weeds wrapped around tines by hand."), true, "Tines can rotate unexpectedly and cause severe injury."),
            FarmMachineSymptom("control", "Unstable movement", listOf("Stop in a clear area and release drive controls.", "Do not operate on steep or slippery ground.", "Arrange inspection of clutch, steering and wheels.")
        ))),
        FarmMachineCategory("transplanter", "Paddy transplanter / seed drill", "Walk-behind and riding transplanters; seed/fertilizer drills", "Moving planting arms, pinch points, PTO and chemical/fertilizer contact", listOf(
            FarmMachineSymptom("uneven", "Uneven planting / seed spacing", listOf("Stop drive and engine before inspecting visible settings.", "Record crop, row setting and model.", "Keep hands away from planting arms and metering mechanisms.")),
            FarmMachineSymptom("jam", "Seedling or seed blockage", listOf("Stop and isolate all power; wait for moving parts to stop.", "Do not clear a blockage while the mechanism is moving.", "Follow exact manual or call trained service."), true, "Planting mechanisms can trap fingers.")
        ))),
        FarmMachineCategory("solar_pump", "Solar water pump controller", "Solar irrigation pumps and inverter/controller units", "DC electrical shock, arc, water and high-voltage PV strings", listOf(
            FarmMachineSymptom("no_output", "Pump not running / controller error", listOf("Photograph the exact controller display and record pump/controller model.", "Observe only external status LEDs and water-source level.", "Do not open controller, disconnect PV wires or measure live DC terminals."), true, "Solar DC circuits can remain energised in daylight even when a switch is off."),
            FarmMachineSymptom("overheat", "Controller hot / burning smell", listOf("Keep clear and stop using the system only from its safe external isolator if dry and accessible.", "Keep water away from electrical equipment.", "Call a solar pump technician; do not open the enclosure."), true, "Burning smell or heat at electrical equipment requires stop-work.")
        ))),
        FarmMachineCategory("generator", "Farm generator / genset", "Diesel, petrol and LPG generators", "Carbon monoxide, fuel, exhaust, hot surfaces and electricity", listOf(
            FarmMachineSymptom("no_start", "Not starting", listOf("Operate outdoors only, far from doors/windows and people.", "Record model, fuel type, display and indicator state.", "Never refuel while hot or running.")),
            FarmMachineSymptom("no_power", "No electrical output", listOf("Disconnect loads only using safe external controls as directed by the manual.", "Do not touch sockets, terminals or internal wiring.", "Arrange qualified electrical/generator service."), true, "Generator output can cause lethal electric shock.")
        ))),
        FarmMachineCategory("winnower", "Winnower / grain cleaner / grader", "Manual, electric and engine-driven grain cleaning units", "Fans, belts, dust and rotating screens", listOf(
            FarmMachineSymptom("poor_cleaning", "Grain not cleaning evenly", listOf("Stop the machine before observing external airflow or feed settings.", "Record crop type, moisture and machine model.", "Keep hands away from fan, belts and screens while powered.")),
            FarmMachineSymptom("dust", "Excessive dust or vibration", listOf("Stop operation if dust becomes unsafe or vibration is unusual.", "Keep bystanders away and use suitable dust protection.", "Have fan, screen and bearings inspected by service staff."), true, "Dust exposure and rotating parts can injure operators.")
        )),
        FarmMachineCategory("grain_dryer", "Grain dryer / seed dryer", "Batch, flat-bed and mechanical grain dryers", "Hot air, burners, fuel, electricity and fire", listOf(
            FarmMachineSymptom("not_drying", "Grain not drying evenly", listOf("Record crop, initial/final moisture readings and controller display.", "Observe only external air inlet/outlet and alarms.", "Do not open burner, heater or electrical compartments.")),
            FarmMachineSymptom("overheat", "High temperature / smoke", listOf("Stop the dryer using the safe external control and move people away.", "Do not open hot chambers or add water to electrical/burner equipment.", "Call trained service before restarting."), true, "Smoke, flame or overheating is an immediate fire risk.")
        )),
        FarmMachineCategory("feed_mixer", "Feed grinder / hammer mill / mixer", "Small farm feed mills and grinder-cum-mixer units", "High-speed hammers, belts, dust and electrical motors", listOf(
            FarmMachineSymptom("poor_grind", "Feed not grinding properly", listOf("Stop and isolate power before checking external feed settings.", "Record material type and machine model.", "Never reach into hopper, grinder or discharge chute.")),
            FarmMachineSymptom("jam", "Machine jam or loud impact", listOf("Stop power and wait for every moving part to stop.", "Do not open the grinding chamber or remove guards.", "Arrange inspection by a trained operator/technician."), true, "Hammer mills can retain dangerous rotational energy.")
        )),
        FarmMachineCategory("root_digger", "Potato / ginger / turmeric digger", "Tractor, power-tiller and animal-drawn diggers", "PTO, rotating chains, digging blades and raised implements", listOf(
            FarmMachineSymptom("poor_digging", "Crop not lifting / digging unevenly", listOf("Stop drive and PTO before observing working depth or soil condition.", "Record crop, soil condition and implement model.", "Do not adjust blades or chains while connected to power.")),
            FarmMachineSymptom("noise", "Chain noise or vibration", listOf("Stop tractor/engine, remove key and wait for all movement to stop.", "Keep clear of chains, blades and raised parts.", "Have the driveline and digging assembly inspected."), true, "Chains and blades can trap or cut hands.")
        )),
        FarmMachineCategory("areca_harvester", "Areca nut / orchard harvester", "Pole-mounted and powered climbing/harvesting equipment", "Falls from height, falling fruit, blades and electrical equipment", listOf(
            FarmMachineSymptom("not_working", "Harvester not working", listOf("Keep the equipment on the ground and away from overhead power lines.", "Record model and visible fault indicators.", "Do not climb or use improvised ladders to inspect the unit.")),
            FarmMachineSymptom("damage", "Cable, pole or cutting head damage", listOf("Stop use and keep people away from the drop zone.", "Do not operate with cracked poles, damaged ropes or exposed conductors.", "Arrange trained inspection before use."), true, "Falls and falling objects can cause fatal injury.")
        )),
        FarmMachineCategory("tea_harvester", "Tea leaf plucker / tea garden machine", "Handheld and ride-on tea pruning/harvesting equipment", "Rotating blades, sharp cutters, fuel and uneven terrain", listOf(
            FarmMachineSymptom("uneven_cut", "Uneven plucking / cutting", listOf("Stop the machine and wait for all cutting parts to stop.", "Record crop row, cutting height setting and model.", "Do not adjust cutter blades while powered.")),
            FarmMachineSymptom("fuel_smell", "Fuel smell / smoke / overheating", listOf("Stop if safe, move away from ignition sources and keep clear of hot parts.", "Do not refuel while running or hot.", "Arrange qualified service before restart."), true, "Fuel leaks and hot engines can cause fire.")
        )),
        FarmMachineCategory("tillage_implements", "Plough / harrow / cultivator / land leveller", "Disc plough, MB plough, disc harrow, rotary plough, subsoiler, cultivator and laser/mechanical leveller", "Tractor movement, PTO shafts, hydraulic lifting and heavy raised implements", listOf(
            FarmMachineSymptom("poor_tillage", "Soil not turning or leveling evenly", listOf("Stop tractor and disengage PTO before observing implement alignment.", "Record soil condition, working depth and implement model.", "Never work beneath a raised implement or adjust linkages while hydraulics are active."), true, "Raised implements can fall suddenly; PTO and tractor movement can cause fatal injuries."),
            FarmMachineSymptom("hydraulic", "Implement not lifting / lowering normally", listOf("Stop in a level safe area and lower the implement to the ground if the operator manual permits.", "Keep clear of hydraulic arms and suspended loads.", "Do not loosen hydraulic hoses or fittings; high-pressure oil injection is dangerous."), true, "Hydraulic systems can retain high pressure and suspended equipment can drop.")
        )),
        FarmMachineCategory("planting_equipment", "Seed drill / planter / potato planter", "Seed-cum-fertilizer drill, pneumatic planter, potato planter and sugarcane planter", "Moving metering rollers, PTO, fertilizer exposure and pinch points", listOf(
            FarmMachineSymptom("uneven_seeding", "Uneven seed spacing / missed rows", listOf("Stop the tractor and machine before observing external row markers or settings.", "Record crop, seed type, row spacing setting and machine model.", "Keep hands away from seed metering rollers and moving chains.")),
            FarmMachineSymptom("blockage", "Seed or fertilizer blockage", listOf("Stop engine/PTO, lower equipment and wait for all movement to stop.", "Avoid direct contact with fertilizer and treated seeds; follow label precautions.", "Do not clear a blockage while rollers or drive chains are moving."), true, "Metering mechanisms and rotating drive parts can trap fingers.")
        )),
        FarmMachineCategory("fodder_harvester", "Fodder harvester / forage chopper", "Tractor PTO and self-propelled fodder harvesting machines", "PTO entanglement, cutter drums, feed rollers and thrown material", listOf(
            FarmMachineSymptom("poor_cut", "Fodder not cutting evenly", listOf("Stop engine/PTO and wait for cutter drum and feed rollers to stop.", "Record crop type, feed condition and model.", "Never push crop by hand or open the cutter housing while powered."), true, "Feed rollers and cutter drums can cause fatal entanglement."),
            FarmMachineSymptom("jam", "Chute or cutter blockage", listOf("Stop power, remove key and prevent accidental restart.", "Keep clear of the feed chute and stored rotating parts.", "Use only the exact manufacturer's isolation and clearing procedure through a trained operator."), true, "Stored mechanical energy can cause unexpected movement.")
        )),
        FarmMachineCategory("mulcher_baler", "Mulcher / straw baler", "Rotary mulcher, round straw baler and crop-residue machines", "PTO shafts, pickup tines, belts, hydraulic compaction and fire", listOf(
            FarmMachineSymptom("poor_mulching", "Residue not mulching evenly", listOf("Disengage PTO, stop engine and wait for all rotor motion to stop.", "Observe residue condition and working height from a safe distance.", "Do not reach under the deck or remove wrapped crop by hand."), true, "Rotor blades and pickup tines can cause severe injury."),
            FarmMachineSymptom("baler_blockage", "Baler blockage / knotting issue", listOf("Stop tractor and baler, remove key and follow the model-specific isolation procedure.", "Stay clear of the bale chamber, belts and hydraulic components.", "Never enter or reach into a baler chamber."), true, "Bale chambers and hydraulic mechanisms can crush or trap a person.")
        )),
        FarmMachineCategory("dairy", "Dairy milking machine / milk chiller", "Vacuum pump, milking cluster and bulk milk cooler", "Electrical equipment near water, vacuum, rotating pumps and hygiene", listOf(
            FarmMachineSymptom("low_vacuum", "Low vacuum / weak suction", listOf("Stop milking if animal welfare or equipment safety is affected.", "Record gauge reading and visible alarms.", "Do not open vacuum pump or electrical panels; clean only parts specified by the hygiene manual.")),
            FarmMachineSymptom("not_cooling", "Milk chiller not cooling", listOf("Keep milk handling hygienic and follow food-safety procedures.", "Record temperature display and alarm code.", "Do not open refrigerant circuits or electrical covers; arrange service."), true, "Milk temperature failure can create food-safety risks.")
        )))
    )

    fun category(id: String): FarmMachineCategory? = categories.firstOrNull { it.id == id }

    fun advice(categoryId: String, symptomId: String): TroubleshootingAdvice? {
        val category = category(categoryId) ?: return null
        val symptom = category.symptoms.firstOrNull { it.id == symptomId } ?: return null
        return TroubleshootingAdvice(
            title = "${category.label}: ${symptom.label}",
            steps = symptom.safeObservations,
            stopWork = symptom.stopWork,
            warning = symptom.stopReason ?: "Observation only. This is not a diagnosis. Confirm instructions in the exact manufacturer manual or contact a trained agricultural machinery technician."
        )
    }

    fun universalSafetyNote(): String =
        "Farm machinery stop rule: switch off from the operator's safe control, remove the key and wait for every moving part to stop before leaving the operating position. Never clear jams by hand, reach into feed chutes, approach PTO shafts, remove guards while running, work beneath raised equipment, open live electrical panels, or operate engines indoors. Use trained service for fuel, hydraulic, chemical, blade, PTO and electrical faults."
}
