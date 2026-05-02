package com.innugo.files.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.innugo.files.util.ThemeUtils

private val LightColorScheme = lightColorScheme(
    primary = IndigoBlue,
    onPrimary = IndigoOnPrimary,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = IndigoOnBackground,
    background = IndigoBackground,
    onBackground = IndigoOnBackground,
    surface = IndigoBackground,
    onSurface = IndigoOnBackground,
    surfaceVariant = IndigoContainer,
    onSurfaceVariant = IndigoOnBackground
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkNavyPrimary,
    onPrimary = DarkNavyOnPrimary,
    primaryContainer = DarkNavyContainer,
    onPrimaryContainer = DarkNavyOnBackground,
    background = DarkNavyBackground,
    onBackground = DarkNavyOnBackground,
    surface = DarkNavySurface,
    onSurface = DarkNavyOnBackground,
    surfaceVariant = DarkNavyContainer,
    onSurfaceVariant = DarkNavyOnBackground
)

@Composable
fun InnugoTheme(
    darkTheme: Boolean = isSystemInDarkTheme() || ThemeUtils.shouldUseDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
