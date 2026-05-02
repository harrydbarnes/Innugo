package com.innugo.files.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.innugo.files.util.FileUtils

@Composable
fun StorageCard(
    usedStorage: Long,
    totalStorage: Long,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (totalStorage > 0L) {
        (usedStorage.toFloat() / totalStorage.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Storage",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Storage",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${FileUtils.formatFileSize(usedStorage)} used",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${FileUtils.formatFileSize(totalStorage)} total",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
