package com.nikolayux.masterchariot.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Grey2,
    onPrimaryContainer = White,
    secondary = Blue2,
    onSecondary = White,
    secondaryContainer = Grey1,
    onSecondaryContainer = White,
    tertiary = White,
    onTertiary = Black,
    tertiaryContainer = Grey3,
    onTertiaryContainer = White,
    background = Black,
    onBackground = White,
    surface = Grey2,
    onSurface = White,
    surfaceVariant = Grey1,
    onSurfaceVariant = White,
    surfaceContainer = Grey1,
    surfaceContainerHigh = Grey3,
    outline = White,
    outlineVariant = Grey5,
    error = Red,
    onError = Black
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = LightWhite,
    onPrimaryContainer = Black,
    secondary = Blue2,
    onSecondary = White,
    secondaryContainer = DirtWhite,
    onSecondaryContainer = Black,
    tertiary = Blue2,
    onTertiary = White,
    tertiaryContainer = LightBlue,
    onTertiaryContainer = Black,
    background = White,
    onBackground = Black,
    surface = LightWhite,
    onSurface = Black,
    surfaceVariant = DirtWhite,
    onSurfaceVariant = Black,
    surfaceContainer = DirtWhite,
    surfaceContainerHigh = LightBlue,
    outline = Black,
    outlineVariant = DirtWhite2,
    error = Red,
    onError = Black
)

@Composable
fun MasterChariotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
