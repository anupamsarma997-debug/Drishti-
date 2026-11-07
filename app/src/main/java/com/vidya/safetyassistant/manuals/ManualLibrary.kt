package com.vidya.safetyassistant.manuals

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.os.ParcelFileDescriptor
import org.json.JSONArray
import org.json.JSONObject

data class LocalManual(
    val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val importedAtMillis: Long
)

/**
 * Local-only PDF library. Original files stay with Android's document provider;
 * this app stores a persistable URI permission and small metadata only.
 */
class ManualLibrary(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("vidya_manual_library", Context.MODE_PRIVATE)

    fun list(): List<LocalManual> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        LocalManual(
                            uri = item.getString("uri"),
                            displayName = item.getString("name"),
                            sizeBytes = item.optLong("size", 0L),
                            pageCount = item.optInt("pages", 0),
                            importedAtMillis = item.optLong("added", 0L)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun importPdf(uri: Uri): LocalManual {
        val descriptor = app.contentResolver.openFileDescriptor(uri, "r")
            ?: error("The selected PDF could not be opened.")
        val pageCount = descriptor.use { fd ->
            PdfRenderer(fd).use { renderer -> renderer.pageCount }
        }
        require(pageCount > 0) { "The selected PDF has no readable pages." }

        val nameAndSize = queryDisplayName(uri)
        val item = LocalManual(
            uri = uri.toString(),
            displayName = nameAndSize.first,
            sizeBytes = nameAndSize.second,
            pageCount = pageCount,
            importedAtMillis = System.currentTimeMillis()
        )
        val updated = (list().filterNot { it.uri == item.uri } + item)
            .sortedByDescending { it.importedAtMillis }
        val array = JSONArray()
        updated.forEach {
            array.put(
                JSONObject()
                    .put("uri", it.uri)
                    .put("name", it.displayName)
                    .put("size", it.sizeBytes)
                    .put("pages", it.pageCount)
                    .put("added", it.importedAtMillis)
            )
        }
        check(prefs.edit().putString("items", array.toString()).commit()) {
            "Could not save the manual library."
        }
        return item
    }

    fun remove(uri: String) {
        val updated = list().filterNot { it.uri == uri }
        val array = JSONArray()
        updated.forEach {
            array.put(
                JSONObject().put("uri", it.uri).put("name", it.displayName)
                    .put("size", it.sizeBytes).put("pages", it.pageCount)
                    .put("added", it.importedAtMillis)
            )
        }
        prefs.edit().putString("items", array.toString()).apply()
        try {
            app.contentResolver.releasePersistableUriPermission(
                Uri.parse(uri),
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some document providers do not expose a revocable persistable grant.
        }
    }

    private fun queryDisplayName(uri: Uri): Pair<String, Long> {
        var name = uri.lastPathSegment?.substringAfterLast('/') ?: "Technical manual.pdf"
        var size = 0L
        app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
        return name to size
    }
}
