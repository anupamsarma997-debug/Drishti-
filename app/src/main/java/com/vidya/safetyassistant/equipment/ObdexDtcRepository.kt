package com.vidya.safetyassistant.equipment

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads the independently maintained generic OBD-II reference once and
 * keeps a validated copy on-device for later offline lookups.
 *
 * It deliberately imports no repair-cost estimates or repair procedures.
 */
class ObdexDtcRepository(context: Context) {
    private val cacheFile = File(context.applicationContext.filesDir, "knowledge/obdex-generic-v1.json")
    @Volatile private var cachedEntries: Map<String, ObdexDtcEntry>? = null

    fun refresh(): Int {
        val connection = (URL(DATA_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 45_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Vidya-Android/1.0")
        }
        try {
            check(connection.responseCode == HttpURLConnection.HTTP_OK) {
                "OBDex source returned HTTP ${connection.responseCode}"
            }
            val bytes = connection.inputStream.use { readBounded(it, MAX_DOWNLOAD_BYTES) }
            require(bytes.isNotEmpty() && bytes.size <= MAX_DOWNLOAD_BYTES) {
                "OBDex data size is outside the allowed limit"
            }
            val entries = parseEntries(bytes.toString(Charsets.UTF_8))
            require(entries.size >= MINIMUM_VALID_ENTRIES) {
                "Downloaded OBDex file is incomplete (${entries.size} entries)"
            }
            val unique = entries.associateBy { it.code }
            require(unique.size == entries.size) { "OBDex contains duplicate DTC identifiers" }
            cacheFile.parentFile?.mkdirs()
            val temporary = File(cacheFile.parentFile, cacheFile.name + ".tmp")
            val backup = File(cacheFile.parentFile, cacheFile.name + ".bak")
            temporary.writeBytes(bytes)
            if (backup.exists()) backup.delete()
            val hadPreviousCache = cacheFile.exists()
            if (hadPreviousCache) {
                check(cacheFile.renameTo(backup)) {
                    temporary.delete()
                    "Could not protect the previous offline database"
                }
            }
            if (!temporary.renameTo(cacheFile)) {
                if (hadPreviousCache) backup.renameTo(cacheFile)
                temporary.delete()
                error("Could not save OBDex data for offline use")
            }
            backup.delete()
            cachedEntries = unique
            return unique.size
        } finally {
            connection.disconnect()
        }
    }

    fun lookupOffline(rawCode: String): VehicleDiagnosticResult? {
        val code = rawCode.trim().uppercase().replace(" ", "")
        if (!Regex("[PBCU][0-9A-F]{4}").matches(code)) return null
        val entries = cachedEntries ?: loadCache().also { cachedEntries = it }
        return entries[code]?.toDiagnosticResult()
    }

    fun cachedCount(): Int {
        val entries = cachedEntries ?: loadCache().also { cachedEntries = it }
        return entries.size
    }

    private fun loadCache(): Map<String, ObdexDtcEntry> {
        if (!cacheFile.isFile || cacheFile.length() !in 1..MAX_DOWNLOAD_BYTES.toLong()) return emptyMap()
        return runCatching { parseEntries(cacheFile.readText(Charsets.UTF_8)).associateBy { it.code } }
            .getOrDefault(emptyMap())
    }

    private fun readBounded(input: java.io.InputStream, maximumBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            require(total <= maximumBytes) { "OBDex download exceeds the 25 MiB safety limit" }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun parseEntries(json: String): List<ObdexDtcEntry> {
        val array = JSONArray(json)
        val entries = ArrayList<ObdexDtcEntry>(array.length())
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val code = item.optString("code").uppercase()
            if (!Regex("[PBCU][0-9A-F]{4}").matches(code)) continue
            val title = item.optJSONObject("title")?.optString("en").orEmpty()
            if (title.isBlank()) continue
            val description = item.optJSONObject("description")?.optString("en").orEmpty()
            val causes = item.optJSONArray("common_causes").toCauses()
            val symptoms = item.optJSONArray("symptoms").toEnglishLabels()
            val components = item.optJSONArray("affected_components").toStringList()
            val sources = item.optJSONArray("sources").toStringList()
            val relatedCodes = item.optJSONArray("related_codes").toStringList().filter { Regex("[PBCU][0-3][0-9A-F]{3}").matches(it) }
            entries += ObdexDtcEntry(
                code = code,
                category = item.optString("category").takeIf { it.isNotBlank() },
                title = title,
                description = description,
                affectedComponents = components,
                commonCauses = causes,
                symptoms = symptoms,
                relatedCodes = relatedCodes,
                sources = sources
            )
        }
        return entries
    }

    private fun JSONArray?.toCauses(): List<ObdexDtcCause> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index ->
            val value = optJSONObject(index) ?: return@mapNotNull null
            val label = value.optJSONObject("label")?.optString("en").orEmpty()
            if (label.isBlank()) return@mapNotNull null
            val likelihood = value.optString("likelihood").takeIf { it == "low" || it == "medium" || it == "high" }
            ObdexDtcCause(label, likelihood)
        }
    }

    private fun JSONArray?.toEnglishLabels(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index ->
            val value = opt(index)
            when (value) {
                is JSONObject -> {
                    val label = value.optJSONObject("label")?.optString("en")
                        ?: value.optString("en")
                    label.takeIf { it.isNotBlank() }
                }
                is String -> value.takeIf { it.isNotBlank() }
                else -> null
            }
        }
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index ->
            optString(index).takeIf { it.isNotBlank() }
        }
    }

    companion object {
        const val DATA_URL = "https://foerbsnavi.github.io/OBDex/generic.min.json"
        private const val MAX_DOWNLOAD_BYTES = 25 * 1024 * 1024
        private const val MINIMUM_VALID_ENTRIES = 1_000
    }
}

data class ObdexDtcCause(val label: String, val likelihood: String?)

data class ObdexDtcEntry(
    val code: String,
    val category: String?,
    val title: String,
    val description: String,
    val affectedComponents: List<String>,
    val commonCauses: List<ObdexDtcCause>,
    val symptoms: List<String>,
    val relatedCodes: List<String>,
    val sources: List<String>
) {
    fun toDiagnosticResult(): VehicleDiagnosticResult = VehicleDiagnosticResult(
        code = code,
        title = title,
        meaning = description.ifBlank { title },
        possibleCauses = commonCauses.take(8).map { it.label },
        causeLikelihoodNotes = commonCauses.take(8).mapNotNull { cause ->
            cause.likelihood?.let { "${cause.label}: dataset likelihood tag '$it' (not a probability or vehicle-specific diagnosis)." }
        },
        symptoms = symptoms.take(12),
        affectedComponents = affectedComponents.take(20),
        relatedCodes = relatedCodes.take(20),
        safeChecks = listOf(
            "This is a generic OBD-II reference, not a confirmed diagnosis for this vehicle.",
            "Record all related codes and freeze-frame data before clearing anything.",
            "Confirm the exact model, engine, year and reporting control module in its official service information.",
            "Do not replace parts or attempt a repair from a code description alone."
        ),
        severity = "Reference only — severity needs vehicle context",
        manufacturerSpecific = false,
        limitation = "OBDex generic DTC data (category: " + (category ?: "not supplied") + "). The project declares CC0-1.0 for data and MIT for tooling; its maintainer says imported DTC identifiers/titles derive from Wal33D/dtc-database under MIT, while other prose is independently authored. Retain the MIT notice and inspect record-level sources. Exact vehicle/year/ECU compatibility is not verified. No repair procedure or cost estimate is provided.",
        sourceReferences = sources.filter { it.startsWith("https://") }.distinct().take(5)
    )
}
