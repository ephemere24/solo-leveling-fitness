package com.sololeveling.fitness.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ═══ COLORES CYBERPUNK OLED ═══
// Fondos — negro puro para OLED
val BgPrimary = Color(0xFF000000)      // Negro puro OLED
val BgSecondary = Color(0xFF0A0A0A)    // Casi negro
val BgTertiary = Color(0xFF111111)     // Gris muy oscuro
val BgCard = Color(0xFF0D0D0D)         // Negro con pelín de gris

// Acentos neÓN
val AccentCyan = Color(0xFF00FFFF)       // Cian neón
val AccentMagenta = Color(0xFF00FF88)    // Verde neón (estilo Matrix/cyberpunk)
val AccentNeonGreen = Color(0xFF39FF14)  // Verde neón puro
val AccentGold = Color(0xFFFFFF00)       // Amarillo neón
val AccentRed = Color(0xFFFF0044)        // Rojo neón
val AccentOrange = Color(0xFFFF6600)     // Naranja neón
val AccentPurple = Color(0xFFBF00FF)     // Púrpura neón

// Texto
val TextPrimary = Color(0xFFFFFFFF)      // Blanco puro
val TextSecondary = Color(0xFFAAAAAA)    // Gris claro
val TextTertiary = Color(0xFF555555)     // Gris medio

// Rangos
val RankE = Color(0xFF666666)
val RankD = Color(0xFF00FFFF)      // Cian
val RankC = Color(0xFF39FF14)      // Verde neón
val RankB = Color(0xFFFFFF00)      // Amarillo
val RankA = Color(0xFFFF6600)      // Naranja
val RankS = Color(0xFFFF0044)      // Rojo neón
val RankNational = Color(0xFFBF00FF) // Púrpura
val RankMonarch = Color(0xFFFFFFFF)  // Blanco (supremo)

// Misc
val XpBarBg = Color(0xFF1A1A1A)
val StatusActive = Color(0xFF39FF14)
val StatusWarning = Color(0xFFFFFF00)
val StatusDanger = Color(0xFFFF0044)
val GlowCyan = Color(0x4000FFFF)    // Cian con glow
val GlowMagenta = Color(0x40FF00FF) // Magenta con glow

// ═══ TIPOGRAFÍA ═══
val DisplayFont = FontFamily.Monospace  // Estilo terminal/cyberpunk

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
        color = TextPrimary, letterSpacing = 1.sp, fontFamily = DisplayFont
    ),
    displayMedium = TextStyle(
        fontSize = 24.sp, fontWeight = FontWeight.Bold,
        color = TextPrimary, letterSpacing = 0.5.sp, fontFamily = DisplayFont
    ),
    headlineLarge = TextStyle(
        fontSize = 20.sp, fontWeight = FontWeight.Bold,
        color = TextPrimary, letterSpacing = 0.5.sp, fontFamily = DisplayFont
    ),
    headlineMedium = TextStyle(
        fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
        color = TextPrimary, fontFamily = DisplayFont
    ),
    titleLarge = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        color = TextPrimary, fontFamily = DisplayFont
    ),
    titleMedium = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Medium,
        color = TextPrimary, fontFamily = DisplayFont
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.Normal,
        color = TextSecondary, fontFamily = DisplayFont
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Normal,
        color = TextSecondary, fontFamily = DisplayFont
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.Normal,
        color = TextTertiary, fontFamily = DisplayFont
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        color = TextPrimary, fontFamily = DisplayFont
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.Medium,
        color = TextSecondary, fontFamily = DisplayFont
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp, fontWeight = FontWeight.Medium,
        color = TextTertiary, fontFamily = DisplayFont
    )
)

// ═══ TEMA OSCURO CYBERPUNK OLED ═══
private val CyberpunkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BgPrimary,
    secondary = AccentNeonGreen,
    onSecondary = BgPrimary,
    tertiary = AccentGold,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgCard,
    onSurface = TextPrimary,
    surfaceVariant = BgTertiary,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = TextPrimary,
    outline = TextTertiary,
    outlineVariant = BgTertiary,
)

@Composable
fun SoloLevelingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberpunkColorScheme,
        typography = AppTypography,
        content = content
    )
}
