package com.sololeveling.fitness.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ═══ COLORES ═══
val BgPrimary = Color(0xFF0D0D10)
val BgSecondary = Color(0xFF1A1A24)
val BgTertiary = Color(0xFF242436)
val BgCard = Color(0xFF1E1E2E)

val AccentBlue = Color(0xFF4A90D9)
val AccentPurple = Color(0xFF7B2FBE)
val AccentCyan = Color(0xFF00D4FF)
val AccentGold = Color(0xFFFFD700)
val AccentRed = Color(0xFFFF3333)
val AccentGreen = Color(0xFF39FF14)
val AccentOrange = Color(0xFFFF6B00)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0C8)
val TextTertiary = Color(0xFF6A6A80)

val RankE = Color(0xFF6B7280)
val RankD = Color(0xFF3B82F6)
val RankC = Color(0xFF22C55E)
val RankB = Color(0xFFF59E0B)
val RankA = Color(0xFFF97316)
val RankS = Color(0xFFEF4444)
val RankNational = Color(0xFFD946EF)
val RankMonarch = Color(0xFF8B5CF6)

val XpBarBg = Color(0xFF2A2A3A)
val StatusActive = Color(0xFF39FF14)
val StatusWarning = Color(0xFFF59E0B)
val StatusDanger = Color(0xFFEF4444)

// ═══ TIPOGRAFÍA ═══
val DisplayFont = FontFamily.Default  // Cambiar a Orbit Rajdhani si se añade el font

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
        color = TextPrimary, letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontSize = 24.sp, fontWeight = FontWeight.Bold,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontSize = 20.sp, fontWeight = FontWeight.Bold,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.Normal,
        color = TextSecondary
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Normal,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.Normal,
        color = TextTertiary
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.Medium,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp, fontWeight = FontWeight.Medium,
        color = TextTertiary
    )
)

// ═══ TEMA OSCURO SOLO LEVELING ═══
private val SoloLevelingColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BgPrimary,
    secondary = AccentPurple,
    onSecondary = TextPrimary,
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
    surfaceContainer = BgCard,
    surfaceContainerHigh = BgSecondary,
    surfaceContainerHighest = BgTertiary,
)

@Composable
fun SoloLevelingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SoloLevelingColorScheme,
        typography = AppTypography,
        content = content
    )
}
