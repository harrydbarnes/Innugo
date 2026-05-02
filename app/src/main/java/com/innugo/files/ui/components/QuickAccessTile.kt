package com.innugo.files.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.innugo.files.data.model.QuickAccessFolder

@Composable
fun QuickAccessTile(
    folder: QuickAccessFolder,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = folderIcon(folder.name),
                    contentDescription = folder.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${folder.itemCount} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                    )
                }
            }

            IconButton(
                onClick = onPinClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = if (folder.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (folder.isPinned) "Unpin" else "Pin",
                    tint = if (folder.isPinned) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun folderIcon(name: String): ImageVector = when (name.lowercase()) {
    "downloads" -> Icons.Filled.FileDownload
    "documents" -> Icons.Filled.Description
    "pictures" -> Icons.Filled.Image
    "music" -> Icons.Filled.MusicNote
    else -> Icons.Filled.Folder
}
