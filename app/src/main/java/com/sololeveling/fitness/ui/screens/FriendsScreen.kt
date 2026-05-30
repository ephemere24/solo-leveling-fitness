package com.sololeveling.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.fitness.data.model.*
import com.sololeveling.fitness.ui.theme.*

@Composable
fun FriendsScreen(
    friends: List<Friendship>,
    myFriendCode: String,
    onAddFriend: (String) -> Unit,
    onChallenge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var friendCodeInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BgCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "TU CÓDIGO",
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentCyan,
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = myFriendCode.ifEmpty { "---------" },
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Comparte para que te añadan",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BgCard)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AccentCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AÑADIR AMIGO", color = AccentCyan, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "AMIGOS [${friends.size}]",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (friends.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("◇", fontSize = 48.sp, color = TextTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Añade amigos para competir",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(friends) { friend ->
                    FriendCard(friend = friend, onChallenge = { onChallenge(friend.friendId) })
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = BgCard,
            title = { Text("AÑADIR AMIGO", color = AccentCyan, letterSpacing = 2.sp) },
            text = {
                Column {
                    Text("Introduce el código de tu amigo:", color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = friendCodeInput,
                        onValueChange = { friendCodeInput = it.uppercase() },
                        placeholder = { Text("ABC12345") },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (friendCodeInput.isNotBlank()) {
                            onAddFriend(friendCodeInput.trim())
                            friendCodeInput = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("AÑADIR", color = BgPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun FriendCard(friend: Friendship, onChallenge: () -> Unit) {
    val rankColor = try {
        Color(android.graphics.Color.parseColor(HunterRank.forLevel(friend.friendLevel).colorHex))
    } catch (_: Exception) { AccentCyan }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, rankColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.15f))
                    .border(1.dp, rankColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.friendName.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = rankColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.friendName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Nivel ${friend.friendLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            OutlinedButton(
                onClick = onChallenge,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.FlashOn, contentDescription = null, tint = AccentRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retar", color = AccentRed, fontSize = 12.sp)
            }
        }
    }
}
