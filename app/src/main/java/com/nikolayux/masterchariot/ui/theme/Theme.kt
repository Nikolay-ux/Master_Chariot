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
    secondary = Blue2,
    tertiary = White,
    background = Black,


//    primary: Color = ColorDarkTokens.Primary,
//    onPrimary: Color = ColorDarkTokens.OnPrimary,
    primaryContainer = Grey2,
//    onPrimaryContainer: Color = ColorDarkTokens.OnPrimaryContai…,
//    inversePrimary: Color = ColorDarkTokens.InversePrimary,
//    secondary: Color = ColorDarkTokens.Secondary,
//    onSecondary: Color = ColorDarkTokens.OnSecondary,
    secondaryContainer = Grey1,
//    onSecondaryContainer: Color = ColorDarkTokens.OnSecondaryCont…,
//    tertiary: Color = ColorDarkTokens.Tertiary,
//    onTertiary: Color = ColorDarkTokens.OnTertiary,
    tertiaryContainer = Grey3,
//    onTertiaryContainer: Color = ColorDarkTokens.OnTertiaryConta…,
//    background: Color = ColorDarkTokens.Background,
//    onBackground: Color = ColorDarkTokens.OnBackground,
    surface = Blue2,
    onSurface = Blue5,
//    surfaceVariant: Color = ColorDarkTokens.SurfaceVariant,
    onSurfaceVariant = Black,
//    surfaceTint: Color = primary,
//    inverseSurface: Color = ColorDarkTokens.InverseSurface,
//    inverseOnSurface: Color = ColorDarkTokens.InverseOnSurface,
//    error: Color = ColorDarkTokens.Error,
//    onError: Color = ColorDarkTokens.OnError,
//    errorContainer: Color = ColorDarkTokens.ErrorContainer,
//    onErrorContainer: Color = ColorDarkTokens.OnErrorContainer,
    outline = White,
    outlineVariant = Grey5,
//    scrim: Color = ColorDarkTokens.Scrim,
//    surfaceBright: Color = ColorDarkTokens.SurfaceBright,
    surfaceContainer = White,
//    surfaceContainerHigh: Color = ColorDarkTokens.SurfaceContaine…,
//    surfaceContainerHighest: Color = ColorDarkTokens.SurfaceContaine…,
//    surfaceContainerLow: Color = ColorDarkTokens.SurfaceContaine…,
//    surfaceContainerLowest: Color = ColorDarkTokens.SurfaceContaine…,
//    surfaceDim: Color = ColorDarkTokens.SurfaceDim,
//    primaryFixed: Color = ColorDarkTokens.PrimaryFixed,
//    primaryFixedDim: Color = ColorDarkTokens.PrimaryFixedDim,
//    onPrimaryFixed: Color = ColorDarkTokens.OnPrimaryFixed,
//    onPrimaryFixedVariant: Color = ColorDarkTokens.OnPrimaryFixedV…,
//    secondaryFixed: Color = ColorDarkTokens.SecondaryFixed,
//    secondaryFixedDim: Color = ColorDarkTokens.SecondaryFixedD…,
//    onSecondaryFixed: Color = ColorDarkTokens.OnSecondaryFixed,
//    onSecondaryFixedVariant: Color = ColorDarkTokens.OnSecondaryFixe…,
//    tertiaryFixed: Color = ColorDarkTokens.TertiaryFixed,
//    tertiaryFixedDim: Color = ColorDarkTokens.TertiaryFixedDim,
//    onTertiaryFixed: Color = ColorDarkTokens.OnTertiaryFixed,
//    onTertiaryFixedVariant: Color = ColorDarkTokens.OnTertiaryFixed…
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    secondary = Blue2,
    tertiary = Blue2,
    background = White,
    outline = White,
    primaryContainer = LightWhite,
    secondaryContainer = DirtWhite,
    outlineVariant = Grey5,
    tertiaryContainer = LightBlue,
    surface = White,
    surfaceContainer = Blue2,
    onSurface = Grey5,
    onSurfaceVariant = Blue5,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
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
