package com.vidya.safetyassistant.manuals

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ManualPageText(val manualName: String, val pageNumber: Int, val text: String)

/**
 * Local OCR index for user-imported manuals. Runs only on-device and stores extracted
 * page text in private app preferences. Scanned PDFs are rasterized page-by-page.
 */
class ManualTextIndexer(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("vidya_manual_ocr", Context.MODE_PRIVATE)
    private val latin = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val devanagari = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())

    fun index(manual: LocalManual, maxPages: Int = 40): Int {
        val descriptor = app.contentResolver.openFileDescriptor(Uri.parse(manual.uri), "r")
            ?: error("The selected manual is no longer accessible.")
        val pages = mutableListOf<ManualPageText>()
        descriptor.use { fd ->
            PdfRenderer(fd).use { renderer ->
                val count = minOf(renderer.pageCount, maxPages)
                for (i in 0 until count) {
                    renderer.openPage(i).use { page ->
                        val scale = minOf(2.0, 1800.0 / maxOf(page.width, page.height))
                        val width = (page.width * scale).toInt().coerceAtLeast(1)
                        val height = (page.height * scale).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        try {
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            val image = InputImage.fromBitmap(bitmap, 0)
                            val latinText = recognize(latin, image)
                            val hindiText = recognize(devanagari, image)
                            val combined = listOf(latinText, hindiText).distinct()
                                .filter { it.isNotBlank() }.joinToString("\n")
                            if (combined.isNotBlank()) {
                                pages += ManualPageText(manual.displayName, i + 1, combined)
                            }
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            }
        }
        val json = JSONArray()
        pages.forEach { json.put(JSONObject().put("page", it.pageNumber).put("text", it.text)) }
        check(prefs.edit().putString(key(manual.uri), json.toString()).commit()) {
            "Could not save the local manual search index."
        }
        return pages.size
    }

    fun search(query: String, limit: Int = 8): List<ManualPageText> {
        val terms = query.lowercase().split(Regex("\\s+")).filter { it.length >= 2 }.distinct()
        if (terms.isEmpty()) return emptyList()
        return buildList {
            for (manual in ManualLibrary(app).list()) {
                val raw = prefs.getString(key(manual.uri), null) ?: continue
                val array = try { JSONArray(raw) } catch (_: Exception) { continue }
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val body = item.optString("text")
                    val normalized = body.lowercase()
                    val hits = terms.count { normalized.contains(it) }
                    if (hits > 0) add(ManualPageText(manual.displayName, item.optInt("page"), body))
                }
            }
        }.sortedByDescending { page ->
            terms.count { page.text.lowercase().contains(it) }
        }.take(limit)
    }

    fun close() {
        latin.close()
        devanagari.close()
    }

    private fun recognize(recognizer: com.google.mlkit.vision.text.TextRecognizer, image: InputImage): String =
        Tasks.await(recognizer.process(image), 25, TimeUnit.SECONDS).text

    private fun key(uri: String) = "manual_" + uri.hashCode().toUInt().toString(16)
}
