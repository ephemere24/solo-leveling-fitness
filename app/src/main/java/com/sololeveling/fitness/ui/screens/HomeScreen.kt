package com.sololeveling.fitness.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.fitness.data.model.*
import com.sololeveling.fitness.ui.theme.*
import com.sololeveling.fitness.util.GameEngine

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
        item { HunterHeaderCard(userProfile, streakMultiplier) }
        item { XPProgressBar(userProfile) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Assignment,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "// MISIONES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AccentCyan,
                        letterSpacing = 2.sp
                    )
                }
                Text(
                    "[${dailyMissions.count { it.isCompleted }}/${dailyMissions.size}]",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (dailyMissions.all { it.isCompleted }) AccentNeonGreen else TextSecondary
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
        item { StatsMiniBar(userProfile.stats) }
    }
}

@Composable
fun HunterHeaderCard(profile: UserProfile, multiplier: Double) {
    val rankColor = try {
        Color(android.graphics.Color.parseColor(profile.rank.colorHex))
    } catch (_: Exception) { AccentCyan }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, rankColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
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
                        text = profile.name.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NV.${profile.level}",
                        style = MaterialTheme.typography.titleMedium,
                        color = rankColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "NIVEL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "${profile.level}",
                        style = MaterialTheme.typography.displayLarge,
                        color = AccentCyan,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(rankColor.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakBadge(profile.currentStreak, multiplier)
                StatPillar("MISIONES", "${profile.missionsCompleted}")
                StatPillar("XP_TOTAL", formatXP(profile.totalXP))
                StatPillar("RACHA_MAX", "${profile.longestStreak}d")
            }
        }
    }
}

@Composable
fun StreakBadge(streak: Int, multiplier: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = if (streak > 0) AccentNeonGreen else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$streak",
                style = MaterialTheme.typography.titleLarge,
                color = if (streak > 0) AccentNeonGreen else TextTertiary,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "x${multiplier.coerceAtMost(99.0).let { "%.1f".format(it) }}",
            style = MaterialTheme.typography.labelSmall,
            color = if (streak > 7) AccentNeonGreen else TextTertiary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun StatPillar(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun XPProgressBar(profile: UserProfile) {
    val animatedProgress by animateFloatAsState(
        targetValue = profile.xpProgress,
        animationSpec = tween(durationMillis = 800),
        label = "xpProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, AccentCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "NV.${profile.level}",
                style = MaterialTheme.typography.labelLarge,
                color = AccentCyan,
                letterSpacing = 1.sp
            )
            Text(
                "NV.${profile.level + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(XpBarBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentCyan, AccentNeonGreen)
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
    val borderColor = if (mission.isCompleted) AccentNeonGreen.copy(alpha = 0.5f) else AccentCyan.copy(alpha = 0.2f)
    val iconVec = exerciseIcon(mission.type)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
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
            // Mission type icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (mission.isCompleted) AccentNeonGreen.copy(alpha = 0.15f)
                        else AccentCyan.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (mission.isCompleted) AccentNeonGreen.copy(alpha = 0.5f) else AccentCyan.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    iconVec,
                    contentDescription = mission.type.displayName,
                    tint = if (mission.isCompleted) AccentNeonGreen else AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mission.type.displayName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    if (mission.isCompleted) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Completada",
                            tint = AccentNeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${mission.completedCount}/${mission.targetCount} reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (!mission.isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = mission.progressPercent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AccentCyan,
                        trackColor = XpBarBg
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${GameEngine.calculateMissionXP(mission, (streakMultiplier * 10).toInt().coerceAtLeast(1))}",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentNeonGreen,
                    fontWeight = FontWeight.Bold
                )
                Text("XP", style = MaterialTheme.typography.labelSmall, color = TextTertiary, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
fun StatsMiniBar(stats: PlayerStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AccentCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "// STATS",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentCyan,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatMiniItem("STR", stats.strength, AccentRed, Icons.Filled.FitnessCenter)
                StatMiniItem("VEL", stats.speed, AccentCyan, Icons.Filled.FlashOn)
                StatMiniItem("RES", stats.endurance, AccentNeonGreen, Icons.Filled.DirectionsRun)
                StatMiniItem("AGU", stats.stamina, AccentOrange, Icons.Filled.LocalFireDepartment)
                StatMiniItem("FLE", stats.flexibility, AccentPurple, Icons.Filled.SelfImprovement)
            }
        }
    }
}

@Composable
fun StatMiniItem(label: String, value: Int, color: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            letterSpacing = 1.sp
        )
    }
}

fun exerciseIcon(type: ExerciseType): ImageVector = when (type) {
    ExerciseType.PUSHUPS -> Icons.Filled.FitnessCenter
    ExerciseType.SQUATS -> Icons.Filled.FitnessCenter
    ExerciseType.PULLUPS -> Icons.Filled.FitnessCenter
    ExerciseType.ABDOMINALS -> Icons.Filled.SportsMartialArts
    ExerciseType.SPRINT_100M -> Icons.Filled.DirectionsRun
    ExerciseType.RUNNING_3KM -> Icons.Filled.DirectionsRun
    ExerciseType.LUNGES -> Icons.Filled.DirectionsRun
    ExerciseType.JUMP_ROPE -> Icons.Filled.FitnessCenter
    ExerciseType.STRETCHING -> Icons.Filled.SelfImprovement
}

fun formatXP(xp: Int): String = when {
    xp >= 1_000_000 -> "%.1fM".format(xp / 1_000_000.0)
    xp >= 1_000 -> "%.1fK".format(xp / 1_000.0)
    else -> "$xp"
}
