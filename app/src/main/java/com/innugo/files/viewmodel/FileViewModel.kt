package com.innugo.files.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.innugo.files.data.model.FileItem
import com.innugo.files.data.preferences.PreferencesManager
import com.innugo.files.data.repository.FileRepository
import com.innugo.files.util.FileUtils
import com.innugo.files.util.ZipUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOrder { NAME, SIZE, DATE, TYPE }

enum class ViewMode { LIST, GRID }

data class FileBrowserUiState(
    val files: List<FileItem> = emptyList(),
    val sortOrder: SortOrder = SortOrder.NAME,
    val viewMode: ViewMode = ViewMode.LIST,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    /** -1 = not extracting; 0–100 = progress percentage */
    val extractionProgress: Int = -1,
    val showDeleteZipDialog: Boolean = false,
    val zipPathToDelete: String = "",
    val showInstallApkDialog: Boolean = false,
    val apkPathToInstall: String = "",
    val errorMessage: String? = null,
    val showRenameDialog: Boolean = false,
    val fileToRename: FileItem? = null,
    val showCreateFolderDialog: Boolean = false
)

class FileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository()
    private val prefsManager = PreferencesManager.getInstance(application)

    private val _uiState = MutableStateFlow(FileBrowserUiState())
    val uiState: StateFlow<FileBrowserUiState> = _uiState.asStateFlow()

    private var currentPath: String = ""
    private var allFiles: List<FileItem> = emptyList()

    init {
        viewModelScope.launch {
            val savedMode = prefsManager.viewMode.first()
            _uiState.update { it.copy(viewMode = if (savedMode == "grid") ViewMode.GRID else ViewMode.LIST) }
        }
    }

    fun loadDirectory(path: String) {
        currentPath = path
        viewModelScope.launch(Dispatchers.IO) {
            val files = repository.getFilesInDirectory(path)
            allFiles = files
            val sorted = applySortOrder(files, _uiState.value.sortOrder)
            _uiState.update { it.copy(files = sorted, isLoading = false) }
        }
    }

    fun setSortOrder(order: SortOrder) {
        val sorted = applySortOrder(allFiles, order)
        _uiState.update { it.copy(sortOrder = order, files = sorted) }
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            val newMode = if (_uiState.value.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
            prefsManager.setViewMode(if (newMode == ViewMode.GRID) "grid" else "list")
            _uiState.update { it.copy(viewMode = newMode) }
        }
    }

    fun setSearchQuery(query: String) {
        val base = if (query.isEmpty()) applySortOrder(allFiles, _uiState.value.sortOrder)
        else allFiles.filter { it.name.contains(query, ignoreCase = true) }
        _uiState.update { it.copy(searchQuery = query, files = base) }
    }

    fun toggleSearch() {
        _uiState.update { state ->
            state.copy(
                isSearchActive = !state.isSearchActive,
                searchQuery = "",
                files = applySortOrder(allFiles, state.sortOrder)
            )
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.deleteFile(path)) loadDirectory(currentPath)
            else _uiState.update { it.copy(errorMessage = "Failed to delete file") }
        }
    }

    fun renameFile(oldPath: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = repository.renameFile(oldPath, newName)
            _uiState.update { it.copy(showRenameDialog = false, fileToRename = null) }
            if (ok) loadDirectory(currentPath)
            else _uiState.update { it.copy(errorMessage = "Failed to rename") }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = repository.createFolder(currentPath, folderName)
            _uiState.update { it.copy(showCreateFolderDialog = false) }
            if (ok) loadDirectory(currentPath)
            else _uiState.update { it.copy(errorMessage = "Failed to create folder") }
        }
    }

    fun extractZip(zipPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(extractionProgress = 0) }
            val result = ZipUtils.extractZip(zipPath) { progress ->
                _uiState.update { it.copy(extractionProgress = progress) }
            }
            _uiState.update { it.copy(extractionProgress = -1) }
            if (result.success) {
                loadDirectory(currentPath)
                _uiState.update {
                    it.copy(
                        showDeleteZipDialog = true,
                        zipPathToDelete = zipPath,
                        showInstallApkDialog = result.foundApk != null,
                        apkPathToInstall = result.foundApk ?: ""
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Extraction failed: ${result.error}") }
            }
        }
    }

    fun installApk(context: Context, apkPath: String) {
        viewModelScope.launch {
            prefsManager.setPendingCleanup(apkPath)
            FileUtils.installApk(context, apkPath)
            _uiState.update { it.copy(showInstallApkDialog = false, apkPathToInstall = "") }
        }
    }

    fun confirmDeleteZip() {
        val path = _uiState.value.zipPathToDelete
        _uiState.update { it.copy(showDeleteZipDialog = false, zipPathToDelete = "") }
        if (path.isNotEmpty()) deleteFile(path)
    }

    fun dismissDeleteZipDialog() {
        _uiState.update { it.copy(showDeleteZipDialog = false, zipPathToDelete = "") }
    }

    fun dismissInstallApkDialog() {
        _uiState.update { it.copy(showInstallApkDialog = false, apkPathToInstall = "") }
    }

    fun showRenameDialog(file: FileItem) {
        _uiState.update { it.copy(showRenameDialog = true, fileToRename = file) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = false, fileToRename = null) }
    }

    fun showCreateFolderDialog() {
        _uiState.update { it.copy(showCreateFolderDialog = true) }
    }

    fun dismissCreateFolderDialog() {
        _uiState.update { it.copy(showCreateFolderDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun applySortOrder(files: List<FileItem>, order: SortOrder): List<FileItem> =
        when (order) {
            SortOrder.NAME -> files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            SortOrder.SIZE -> files.sortedWith(compareBy({ !it.isDirectory }, { it.size }))
            SortOrder.DATE -> files.sortedWith(compareBy({ !it.isDirectory }, { -it.lastModified }))
            SortOrder.TYPE -> files.sortedWith(compareBy({ !it.isDirectory }, { it.mimeType }, { it.name.lowercase() }))
        }
}
