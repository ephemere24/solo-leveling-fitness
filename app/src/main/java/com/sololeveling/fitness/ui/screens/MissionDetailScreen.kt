package com.sololeveling.fitness.ui.screens

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
 * Pantalla de detalle/ejecución de misión — Contador interactivo
 * Auto-completa al llegar al máximo.
 */
@Composable
fun MissionDetailScreen(
    mission: Mission,
    streakMultiplier: Double,
    onComplete: (Mission) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentCount by remember { mutableIntStateOf(mission.completedCount) }
    var isCompleted by remember { mutableStateOf(mission.isCompleted) }
    var showCelebration by remember { mutableStateOf(false) }
    var hasAutoCompleted by remember { mutableStateOf(false) }

    val target = mission.targetCount
    val progress = (currentCount.toFloat() / target).coerceIn(0f, 1f)
    val earnedXP = GameEngine.calculateMissionXP(mission, (streakMultiplier * 10).toInt().coerceAtLeast(1))

    // Auto-completar al llegar al máximo
    LaunchedEffect(currentCount) {
        if (currentCount >= target && !isCompleted && !hasAutoCompleted) {
            hasAutoCompleted = true
            isCompleted = true
            showCelebration = true
            val completedMission = mission.copy(
                completedCount = currentCount,
                isCompleted = true,
                completedDate = System.currentTimeMillis()
            )
            onComplete(completedMission)
        }
    }

    // Animación de pulso para el botón
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Icono de la misión
            val missionIcon = exerciseIcon(mission.type)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(alpha = 0.1f))
                    .border(2.dp, AccentCyan.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    missionIcon,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mission.type.displayName,
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = mission.type.primaryStat.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = AccentCyan
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Contador grande
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                AccentCyan.copy(alpha = 0.15f),
                                BgCard
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = if (isCompleted) AccentNeonGreen else AccentCyan,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$currentCount",
                        style = MaterialTheme.typography.displayLarge,
                        color = if (isCompleted) AccentNeonGreen else TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 56.sp
                    )
                    Text(
                        text = "/ $target",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Barra de progreso elegante
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
                color = if (isCompleted) AccentNeonGreen else AccentCyan,
                trackColor = XpBarBg
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isCompleted) {
                // Botones +/-
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón -
                    FilledIconButton(
                        onClick = { if (currentCount > 0) currentCount-- },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AccentRed.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Quitar", tint = AccentRed, modifier = Modifier.size(32.dp))
                    }

                    // Botón + (grande)
                    FilledIconButton(
                        onClick = { if (currentCount < target) currentCount++ },
                        modifier = Modifier
                            .size(80.dp)
                            .scale(pulseScale),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AccentCyan
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir", tint = BgPrimary, modifier = Modifier.size(40.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón completar (manual, por si acaso)
                Button(
                    onClick = {
                        if (currentCount >= target && !isCompleted) {
                            isCompleted = true
                            showCelebration = true
                            val completedMission = mission.copy(
                                completedCount = currentCount,
                                isCompleted = true,
                                completedDate = System.currentTimeMillis()
                            )
                            onComplete(completedMission)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentCount >= target) AccentNeonGreen else BgTertiary
                    ),
                    enabled = currentCount >= target && !isCompleted
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (currentCount >= target) BgPrimary else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "COMPLETAR MISIÓN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (currentCount >= target) BgPrimary else TextTertiary
                    )
                }
            } else {
                // Completada
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentNeonGreen.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = AccentNeonGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¡Misión Completada!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AccentNeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "+$earnedXP XP ganados",
                            style = MaterialTheme.typography.titleLarge,
                            color = AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                        if (streakMultiplier > 1.0) {
                            val multStr = "%.1fx".format(streakMultiplier)
                            Text(
                                "Multiplicador: $multStr",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // Celebración overlay
        if (showCelebration) {
            CelebrationOverlay(
                xp = earnedXP,
                onDismiss = { showCelebration = false }
            )
        }
    }
}

@Composable
fun CelebrationOverlay(xp: Int, onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "¡MISIÓN COMPLETADA!",
                style = MaterialTheme.typography.displayMedium,
                color = AccentGold,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "+$xp XP",
                style = MaterialTheme.typography.displayLarge,
                color = AccentCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
