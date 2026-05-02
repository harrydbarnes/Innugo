package com.innugo.files.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.innugo.files.data.model.FileItem
import com.innugo.files.data.model.QuickAccessFolder
import com.innugo.files.data.preferences.PreferencesManager
import com.innugo.files.data.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val quickAccessFolders: List<QuickAccessFolder> = emptyList(),
    val recentFiles: List<FileItem> = emptyList(),
    val usedStorage: Long = 0L,
    val totalStorage: Long = 1L,
    val isLoading: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository()
    private val prefsManager = PreferencesManager.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            prefsManager.pinnedFolders.collect { pinnedPaths ->
                val folders = withContext(Dispatchers.IO) { buildQuickAccessFolders(pinnedPaths) }
                val recent = withContext(Dispatchers.IO) { repository.getRecentFiles(10) }
                val (used, total) = withContext(Dispatchers.IO) { repository.getStorageInfo() }
                _uiState.update {
                    it.copy(
                        quickAccessFolders = folders,
                        recentFiles = recent,
                        usedStorage = used,
                        totalStorage = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun buildQuickAccessFolders(pinned: Set<String>): List<QuickAccessFolder> {
        val defs = listOf(
            "Downloads" to Environment.DIRECTORY_DOWNLOADS,
            "Documents" to Environment.DIRECTORY_DOCUMENTS,
            "Pictures" to Environment.DIRECTORY_PICTURES,
            "Music" to Environment.DIRECTORY_MUSIC
        )
        return defs.map { (name, type) ->
            val dir = Environment.getExternalStoragePublicDirectory(type)
            QuickAccessFolder(
                name = name,
                path = dir.absolutePath,
                itemCount = repository.getItemCount(dir.absolutePath),
                isPinned = dir.absolutePath in pinned
            )
        }
    }

    fun togglePin(path: String) {
        viewModelScope.launch { prefsManager.togglePinnedFolder(path) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val pinned = prefsManager.pinnedFolders.first()
            val folders = withContext(Dispatchers.IO) { buildQuickAccessFolders(pinned) }
            val recent = withContext(Dispatchers.IO) { repository.getRecentFiles(10) }
            val (used, total) = withContext(Dispatchers.IO) { repository.getStorageInfo() }
            _uiState.update {
                it.copy(
                    quickAccessFolders = folders,
                    recentFiles = recent,
                    usedStorage = used,
                    totalStorage = total,
                    isLoading = false
                )
            }
        }
    }
}
