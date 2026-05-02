package com.innugo.files.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object ZipUtils {

    data class ExtractionResult(
        val success: Boolean,
        val extractedPath: String,
        val foundApk: String? = null,
        val error: String? = null
    )

    suspend fun extractZip(
        zipFilePath: String,
        onProgress: (Int) -> Unit = {}
    ): ExtractionResult = withContext(Dispatchers.IO) {
        try {
            val zipFile = File(zipFilePath)
            val destFolder = File(zipFile.parent ?: return@withContext failed("No parent dir"), zipFile.nameWithoutExtension)
            destFolder.mkdirs()

            // Count entries for progress tracking
            var totalEntries = 0
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                while (zis.nextEntry != null) totalEntries++
            }

            var processed = 0
            var foundApk: String? = null
            val destCanonical = destFolder.canonicalPath

            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val destFile = File(destFolder, entry.name)

                    // Zip-slip prevention: reject any entry whose resolved path
                    // is not strictly inside the destination folder.
                    if (!destFile.canonicalPath.startsWith(destCanonical + File.separator)) {
                        entry = zis.nextEntry
                        continue
                    }

                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        FileOutputStream(destFile).use { fos -> zis.copyTo(fos) }
                        if (destFile.name.lowercase().endsWith(".apk") && foundApk == null) {
                            foundApk = destFile.absolutePath
                        }
                    }

                    processed++
                    if (totalEntries > 0) onProgress((processed * 100) / totalEntries)
                    entry = zis.nextEntry
                }
            }

            ExtractionResult(success = true, extractedPath = destFolder.absolutePath, foundApk = foundApk)
        } catch (e: Exception) {
            failed(e.message ?: "Unknown error")
        }
    }

    private fun failed(msg: String) = ExtractionResult(success = false, extractedPath = "", error = msg)
}
