package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = Color(0xFF231700),
    primaryContainer = DeepGold,
    onPrimaryContainer = Color(0xFFFFECCF),
    secondary = AgateLight,
    onSecondary = Color.White,
    secondaryContainer = YemeniAgate,
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = YemeniEmerald,
    onTertiary = Color.White,
    background = HeritageDarkBackground,
    onBackground = TextPrimaryDark,
    surface = HeritageDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = HeritageDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = HeritageDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = DeepGold,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECCF),
    onPrimaryContainer = Color(0xFF281800),
    secondary = YemeniAgate,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = YemeniEmerald,
    onTertiary = Color.White,
    background = HeritageLightBackground,
    onBackground = TextPrimaryLight,
    surface = HeritageLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = HeritageLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = HeritageLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // نستخدم ألوان الهوية التراثية الأصيلة
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
