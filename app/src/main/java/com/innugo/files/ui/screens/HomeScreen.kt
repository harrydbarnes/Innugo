package com.innugo.files.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.innugo.files.data.model.FileItem
import com.innugo.files.ui.components.QuickAccessTile
import com.innugo.files.ui.components.StorageCard
import com.innugo.files.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onFolderClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPermissionRationale by remember { mutableStateOf(false) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) viewModel.refresh()
        else showPermissionRationale = true
    }

    LaunchedEffect(Unit) { permissionLauncher.launch(permissions) }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Permission Required") },
            text = {
                Text(
                    "Innugo needs storage access to show your files. " +
                        "Please grant the permission in Settings."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPermissionRationale = false }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Innugo") },
                actions = {
                    IconButton(onClick = { /* Settings screen – future */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Quick Access ─────────────────────────────────────────────
            Text("Quick Access", style = MaterialTheme.typography.titleMedium)

            val folders = uiState.quickAccessFolders
            if (folders.size >= 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessTile(
                        folder = folders[0],
                        modifier = Modifier.weight(1f),
                        onClick = { onFolderClick(folders[0].path) },
                        onPinClick = { viewModel.togglePin(folders[0].path) }
                    )
                    QuickAccessTile(
                        folder = folders[1],
                        modifier = Modifier.weight(1f),
                        onClick = { onFolderClick(folders[1].path) },
                        onPinClick = { viewModel.togglePin(folders[1].path) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessTile(
                        folder = folders[2],
                        modifier = Modifier.weight(1f),
                        onClick = { onFolderClick(folders[2].path) },
                        onPinClick = { viewModel.togglePin(folders[2].path) }
                    )
                    QuickAccessTile(
                        folder = folders[3],
                        modifier = Modifier.weight(1f),
                        onClick = { onFolderClick(folders[3].path) },
                        onPinClick = { viewModel.togglePin(folders[3].path) }
                    )
                }
            }

            // ── Recent Files ─────────────────────────────────────────────
            if (uiState.recentFiles.isNotEmpty()) {
                Text("Recent Files", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.recentFiles, key = { it.path }) { file ->
                        RecentFileCard(
                            file = file,
                            onClick = {
                                val parent = java.io.File(file.path).parent ?: return@RecentFileCard
                                onFolderClick(parent)
                            }
                        )
                    }
                }
            }

            // ── Storage Card ──────────────────────────────────────────────
            StorageCard(
                usedStorage = uiState.usedStorage,
                totalStorage = uiState.totalStorage
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RecentFileCard(
    file: FileItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = recentFileIcon(file.mimeType),
                contentDescription = file.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = file.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun recentFileIcon(mimeType: String): ImageVector = when {
    mimeType.startsWith("image/") -> Icons.Filled.Image
    mimeType.startsWith("audio/") -> Icons.Filled.AudioFile
    mimeType.startsWith("video/") -> Icons.Filled.VideoFile
    mimeType == "application/pdf" -> Icons.Filled.PictureAsPdf
    mimeType.startsWith("text/") -> Icons.Filled.TextSnippet
    else -> Icons.Filled.InsertDriveFile
}
