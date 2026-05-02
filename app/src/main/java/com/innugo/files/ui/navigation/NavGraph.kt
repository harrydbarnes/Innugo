package com.innugo.files.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.innugo.files.ui.screens.FileBrowserScreen
import com.innugo.files.ui.screens.HomeScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onFolderClick = { path ->
                    val encoded = URLEncoder.encode(path, StandardCharsets.UTF_8.toString())
                    navController.navigate("browser/$encoded")
                }
            )
        }

        composable(
            route = "browser/{encodedPath}",
            arguments = listOf(navArgument("encodedPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("encodedPath") ?: ""
            val path = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
            FileBrowserScreen(
                path = path,
                onNavigateUp = { navController.popBackStack() },
                onFolderClick = { folderPath ->
                    val encodedFolder = URLEncoder.encode(folderPath, StandardCharsets.UTF_8.toString())
                    navController.navigate("browser/$encodedFolder")
                }
            )
        }
    }
}
