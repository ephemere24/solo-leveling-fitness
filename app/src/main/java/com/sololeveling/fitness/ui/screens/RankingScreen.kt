package com.sololeveling.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.fitness.data.model.*
import com.sololeveling.fitness.ui.theme.*

/**
 * Pantalla de Ranking — Global y de Amigos
 */
@Composable
fun RankingScreen(
    globalRanking: List<RankingEntry>,
    friendsRanking: List<RankingEntry>,
    myUserId: String,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("🏆 Global", "👥 Amigos")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BgSecondary,
            contentColor = AccentCyan,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .width(tabPositions[selectedTab].width)
                            .offset(x = tabPositions[selectedTab].left)
                            .height(3.dp)
                            .background(AccentCyan)
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) AccentCyan else TextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> GlobalRankingList(globalRanking, myUserId)
            1 -> FriendsRankingList(friendsRanking, myUserId)
        }
    }
}

@Composable
fun GlobalRankingList(ranking: List<RankingEntry>, myUserId: String) {
    if (ranking.isEmpty()) {
        EmptyRankingState("Cargando ranking global…")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top 3 podium
        if (ranking.size >= 3) {
            item { PodiumView(ranking.take(3)) }
        }

        itemsIndexed(ranking) { index, entry ->
            RankingItem(
                position = index + 1,
                entry = entry,
                isMe = entry.userId == myUserId
            )
        }
    }
}

@Composable
fun FriendsRankingList(ranking: List<RankingEntry>, myUserId: String) {
    if (ranking.isEmpty()) {
        EmptyRankingState("¡Añade amigos para competir!")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(ranking) { index, entry ->
            RankingItem(
                position = index + 1,
                entry = entry,
                isMe = entry.userId == myUserId
            )
        }
    }
}

@Composable
fun PodiumView(top3: List<RankingEntry>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd place
        if (top3.size > 1) {
            PodiumPlace(top3[1], 2, 80.dp, AccentCyan)
        }
        // 1st place
        PodiumPlace(top3[0], 1, 100.dp, AccentGold)
        // 3rd place
        if (top3.size > 2) {
            PodiumPlace(top3[2], 3, 60.dp, AccentOrange)
        }
    }
}

@Composable
fun PodiumPlace(entry: RankingEntry, place: Int, height: androidx.compose.ui.unit.Dp, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            text = when (place) {
                1 -> "👑"
                2 -> "🥈"
                else -> "🥉"
            },
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = "Nv.${entry.level}",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$place",
                style = MaterialTheme.typography.displayMedium,
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun RankingItem(
    position: Int,
    entry: RankingEntry,
    isMe: Boolean
) {
    val rankColor = try {
        Color(android.graphics.Color.parseColor(entry.rank.colorHex))
    } catch (_: Exception) { TextSecondary }

    val bgColor = when {
        isMe -> AccentCyan.copy(alpha = 0.1f)
        position <= 3 -> AccentGold.copy(alpha = 0.05f)
        else -> BgCard
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isMe) Modifier.border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Position
            Text(
                text = "#$position",
                style = MaterialTheme.typography.titleLarge,
                color = when (position) {
                    1 -> AccentGold
                    2 -> AccentCyan
                    3 -> AccentOrange
                    else -> TextTertiary
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )

            // Rank badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.rank.displayName.first().toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = rankColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Name & info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isMe) "${entry.name} (Tú)" else entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isMe) AccentCyan else TextPrimary,
                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "${entry.rank.displayName} · ${entry.missionsCompleted} misiones",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            // XP
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatXP(entry.totalXP),
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
fun EmptyRankingState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏆", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
