package com.vidya.safetyassistant.equipment

/**
 * India-market farm equipment brand suggestions.
 * This is a typing aid only: it does not prove a brand makes every machine
 * in a category or establish model compatibility.
 */
object FarmMachineBrandCatalog {
    private val brandsByGroup = mapOf(
        "tractor" to listOf("ACE", "Captain", "Eicher", "Farmtrac", "Force", "Indo Farm", "John Deere", "Kubota", "Mahindra", "Massey Ferguson", "New Holland", "Preet", "Powertrac", "Sonalika", "Standard", "Swaraj", "TAFE", "Trakstar"),
        "power_tiller" to listOf("Balwaan", "BCS", "Honda", "KAMCO", "Kubota", "Shrachi", "VST Shakti"),
        "rotavator" to listOf("Fieldking", "John Deere", "Kubota", "Mahindra", "Shaktiman", "Sonalika", "Swaraj"),
        "pump" to listOf("Crompton", "CRI Pumps", "Kirloskar", "KSB", "Lubi", "Shakti Pumps", "Texmo", "V-Guard"),
        "diesel_engine" to listOf("Cooper", "Greaves", "Kirloskar", "Mahindra Powerol", "Honda", "Yanmar"),
        "sprayer" to listOf("Aspee", "Balwaan", "KisanKraft", "Neptune", "Shakti", "Stihl"),
        "thresher" to listOf("Dasmesh", "Fieldking", "Kartar", "New Hind", "Preet", "Shaktiman"),
        "combine" to listOf("ACE", "Claas", "John Deere", "Kubota", "New Holland", "Preet", "Sonalika"),
        "reaper" to listOf("Dasmesh", "Kartar", "KisanKraft", "Preet", "Shrachi"),
        "chaff_cutter" to listOf("Cooper", "Greaves", "KisanKraft", "Neptune", "Shrachi"),
        "rice_mill" to listOf("Bharat", "GEM", "Hindustan", "KisanKraft", "local manufacturer / other"),
        "maize_sheller" to listOf("KisanKraft", "Shrachi", "local manufacturer / other"),
        "brush_cutter" to listOf("Echo", "Honda", "Husqvarna", "KisanKraft", "Makita", "Stihl"),
        "power_weeder" to listOf("Balwaan", "BCS", "Honda", "KAMCO", "KisanKraft", "VST Shakti"),
        "transplanter" to listOf("Kubota", "KAMCO", "Yanmar", "local manufacturer / other"),
        "solar_pump" to listOf("Crompton", "CRI Pumps", "Kirloskar", "Shakti Pumps", "Tata Power Solar", "Waaree"),
        "generator" to listOf("Cooper", "Cummins", "Greaves", "Honda", "Kirloskar", "Mahindra Powerol"),
        "winnower" to listOf("KisanKraft", "local manufacturer / other"),
        "grain_dryer" to listOf("local manufacturer / other"),
        "feed_mixer" to listOf("KisanKraft", "local manufacturer / other"),
        "root_digger" to listOf("Fieldking", "Khedut", "Shaktiman", "local manufacturer / other"),
        "areca_harvester" to listOf("KisanKraft", "local manufacturer / other"),
        "tea_harvester" to listOf("Honda", "KisanKraft", "Stihl", "local manufacturer / other"),
        "tillage_implements" to listOf("Fieldking", "Khedut", "Landforce", "Lemken", "Shaktiman", "Sonalika", "Swaraj"),
        "planting_equipment" to listOf("Captain", "Fieldking", "Khedut", "Kubota", "Shaktiman", "Sonalika"),
        "fodder_harvester" to listOf("John Deere", "New Holland", "Preet", "Shaktiman"),
        "mulcher_baler" to listOf("Fieldking", "Kartar", "New Holland", "Preet", "Shaktiman"),
        "dairy" to listOf("DeLaval", "GEA", "JAP", "local manufacturer / other")
    )

    fun brandsForCategory(categoryId: String): List<String> =
        brandsByGroup[categoryId].orEmpty()

    fun suggestions(categoryId: String, query: String): List<String> {
        val brands = brandsForCategory(categoryId)
        if (query.isBlank()) return brands
        return brands.filter { it.contains(query.trim(), ignoreCase = true) }
    }
}
