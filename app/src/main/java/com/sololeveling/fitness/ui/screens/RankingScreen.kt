package com.sololeveling.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun RankingScreen(
    globalRanking: List<RankingEntry>,
    friendsRanking: List<RankingEntry>,
    myUserId: String,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
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
                            .height(2.dp)
                            .background(AccentCyan)
                    )
                }
            }
        ) {
            listOf("GLOBAL", "AMIGOS").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) AccentCyan else TextSecondary,
                            letterSpacing = 2.sp
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
        EmptyRankingState("Cargando ranking...", "Conectando con el servidor...")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (ranking.size >= 3) {
            item { PodiumView(ranking.take(3)) }
        }
        itemsIndexed(ranking) { index, entry ->
            RankingItem(index + 1, entry, entry.userId == myUserId)
        }
    }
}

@Composable
fun FriendsRankingList(ranking: List<RankingEntry>, myUserId: String) {
    if (ranking.size <= 1) {
        EmptyRankingState("Sin amigos aún", "Añade amigos con su código para competir")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(ranking) { index, entry ->
            RankingItem(index + 1, entry, entry.userId == myUserId)
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
        if (top3.size > 1) PodiumPlace(top3[1], 2, 80.dp, AccentCyan)
        PodiumPlace(top3[0], 1, 100.dp, AccentNeonGreen)
        if (top3.size > 2) PodiumPlace(top3[2], 3, 60.dp, AccentOrange)
    }
}

@Composable
fun PodiumPlace(entry: RankingEntry, place: Int, height: androidx.compose.ui.unit.Dp, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            text = when (place) { 1 -> "♛"; 2 -> "◆"; 3 -> "◇"; else -> "·" },
            fontSize = 28.sp,
            color = color
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
            text = "NV.${entry.level}",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(color.copy(alpha = 0.05f)),
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
fun RankingItem(position: Int, entry: RankingEntry, isMe: Boolean) {
    val rankColor = try {
        Color(android.graphics.Color.parseColor(entry.rank.colorHex))
    } catch (_: Exception) { TextSecondary }

    val accentColor = when (position) {
        1 -> AccentNeonGreen; 2 -> AccentCyan; 3 -> AccentOrange; else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isMe) Modifier.border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) AccentCyan.copy(alpha = 0.05f) else BgCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "#$position",
                style = MaterialTheme.typography.titleLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = 0.15f))
                    .border(1.dp, rankColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.rank.displayName.first().toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = rankColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isMe) "${entry.name} [TÚ]" else entry.name,
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
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatXP(entry.totalXP),
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
fun EmptyRankingState(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.TravelExplore, contentDescription = null, tint = AccentCyan.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}
