package com.vidya.safetyassistant.manuals

data class OfficialManualSource(
    val brand: String,
    val vehicleTypes: Set<String>,
    val description: String,
    val url: String,
    val sourceType: String = "Official manufacturer portal"
)

/**
 * Curated entry points to official manufacturer/manual pages.
 * These are source links, not a bundled or mirrored copy of copyrighted manuals.
 * Model/year/variant applicability must still be confirmed on the source website.
 */
object OfficialManualSources {
    val entries = listOf(
        OfficialManualSource("Honda 2Wheelers India", setOf("Bike", "Scooter", "EV"), "Select segment, model and year; owner's manuals.", "https://www.honda2wheelersindia.com/resources/owners-manual"),
        OfficialManualSource("Hero MotoCorp", setOf("Bike", "Scooter", "EV"), "Select your two-wheeler and download its owner's manual.", "https://www.heromotocorp.com/en-in/services/owner-manual.html"),
        OfficialManualSource("TVS Motor", setOf("Bike", "Scooter", "EV"), "Model and manufacturing-date-specific user manuals.", "https://www.tvsmotor.com/our-service/user-manual"),
        OfficialManualSource("Bajaj Auto", setOf("Bike", "Scooter", "EV"), "Owner's manual portal.", "https://www.bajajauto.com/customer-service/owners-manual"),
        OfficialManualSource("Suzuki Motorcycle India", setOf("Bike", "Scooter", "EV"), "Current and previous model user manuals.", "https://www.suzukimotorcycle.co.in/User-Manual"),
        OfficialManualSource("Royal Enfield", setOf("Bike"), "Motorcycle owner's manuals; some downloads require form submission.", "https://www.royalenfield.com/in/en/support/owners-manual/"),
        OfficialManualSource("Maruti Suzuki", setOf("Car"), "Arena car manuals with model and powertrain variants.", "https://www.marutisuzuki.com/arena/car-manuals"),
        OfficialManualSource("Hyundai India", setOf("Car", "EV"), "Owner manuals grouped by model and model-year period.", "https://www.hyundai.com/in/en/connect-to-service/hyundai-service/owners-manual"),
        OfficialManualSource("Tata Motors Cars", setOf("Car"), "Official owner manual entry point for passenger vehicles; confirm exact model and variant.", "https://cars.tatamotors.com/service/owners/owners-manual"),
        OfficialManualSource("Tata.ev", setOf("Car", "EV"), "Official Tata electric vehicle support and owner manual catalogue.", "https://ev.tata.cars/support.html"),
        OfficialManualSource("Mahindra Owner Manuals", setOf("Car", "EV"), "Public model-wise owner manual portal; workshop technical data may have separate access rules.", "https://mvault.mahindra.com/owners_manual"),
        OfficialManualSource("Mahindra TIS", setOf("Car", "EV", "Truck"), "Owner manuals and technical service information; some technical material is access-restricted.", "https://mahindra-tis.com/en/"),
        OfficialManualSource("Government of India — Right to Repair", setOf("Bike", "Scooter", "Car", "EV"), "Department of Consumer Affairs portal for registered-brand repair/support information.", "https://doca.gov.in/r2r-dbim/index.php")
    )

    fun forVehicleType(vehicleType: String): List<OfficialManualSource> =
        entries.filter { source -> source.vehicleTypes.any { it.equals(vehicleType, ignoreCase = true) } }
}
