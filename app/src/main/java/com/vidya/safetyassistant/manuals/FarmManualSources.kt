package com.vidya.safetyassistant.manuals

data class FarmManualSource(
    val name: String,
    val description: String,
    val url: String
)

/** Public Indian government reference portals for agricultural machinery categories/specifications. */
object FarmManualSources {
    val entries = listOf(
        FarmManualSource("Southern Region Farm Machinery Training & Testing Institute — Testing and Standards", "Official Government of India testing catalogue with agricultural machinery standards metadata, including tractor preventive maintenance, operator manual requirements, operator controls and PTO guards. Standard indexes are not substitutes for licensed full standards or model-specific repair manuals.", "https://srfmtti.dacnet.nic.in/Testing.html"),
        FarmManualSource("Government of India — Right to Repair India", "Official repair-support discovery portal for participating brands across farming equipment, vehicles, consumer durables and electronics. Check exact brand/model availability and follow the portal's own terms.", "https://www.righttorepairindia.gov.in/"),
        FarmManualSource("ICAR — Publications", "Official catalogue for agricultural publications, including farm machinery principles and maintenance references. Check each publication's reuse rights before copying or indexing its content.", "https://icar.gov.in/en/all-publications"),
        FarmManualSource("Right to Repair India — Covered product categories", "Official category index explicitly includes tractor parts, harvesters and water-pump motors under farming equipment. Use it to discover relevant brand support; it is not a downloadable workshop-manual repository.", "https://righttorepairindia.gov.in/sectorcoverd"),
        FarmManualSource("ICAR — Farm Machinery Maintenance and Management", "Official ICAR publication catalogue entry for farm machinery maintenance and management. Check access and copyright before using text in Vidya.", "https://icar.org.in/en/product/44"),
        FarmManualSource("ICAR — Farm Machinery Principles and Applications", "Official ICAR publication catalogue entry covering farm machinery principles and applications. Use as a reference discovery link unless reuse permission is confirmed.", "https://www.icar.org.in/en/product/46"),
        FarmManualSource("ICAR — Agricultural Engineering curriculum", "Official ICAR e-book/curriculum page describing tractor systems, farm machinery and scheduled maintenance topics. This is educational metadata, not a model-specific repair manual.", "https://ebook.icar.org.in/index.php/bookprocess/ugengg_list"),
        FarmManualSource("NERFMTTI — Machinery available for training", "Official Assam institute inventory covering tractors, power tillers, ploughs, harrows, levellers, planters, transplanters, power weeders, reapers, fodder harvesters, threshers, mini combines, grain dryers, mulchers and balers.", "https://nerfmtti.nic.in/trg_machineries.html"),
        FarmManualSource("Government of India — Farm Mechanization Portal", "Central government portal for farm machinery performance testing, testing institutes, mechanization resources and FARMS app information.", "https://agrimachinery.nic.in/"),
        FarmManualSource("North Eastern Region Farm Machinery Training & Testing Institute (Assam)", "Official specifications and training/testing information for rotavators, power tillers, brush cutters, chaff cutters, rice transplanters, threshers, rice mills, power weeders and more.", "https://nerfmtti.nic.in/test_application.html"),
        FarmManualSource("KIRAN — Farm Tools (North East)", "North-East focused farm-tool and crop-operation guide covering tillage, paddy, planting, weeding, spraying, harvesting, threshing and winnowing.", "https://kiran.nic.in/farm_tools.html"),
        FarmManualSource("KIRAN — Agricultural Engineering FAQ", "Regional guidance on hill paddy machinery, power tillers, transplanters, sprayers, reapers, threshers and cono weeders.", "https://kiran.nic.in/faq_engg.html"),
        FarmManualSource("Sub-Mission on Agricultural Mechanization", "Government of India scheme guidelines listing eligible farm machinery categories and equipment types.", "https://agrimachinery.nic.in/Files/Guidelines/Guidelines_SMAM2024.pdf"),
        FarmManualSource("Bureau of Indian Standards — Farm Machinery Standards", "Reference list of Indian Standards for tractors, power tillers, combine harvesters and rotary tillers.", "https://www.bis.gov.in/wp-content/uploads/2021/09/Indian-Standards-on-AGRICULTURE.pdf"),
        FarmManualSource("Directorate General of Training — Mechanic Agricultural Machinery curriculum", "Official vocational-training curriculum covering service and troubleshooting of irrigation systems, power tillers, power weeders and agricultural implements. Educational training reference only; not an OEM model-specific repair manual.", "https://www.dgt.gov.in/sites/default/files/2023-12/Mech.%20Agricultural%20Machinery_CTS2.0_NSQF-4.pdf"),
        FarmManualSource("ICAR — Agricultural Engineering machinery catalogue", "Official ICAR educational catalogue describing Indian farm machinery such as seed drills, planters, harvesters, reapers, threshers and winnowers. Use for machine-category discovery; verify publication rights before reproducing content.", "https://icar.org.in/sites/default/files/inline-files/AgriculturalEngineering_1.pdf")
    )
}
