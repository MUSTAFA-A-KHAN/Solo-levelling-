package com.sololeveling.system.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// The System only exists in Dark Mode
private val SystemColorScheme = darkColorScheme(
    primary = SystemNeonBlue,
    secondary = SystemNeonPurple,
    tertiary = SystemNeonBlueVariant,
    background = SystemBackground,
    surface = SystemSurface,
    onPrimary = TrueBlack,
    onSecondary = TextPrimary,
    onTertiary = TrueBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SystemSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = StatusError
)

@Composable
fun SystemTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TrueBlack.toArgb()
            window.navigationBarColor = TrueBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = SystemColorScheme,
        typography = SystemTypography,
        content = content
    )
}
