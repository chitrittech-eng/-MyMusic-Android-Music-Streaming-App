package com.mymusic.app.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = SpotifyBlack,
    primaryContainer = SpotifyGreenDark,
    onPrimaryContainer = SpotifyTextPrimary,
    secondary = SpotifyTextSecondary,
    onSecondary = SpotifyBlack,
    secondaryContainer = SpotifyGray,
    onSecondaryContainer = SpotifyTextPrimary,
    tertiary = SpotifyInfo,
    onTertiary = SpotifyTextPrimary,
    background = SpotifyBlack,
    onBackground = SpotifyTextPrimary,
    surface = SpotifyDarkGray,
    onSurface = SpotifyTextPrimary,
    surfaceVariant = SpotifyGray,
    onSurfaceVariant = SpotifyTextSecondary,
    outline = SpotifyLightGray,
    outlineVariant = SpotifyDivider,
    error = SpotifyError,
    onError = SpotifyTextPrimary,
    inverseSurface = SpotifyTextPrimary,
    inverseOnSurface = SpotifyBlack,
    inversePrimary = SpotifyGreenDark,
    scrim = SpotifyBlack
)

@Composable
fun MyMusicTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SpotifyBlack.toArgb()
            window.navigationBarColor = SpotifyBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
