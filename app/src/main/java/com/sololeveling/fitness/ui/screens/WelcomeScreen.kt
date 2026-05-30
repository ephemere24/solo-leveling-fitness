package com.sololeveling.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.sololeveling.fitness.ui.theme.*
import com.sololeveling.fitness.util.GameEngine

@Composable
fun WelcomeScreen(
    onStart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Scanline effect overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            AccentCyan.copy(alpha = 0.03f),
                            Color.Transparent,
                            AccentCyan.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Icon(
                Icons.Filled.Shield,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "SOLO LEVELING",
                style = MaterialTheme.typography.displayLarge,
                color = AccentCyan,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
            Text(
                "FITNESS",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Neon line divider
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, AccentCyan, Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "// Entrena como un Cazador",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Text(
                "// Sube de nivel. Domina tu cuerpo.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Input card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        ">> ¿NOMBRE DEL CAZADOR?",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentCyan,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Ingresa tu nombre...", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = TextTertiary,
                            cursorColor = AccentCyan
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onStart(name.trim()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        enabled = name.isNotBlank()
                    ) {
                        Text(
                            "INICIAR",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = BgPrimary,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureHint(Icons.Filled.Assignment, "MISIONES")
                FeatureHint(Icons.Filled.BarChart, "STATS")
                FeatureHint(Icons.Filled.Leaderboard, "RANKING")
                FeatureHint(Icons.Filled.LocalFireDepartment, "RACHAS")
            }
        }
    }
}

@Composable
fun FeatureHint(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            letterSpacing = 1.sp
        )
    }
}
