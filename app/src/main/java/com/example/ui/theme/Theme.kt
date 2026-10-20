package com.example.ui.theme

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

private val BigDealDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldLight,
    secondary = ElectricCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF083344),
    onSecondaryContainer = CyanLight,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF451A03),
    onTertiaryContainer = GoldLight,
    error = CrimsonAlert,
    onError = Color.White,
    background = ExecutiveMidnight,
    onBackground = TextWhite,
    surface = ExecutiveSurface,
    onSurface = TextWhite,
    surfaceVariant = ExecutiveSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = ExecutiveBorder
)

private val BigDealLightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF0E7490),
    onSecondary = Color.White,
    tertiary = Color(0xFFB45309),
    background = LightSurface,
    onBackground = TextDark,
    surface = LightCard,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = LightBorder
)

@Composable
fun BigDealTheme(
    darkTheme: Boolean = true, // Default to sleek executive dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> BigDealDarkColorScheme
        else -> BigDealLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
