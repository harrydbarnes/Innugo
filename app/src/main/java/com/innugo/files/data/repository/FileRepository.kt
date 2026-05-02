package com.innugo.files.data.repository

import android.os.Environment
import android.webkit.MimeTypeMap
import com.innugo.files.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository {

    fun getFilesInDirectory(path: String): List<FileItem> {
        return try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) return emptyList()
            dir.listFiles()
                ?.map { file -> file.toFileItem() }
                ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getRecentFiles(limit: Int = 10): List<FileItem> {
        return try {
            val dirs = listOf(
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_DOCUMENTS,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_DCIM
            ).mapNotNull { type ->
                try { Environment.getExternalStoragePublicDirectory(type) } catch (_: Exception) { null }
            }

            val files = mutableListOf<FileItem>()
            for (dir in dirs) {
                try {
                    dir.listFiles()?.filter { it.isFile }?.forEach { files.add(it.toFileItem()) }
                } catch (_: SecurityException) { /* skip */ }
            }
            files.sortedByDescending { it.lastModified }.take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getStorageInfo(): Pair<Long, Long> {
        return try {
            val root = Environment.getExternalStorageDirectory()
            val total = root.totalSpace
            val used = total - root.freeSpace
            Pair(used, total)
        } catch (e: Exception) {
            Pair(0L, 1L)
        }
    }

    fun getItemCount(path: String): Int {
        return try {
            File(path).listFiles()?.size ?: 0
        } catch (_: SecurityException) { 0 }
    }

    suspend fun renameFile(oldPath: String, newName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val old = File(oldPath)
                old.renameTo(File(old.parent ?: return@withContext false, newName))
            } catch (e: Exception) { false }
        }

    suspend fun deleteFile(path: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val f = File(path)
                if (f.isDirectory) f.deleteRecursively() else f.delete()
            } catch (e: Exception) { false }
        }

    suspend fun createFolder(parentPath: String, folderName: String): Boolean =
        withContext(Dispatchers.IO) {
            try { File(parentPath, folderName).mkdirs() }
            catch (e: Exception) { false }
        }

    private fun File.toFileItem() = FileItem(
        name = name,
        path = absolutePath,
        isDirectory = isDirectory,
        size = if (isFile) length() else 0L,
        lastModified = lastModified(),
        mimeType = if (isDirectory) "inode/directory" else getMimeType(name)
    )

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
            ?: "application/octet-stream"
    }
}
