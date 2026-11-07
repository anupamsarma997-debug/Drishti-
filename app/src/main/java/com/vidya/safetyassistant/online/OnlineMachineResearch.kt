package com.vidya.safetyassistant.online

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class OnlineMachineResult(
    val title: String,
    val summary: String,
    val url: String
)

/**
 * Retrieves short public encyclopedia introductions for discovery only.
 * It is not a repair database and must never be treated as model-specific service data.
 */
object OnlineMachineResearch {
    fun search(query: String, maxResults: Int = 5): List<OnlineMachineResult> {
        require(query.isNotBlank()) { "Enter a machine name or model." }
        require(maxResults in 1..10) { "Result limit must be between 1 and 10." }

        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val endpoint = URL(
            "https://en.wikipedia.org/w/api.php?action=query&generator=search" +
                "&gsrsearch=$encoded&gsrlimit=$maxResults&prop=extracts" +
                "&exintro=1&explaintext=1&format=json"
        )
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 10000
            // Wikimedia asks API clients to identify themselves with a meaningful User-Agent.
            setRequestProperty(
                "User-Agent",
                "VidyaAndroid/0.1 (https://github.com/anupamsarma997-debug/Drishti-)"
            )
            setRequestProperty("Accept", "application/json")
        }

        try {
            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_TOO_MANY_REQUESTS) {
                val retryAfter = connection.getHeaderField("Retry-After")
                throw IllegalStateException(
                    if (retryAfter.isNullOrBlank()) {
                        "Online source is rate-limiting requests. Please wait before trying again."
                    } else {
                        "Online source asked us to wait $retryAfter seconds. Please try again later."
                    }
                )
            }
            if (status !in 200..299) {
                throw IllegalStateException("Online source returned HTTP $status.")
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val pages = root.optJSONObject("query")?.optJSONObject("pages")
                ?: return emptyList()
            val results = mutableListOf<OnlineMachineResult>()
            val keys = pages.keys()
            while (keys.hasNext()) {
                val page = pages.getJSONObject(keys.next())
                val title = page.optString("title").trim()
                val extract = page.optString("extract").trim()
                val pageId = page.optLong("pageid", -1L)
                if (title.isNotBlank() && extract.isNotBlank() && pageId > 0) {
                    results += OnlineMachineResult(
                        title = title,
                        summary = extract.take(1800),
                        url = "https://en.wikipedia.org/?curid=$pageId"
                    )
                }
            }
            return results
        } finally {
            connection.disconnect()
        }
    }
}
