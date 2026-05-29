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

    val target = mission.targetCount
    val progress = (currentCount.toFloat() / target).coerceIn(0f, 1f)
    val earnedXP = GameEngine.calculateMissionXP(mission, (streakMultiplier * 10).toInt().coerceAtLeast(1))

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

            // Título misión
            Text(
                text = exerciseEmoji(mission.type),
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mission.type.displayName,
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = mission.type.statCategory.displayName,
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
                        color = if (isCompleted) AccentGreen else AccentCyan,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$currentCount",
                        style = MaterialTheme.typography.displayLarge,
                        color = if (isCompleted) AccentGreen else TextPrimary,
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

            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(XpBarBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentCyan, AccentPurple)
                            )
                        )
                )
            }

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

                // Botón completar
                Button(
                    onClick = {
                        if (currentCount >= target) {
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
                        containerColor = if (currentCount >= target) AccentGreen else BgTertiary
                    ),
                    enabled = currentCount >= target
                ) {
                    Text(
                        "⚔️ COMPLETAR MISIÓN",
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
                    colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✅", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "¡Misión Completada!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AccentGreen,
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
                            Text(
                                "Multiplicador: ${"%.1fx".format(streakMultiplier)}",
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
            Text("⚔️", fontSize = 72.sp)
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
