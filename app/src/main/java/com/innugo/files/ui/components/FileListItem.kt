package com.innugo.files.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.innugo.files.data.model.FileItem
import com.innugo.files.util.FileUtils

@Composable
fun FileListItem(
    file: FileItem,
    onClick: () -> Unit,
    onRename: (FileItem) -> Unit,
    onDelete: (String) -> Unit,
    onShare: (String) -> Unit,
    onExtract: (String) -> Unit,
    onInstall: (String) -> Unit,
    onCopyPath: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = fileIcon(file),
            contentDescription = file.name,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!file.isDirectory) {
                    Text(
                        text = FileUtils.formatFileSize(file.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
                Text(
                    text = FileUtils.formatDate(file.lastModified),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Open") },
                    onClick = { onClick(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.OpenInNew, null) }
                )
                if (!file.isDirectory) {
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = { onShare(file.path); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Share, null) }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = { onRename(file); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { onDelete(file.path); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Delete, null) }
                )
                if (FileUtils.isZipFile(file.name)) {
                    DropdownMenuItem(
                        text = { Text("Extract") },
                        onClick = { onExtract(file.path); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Archive, null) }
                    )
                }
                if (FileUtils.isApkFile(file.name)) {
                    DropdownMenuItem(
                        text = { Text("Install") },
                        onClick = { onInstall(file.path); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.SystemUpdateAlt, null) }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Copy path") },
                    onClick = { onCopyPath(file.path); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                )
            }
        }
    }
}

/** Shared helper — visible to other files in this package (FileGridItem, HomeScreen). */
fun fileIcon(file: FileItem): ImageVector = when {
    file.isDirectory -> Icons.Filled.Folder
    file.mimeType.startsWith("image/") -> Icons.Filled.Image
    file.mimeType.startsWith("audio/") -> Icons.Filled.AudioFile
    file.mimeType.startsWith("video/") -> Icons.Filled.VideoFile
    file.mimeType == "application/pdf" -> Icons.Filled.PictureAsPdf
    file.mimeType.startsWith("text/") -> Icons.Filled.TextSnippet
    file.mimeType == "application/vnd.android.package-archive" -> Icons.Filled.Android
    FileUtils.isZipFile(file.name) -> Icons.Filled.Archive
    else -> Icons.Filled.InsertDriveFile
}
