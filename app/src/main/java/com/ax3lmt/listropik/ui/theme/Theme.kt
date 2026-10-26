package com.ax3lmt.listropik.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PumpkinOrange,
    onPrimary = Color.Black,
    primaryContainer = WitchPurpleDark,
    onPrimaryContainer = PumpkinOrangeLight,
    secondary = SlimeGreen,
    onSecondary = Color.Black,
    secondaryContainer = SlimeGreenDark,
    onSecondaryContainer = Color.White,
    tertiary = WitchPurpleLight,
    onTertiary = Color.Black,
    background = SpookyDarkBackground,
    onBackground = Color.White,
    surface = SpookyDarkSurface,
    onSurface = Color.White,
    surfaceVariant = WitchPurpleDark,
    onSurfaceVariant = PumpkinOrangeLight
)

private val LightColorScheme = lightColorScheme(
    primary = PumpkinOrange,
    onPrimary = Color.White,
    primaryContainer = SpookyLightSurface,
    onPrimaryContainer = PumpkinOrangeDark,
    secondary = SlimeGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = SlimeGreenDark,
    tertiary = WitchPurple,
    onTertiary = Color.White,
    background = SpookyLightBackground,
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3E5F5),
    onSurfaceVariant = WitchPurpleDark
)

@Composable
fun ListropikTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor = false so the custom Halloween palette is used instead of Android wallpaper colors
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
