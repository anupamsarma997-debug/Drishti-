package com.vidya.safetyassistant.manuals

data class OfficialPartsSource(
    val brand: String,
    val machineTypes: Set<String>,
    val description: String,
    val url: String
)

/**
 * Official manufacturer parts/support entry points.
 * Link-out only: these entries are not a local parts database or a guarantee of fitment.
 */
object OfficialPartsSources {
    val entries = listOf(
        OfficialPartsSource(
            "Honda Motorcycle & Scooter India",
            setOf("Bike", "Scooter", "EV"),
            "Honda genuine-parts guidance and authenticity markers; confirm the exact model/part with an authorised distributor.",
            "https://www.honda2wheelersindia.com/honda-genuine-parts"
        ),
        OfficialPartsSource(
            "Hero MotoCorp",
            setOf("Bike", "Scooter"),
            "Official Hero Genuine Parts portal. Select the exact vehicle and verify part compatibility before purchase.",
            "https://www.heromotocorp.com/en-in/services/hero-genuine-parts.html"
        ),
        OfficialPartsSource(
            "Bajaj Auto",
            setOf("Bike", "Scooter"),
            "Bajaj Genuine Parts information and distributor locator; this is an older official portal, so verify current availability with Bajaj support.",
            "https://legacy.bajajauto.com/customer-service/spares"
        ),
        OfficialPartsSource(
            "John Deere India",
            setOf("Tractor", "Farm Machine"),
            "Official India parts-and-services page with a Find Parts entry point and diagrams. Use exact model and serial details on the manufacturer site.",
            "https://www.deere.co.in/en-in/parts-owner-support/parts-and-services"
        ),
        OfficialPartsSource(
            "Swaraj Tractors",
            setOf("Tractor", "Farm Machine"),
            "Genuine-parts availability and authenticity guidance through authorised dealers; not a downloadable exploded-parts catalogue.",
            "https://www.swarajtractors.com/genuine-parts"
        )
    )

    fun forMachineType(machineType: String): List<OfficialPartsSource> =
        entries.filter { entry -> entry.machineTypes.any { it.equals(machineType, ignoreCase = true) } }
}
