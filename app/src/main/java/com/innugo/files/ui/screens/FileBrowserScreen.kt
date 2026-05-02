package com.innugo.files.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.innugo.files.ui.components.FileGridItem
import com.innugo.files.ui.components.FileListItem
import com.innugo.files.ui.components.SortMenu
import com.innugo.files.util.FileUtils
import com.innugo.files.viewmodel.FileViewModel
import com.innugo.files.viewmodel.ViewMode
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    path: String,
    onNavigateUp: () -> Unit,
    onFolderClick: (String) -> Unit,
    viewModel: FileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val folderName = remember(path) { File(path).name.ifEmpty { "Files" } }

    LaunchedEffect(path) { viewModel.loadDirectory(path) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // ── Rename Dialog ────────────────────────────────────────────────────
    if (uiState.showRenameDialog && uiState.fileToRename != null) {
        var newName by rememberSaveable(uiState.fileToRename) {
            mutableStateOf(uiState.fileToRename!!.name)
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissRenameDialog() },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { if (newName.isNotBlank()) viewModel.renameFile(uiState.fileToRename!!.path, newName) }
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRenameDialog() }) { Text("Cancel") }
            }
        )
    }

    // ── Create Folder Dialog ─────────────────────────────────────────────
    if (uiState.showCreateFolderDialog) {
        var folderNameInput by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.dismissCreateFolderDialog() },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    label = { Text("Folder name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { if (folderNameInput.isNotBlank()) viewModel.createFolder(folderNameInput) }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreateFolderDialog() }) { Text("Cancel") }
            }
        )
    }

    // ── Extraction Progress Dialog ────────────────────────────────────────
    if (uiState.extractionProgress >= 0) {
        AlertDialog(
            onDismissRequest = { /* block dismissal during extraction */ },
            title = { Text("Extracting…") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { uiState.extractionProgress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${uiState.extractionProgress}%")
                }
            },
            confirmButton = { }
        )
    }

    // ── Delete ZIP Dialog ─────────────────────────────────────────────────
    if (uiState.showDeleteZipDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteZipDialog() },
            title = { Text("Delete Original ZIP?") },
            text = { Text("Extraction successful. Delete the original ZIP file?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteZip() }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteZipDialog() }) { Text("No") }
            }
        )
    }

    // ── Install APK Dialog ────────────────────────────────────────────────
    if (uiState.showInstallApkDialog && uiState.apkPathToInstall.isNotEmpty()) {
        val apkName = File(uiState.apkPathToInstall).name
        AlertDialog(
            onDismissRequest = { viewModel.dismissInstallApkDialog() },
            title = { Text("Install APK?") },
            text = { Text("An APK was found inside the archive: $apkName\nWould you like to install it?") },
            confirmButton = {
                TextButton(onClick = { viewModel.installApk(context, uiState.apkPathToInstall) }) {
                    Text("Install")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissInstallApkDialog() }) { Text("Skip") }
            }
        )
    }

    // ── Main Scaffold ─────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSearchActive) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search files…") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            )
                        )
                    } else {
                        Text(
                            text = folderName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(
                            imageVector = if (uiState.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (uiState.isSearchActive) "Close search" else "Search"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (uiState.viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "Toggle view mode"
                        )
                    }
                    SortMenu(currentSort = uiState.sortOrder, onSortSelected = { viewModel.setSortOrder(it) })
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateFolderDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.CreateNewFolder,
                    contentDescription = "New folder",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.files.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = "This folder is empty",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }

                uiState.viewMode == ViewMode.LIST -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.files, key = { it.path }) { file ->
                            FileListItem(
                                file = file,
                                onClick = {
                                    if (file.isDirectory) onFolderClick(file.path)
                                    else FileUtils.openFile(context, file.path)
                                },
                                onRename = { viewModel.showRenameDialog(it) },
                                onDelete = { viewModel.deleteFile(it) },
                                onShare = { FileUtils.shareFile(context, it) },
                                onExtract = { viewModel.extractZip(it) },
                                onInstall = { viewModel.installApk(context, it) },
                                onCopyPath = { FileUtils.copyToClipboard(context, it) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.files, key = { it.path }) { file ->
                            FileGridItem(
                                file = file,
                                onClick = {
                                    if (file.isDirectory) onFolderClick(file.path)
                                    else FileUtils.openFile(context, file.path)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
