package com.innugo.files

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.innugo.files.data.preferences.PreferencesManager
import com.innugo.files.ui.navigation.NavGraph
import com.innugo.files.ui.theme.InnugoTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsManager = PreferencesManager.getInstance(this)

        setContent {
            InnugoTheme {
                val navController = rememberNavController()
                var showCleanupDialog by remember { mutableStateOf(false) }
                var pendingCleanupPath by remember { mutableStateOf("") }

                // Observe pending APK cleanup on each launch
                LaunchedEffect(Unit) {
                    prefsManager.pendingCleanup.collect { path ->
                        if (!path.isNullOrEmpty() && !showCleanupDialog) {
                            pendingCleanupPath = path
                            showCleanupDialog = true
                        }
                    }
                }

                if (showCleanupDialog && pendingCleanupPath.isNotEmpty()) {
                    val apkName = File(pendingCleanupPath).name
                    AlertDialog(
                        onDismissRequest = { showCleanupDialog = false },
                        title = { Text("Cleanup APK") },
                        text = { Text("You installed $apkName. Delete the APK file to free up space?") },
                        confirmButton = {
                            TextButton(onClick = {
                                val pathCopy = pendingCleanupPath
                                showCleanupDialog = false
                                pendingCleanupPath = ""
                                lifecycleScope.launch {
                                    runCatching { File(pathCopy).delete() }
                                    prefsManager.setPendingCleanup(null)
                                }
                            }) { Text("Yes, Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showCleanupDialog = false
                                pendingCleanupPath = ""
                                lifecycleScope.launch { prefsManager.setPendingCleanup(null) }
                            }) { Text("No, Keep") }
                        }
                    )
                }

                NavGraph(navController = navController)
            }
        }
    }
}
