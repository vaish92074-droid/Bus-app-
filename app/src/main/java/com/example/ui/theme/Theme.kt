package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SmartBusPrimaryDark,
    onPrimary = SmartBusOnPrimaryDark,
    primaryContainer = SmartBusPrimaryContainerDark,
    onPrimaryContainer = SmartBusOnPrimaryContainerDark,
    secondary = SmartBusSecondaryDark,
    onSecondary = SmartBusOnSecondaryDark,
    secondaryContainer = SmartBusSecondaryContainerDark,
    onSecondaryContainer = SmartBusOnSecondaryContainerDark,
    tertiary = SmartBusTertiaryDark,
    onTertiary = SmartBusOnTertiaryDark,
    background = SmartBusBackgroundDark,
    surface = SmartBusSurfaceDark,
    surfaceVariant = SmartBusSurfaceVariantDark,
    outline = SmartBusOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = SmartBusPrimary,
    onPrimary = SmartBusOnPrimary,
    primaryContainer = SmartBusPrimaryContainer,
    onPrimaryContainer = SmartBusOnPrimaryContainer,
    secondary = SmartBusSecondary,
    onSecondary = SmartBusOnSecondary,
    secondaryContainer = SmartBusSecondaryContainer,
    onSecondaryContainer = SmartBusOnSecondaryContainer,
    tertiary = SmartBusTertiary,
    onTertiary = SmartBusOnTertiary,
    background = SmartBusBackgroundLight,
    surface = SmartBusSurfaceLight,
    surfaceVariant = SmartBusSurfaceVariantLight,
    outline = SmartBusOutlineLight
)

@Composable
fun SmartBusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature transit colors for consistent branding
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SmartBusTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
