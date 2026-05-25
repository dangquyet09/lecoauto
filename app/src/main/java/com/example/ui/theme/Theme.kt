package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class CarThemeStyle(val displayName: String) {
    TESLA("Tesla Minimalist"),
    BMW("BMW M-Sport"),
    MERCEDES("Mercedes Ambient"),
    CYBERPUNK("Cyberpunk Neon 3D")
}

// 1. Tesla Color Scheme
private val TeslaColors = darkColorScheme(
    primary = TeslaRed,
    secondary = Color(0xFFFF4D4D),
    tertiary = Color(0xFF555555),
    background = TeslaBg,
    surface = TeslaCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF5F5F7),
    onSurface = Color(0xFFE5E5EA)
)

// 2. BMW Color Scheme
private val BMWColors = darkColorScheme(
    primary = BMWCyan,
    secondary = BMWBlue,
    tertiary = BMWMiniRed,
    background = BMWBg,
    surface = BMWCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFCFD8DC)
)

// 3. Mercedes Color Scheme
private val MercedesColors = darkColorScheme(
    primary = MercedesCyan,
    secondary = MercedesAmbientGold,
    tertiary = Color(0xFF3A4D5C),
    background = MercedesBg,
    surface = MercedesCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFECEFF1),
    onSurface = Color(0xFFB0BEC5)
)

// 4. Cyberpunk Color Scheme
private val CyberpunkColors = darkColorScheme(
    primary = CyberYellow,
    secondary = CyberPink,
    tertiary = CyberCyan,
    background = CyberBg,
    surface = CyberCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color(0xFFFCEE09),
    onSurface = Color(0xFF00FDFD)
)

@Composable
fun MyApplicationTheme(
    themeStyle: CarThemeStyle = CarThemeStyle.CYBERPUNK,
    content: @Composable () -> Unit
) {
    val colors = when (themeStyle) {
        CarThemeStyle.TESLA -> TeslaColors
        CarThemeStyle.BMW -> BMWColors
        CarThemeStyle.MERCEDES -> MercedesColors
        CarThemeStyle.CYBERPUNK -> CyberpunkColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
