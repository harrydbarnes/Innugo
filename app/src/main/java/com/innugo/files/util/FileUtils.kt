package com.innugo.files.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1_024L -> "$bytes B"
        bytes < 1_048_576L -> "%.1f KB".format(bytes / 1_024.0)
        bytes < 1_073_741_824L -> "%.1f MB".format(bytes / 1_048_576.0)
        else -> "%.2f GB".format(bytes / 1_073_741_824.0)
    }

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

    fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
            ?: "application/octet-stream"
    }

    fun isZipFile(fileName: String) = fileName.lowercase().endsWith(".zip")

    fun isApkFile(fileName: String) = fileName.lowercase().endsWith(".apk")

    fun openFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(file.name))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    fun shareFile(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = getMimeType(file.name)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
        } catch (_: Exception) { }
    }

    fun installApk(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    fun copyToClipboard(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("File Path", text))
    }
}
