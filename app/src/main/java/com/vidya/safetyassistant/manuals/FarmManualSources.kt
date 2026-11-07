package com.vidya.safetyassistant.manuals

data class FarmManualSource(
    val name: String,
    val description: String,
    val url: String
)

/** Public Indian government reference portals for agricultural machinery categories/specifications. */
object FarmManualSources {
    val entries = listOf(
        FarmManualSource("North Eastern Region Farm Machinery Training & Testing Institute (Assam)", "Official specifications and training/testing information for rotavators, power tillers, brush cutters, chaff cutters, rice transplanters, threshers, rice mills, power weeders and more.", "https://nerfmtti.nic.in/test_application.html"),
        FarmManualSource("KIRAN — Farm Tools (North East)", "North-East focused farm-tool and crop-operation guide covering tillage, paddy, planting, weeding, spraying, harvesting, threshing and winnowing.", "https://kiran.nic.in/farm_tools.html"),
        FarmManualSource("KIRAN — Agricultural Engineering FAQ", "Regional guidance on hill paddy machinery, power tillers, transplanters, sprayers, reapers, threshers and cono weeders.", "https://kiran.nic.in/faq_engg.html"),
        FarmManualSource("Sub-Mission on Agricultural Mechanization", "Government of India scheme guidelines listing eligible farm machinery categories and equipment types.", "https://agrimachinery.nic.in/Files/Guidelines/Guidelines_SMAM2024.pdf"),
        FarmManualSource("Bureau of Indian Standards — Farm Machinery Standards", "Reference list of Indian Standards for tractors, power tillers, combine harvesters and rotary tillers.", "https://www.bis.gov.in/wp-content/uploads/2021/09/Indian-Standards-on-AGRICULTURE.pdf")
    )
}
