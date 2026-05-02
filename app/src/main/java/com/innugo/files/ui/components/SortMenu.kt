package com.innugo.files.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
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
import com.innugo.files.viewmodel.SortOrder

@Composable
fun SortMenu(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.Sort, contentDescription = "Sort options")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        SortOrder.entries.forEach { order ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = order.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                        color = if (currentSort == order) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = { onSortSelected(order); expanded = false }
            )
        }
    }
}
