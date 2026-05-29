package com.sololeveling.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.sololeveling.fitness.util.GameEngine

/**
 * Pantalla Home — Panel principal del cazador con misiones diarias
 */
@Composable
fun HomeScreen(
    userProfile: UserProfile,
    dailyMissions: List<Mission>,
    streakMultiplier: Double,
    onMissionClick: (Mission) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // ═══ HEADER: Nivel y Rango ═══
        item {
            HunterHeaderCard(userProfile, streakMultiplier)
        }

        // ═══ BARRA DE XP ═══
        item {
            XPProgressBar(userProfile)
        }

        // ═══ MISIONES DIARIAS ═══
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⚔️ Misiones Diarias",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    "${dailyMissions.count { it.isCompleted }}/${dailyMissions.size}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (dailyMissions.all { it.isCompleted }) AccentGreen else AccentCyan
                )
            }
        }

        itemsIndexed(dailyMissions) { _, mission ->
            MissionCard(
                mission = mission,
                streakMultiplier = streakMultiplier,
                onClick = { onMissionClick(mission) }
            )
        }

        // ═══ RESUMEN STATS ═══
        item {
            StatsMiniBar(userProfile.stats)
        }
    }
}

@Composable
fun HunterHeaderCard(profile: UserProfile, multiplier: Double) {
    val rankColor = try {
        Color(android.graphics.Color.parseColor(profile.rank.colorHex))
    } catch (_: Exception) { AccentPurple }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        BgCard,
                        rankColor.copy(alpha = 0.15f),
                        BgCard
                    )
                )
            )
            .border(
                width = 1.dp,
                color = rankColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.hunterTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = rankColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NIVEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "${profile.level}",
                        style = MaterialTheme.typography.displayLarge,
                        color = rankColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streak & multiplier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakBadge(profile.currentStreak, multiplier)
                StatPillar("🏆", "Misiones", "${profile.missionsCompleted}")
                StatPillar(
                    "⭐", "Total XP",
                    formatXP(profile.totalXP)
                )
                StatPillar("📅", "Racha Max", "${profile.longestStreak}d")
            }
        }
    }
}

@Composable
fun StreakBadge(streak: Int, multiplier: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "🔥 $streak",
            style = MaterialTheme.typography.titleLarge,
            color = if (streak > 0) AccentOrange else TextTertiary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${multiplier.coerceAtMost(99.0).let { "%.1fx".format(it) }}",
            style = MaterialTheme.typography.labelMedium,
            color = if (streak > 7) AccentGreen else TextSecondary
        )
    }
}

@Composable
fun StatPillar(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 18.sp)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

@Composable
fun XPProgressBar(profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Nivel ${profile.level}", style = MaterialTheme.typography.labelLarge, color = AccentCyan)
            Text(
                "Nivel ${profile.level + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(XpBarBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(profile.xpProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentCyan, AccentPurple)
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${profile.xp} / ${profile.xpForNextLevel} XP",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionCard(
    mission: Mission,
    streakMultiplier: Double,
    onClick: () -> Unit
) {
    val rankColor = try {
        Color(android.graphics.Color.parseColor(HunterRank.forLevel(mission.baseXP / 10 + 1).colorHex))
    } catch (_: Exception) { AccentBlue }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.isCompleted) BgTertiary else BgCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono de tipo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (mission.isCompleted) AccentGreen.copy(alpha = 0.15f)
                        else rankColor.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = exerciseEmoji(mission.type),
                    fontSize = 24.sp
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mission.type.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    if (mission.isCompleted) {
                        Text("✅", fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${mission.completedCount}/${mission.targetCount} repeticiones",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (!mission.isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(XpBarBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(mission.progressPercent)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(rankColor)
                        )
                    }
                }
            }

            // XP reward
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${GameEngine.calculateMissionXP(mission, (streakMultiplier * 10).toInt().coerceAtLeast(1))}",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentGold,
                    fontWeight = FontWeight.Bold
                )
                Text("XP", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
    }
}

@Composable
fun StatsMiniBar(stats: PlayerStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📊 Estadísticas",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatMiniItem("💪", "FUE", stats.strength, AccentRed)
                StatMiniItem("⚡", "VEL", stats.speed, AccentCyan)
                StatMiniItem("🏃", "RES", stats.endurance, AccentGreen)
                StatMiniItem("🔥", "AGU", stats.stamina, AccentOrange)
                StatMiniItem("🧘", "FLE", stats.flexibility, AccentPurple)
            }
        }
    }
}

@Composable
fun StatMiniItem(icon: String, label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

fun exerciseEmoji(type: ExerciseType): String = when (type) {
    ExerciseType.PUSHUPS -> "💪"
    ExerciseType.SQUATS -> "🦵"
    ExerciseType.PULLUPS -> "🏋️"
    ExerciseType.ABDOMINALS -> "🔥"
    ExerciseType.PLANK -> "🧱"
    ExerciseType.BURPEES -> "💥"
    ExerciseType.RUNNING -> "🏃"
    ExerciseType.MOUNTAIN_CLIMBERS -> "⛰️"
    ExerciseType.JUMPING_JACKS -> "⭐"
    ExerciseType.STRETCHING -> "🧘"
}

fun formatXP(xp: Int): String = when {
    xp >= 1_000_000 -> "%.1fM".format(xp / 1_000_000.0)
    xp >= 1_000 -> "%.1fK".format(xp / 1_000.0)
    else -> "$xp"
}
