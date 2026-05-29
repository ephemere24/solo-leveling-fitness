package com.sololeveling.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.fitness.data.model.*
import com.sololeveling.fitness.ui.theme.*

/**
 * Pantalla de Perfil con estadísticas detalladas y logros
 */
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    achievements: List<Achievement>,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // ═══ HEADER PERFIL ═══
        item {
            ProfileHeader(userProfile)
        }

        // ═══ STATS DETALLADOS ═══
        item {
            DetailedStatsCard(userProfile.stats)
        }

        // ═══ LOGROS ═══
        item {
            Text(
                "🏅 Logros",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        items(achievements.chunked(2)) { rowAchievements ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowAchievements.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowAchievements.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // ═══ AJUSTES ═══
        item {
            SettingsSection(onLogout)
        }
    }
}

@Composable
fun ProfileHeader(profile: UserProfile) {
    val rankColor = try {
        Color(android.graphics.Color.parseColor(profile.rank.colorHex))
    } catch (_: Exception) { AccentPurple }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        rankColor.copy(alpha = 0.2f),
                        BgCard
                    )
                )
            )
            .border(1.dp, rankColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.2f))
                    .border(2.dp, rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.first().uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    color = rankColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = profile.hunterTitle,
                style = MaterialTheme.typography.titleMedium,
                color = rankColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Quick stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStat("Nivel", "${profile.level}", AccentCyan)
                QuickStat("Misiones", "${profile.missionsCompleted}", AccentGold)
                QuickStat("Racha", "${profile.currentStreak}🔥", AccentOrange)
                QuickStat("Total XP", formatXP(profile.totalXP), AccentGreen)
            }
        }
    }
}

@Composable
fun QuickStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

@Composable
fun DetailedStatsCard(stats: PlayerStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "📊 Estadísticas de Cazador",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            StatBar("💪 Fuerza", stats.strength, AccentRed, 200)
            Spacer(modifier = Modifier.height(12.dp))
            StatBar("⚡ Velocidad", stats.speed, AccentCyan, 200)
            Spacer(modifier = Modifier.height(12.dp))
            StatBar("🏃 Resistencia", stats.endurance, AccentGreen, 200)
            Spacer(modifier = Modifier.height(12.dp))
            StatBar("🔥 Aguante", stats.stamina, AccentOrange, 200)
            Spacer(modifier = Modifier.height(12.dp))
            StatBar("🧘 Flexibilidad", stats.flexibility, AccentPurple, 200)

            Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = BgTertiary)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Stats", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                Text(
                    "${stats.total}",
                    style = MaterialTheme.typography.titleLarge,
                    color = AccentGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatBar(label: String, value: Int, color: Color, maxValue: Int) {
    val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text(
                "$value",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(XpBarBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.7f), color)
                        )
                    )
            )
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement, modifier: Modifier = Modifier) {
    val bgColor = if (achievement.isUnlocked) {
        AccentGold.copy(alpha = 0.1f)
    } else {
        BgTertiary
    }
    val borderColor = if (achievement.isUnlocked) {
        AccentGold.copy(alpha = 0.4f)
    } else {
        Color.Transparent
    }

    Card(
        modifier = modifier
            .then(
                if (achievement.isUnlocked)
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (achievement.isUnlocked) achievement.icon else "🔒",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelLarge,
                color = if (achievement.isUnlocked) TextPrimary else TextTertiary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (!achievement.isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = achievement.progressPercent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AccentCyan,
                    trackColor = XpBarBg
                )
                Text(
                    "${achievement.currentProgress}/${achievement.requirement}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
fun SettingsSection(onLogout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("⚙️ Ajustes", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            SettingItem("🔔 Notificaciones", "Recordatorios diarios") {}
            SettingItem("🌙 Modo oscuro", "Activado") {}
            SettingItem("📤 Compartir perfil", "Invita a amigos") {}

            Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = BgTertiary)
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚪 Cerrar Sesión", color = AccentRed)
            }
        }
    }
}

@Composable
fun SettingItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary
        )
    }
}
