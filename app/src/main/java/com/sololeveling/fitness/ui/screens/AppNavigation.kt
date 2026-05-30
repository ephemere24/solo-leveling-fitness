package com.sololeveling.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sololeveling.fitness.viewmodel.GameViewModel
import com.sololeveling.fitness.ui.theme.*

@Composable
fun ErrorScreen(error: String, onRetry: () -> Unit) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(24.dp)
            .verticalScroll(scroll),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = AccentRed, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("FATAL ERROR", style = MaterialTheme.typography.headlineLarge, color = AccentRed)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Copia esto y envíamelo:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgCard)
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = AccentOrange,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
        ) {
            Text("REINTENTAR", color = BgPrimary)
        }
    }
}

@Composable
fun AppRootScreen() {
    val viewModel: GameViewModel = viewModel()
    val userProfile by viewModel.userProfile.collectAsState()
    val dailyMissions by viewModel.dailyMissions.collectAsState()
    val streakMultiplier by viewModel.streakMultiplier.collectAsState()
    val globalRanking by viewModel.globalRanking.collectAsState()
    val friendsRanking by viewModel.friendsRanking.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val showWelcome by viewModel.showWelcome.collectAsState()

    if (showWelcome) {
        WelcomeScreen(onStart = { name -> viewModel.createProfile(name) })
        return
    }

    SlfNavigation(
        userProfile = userProfile,
        dailyMissions = dailyMissions,
        streakMultiplier = streakMultiplier,
        globalRanking = globalRanking,
        friendsRanking = friendsRanking,
        friends = friends,
        achievements = achievements,
        viewModel = viewModel,
    )
}

sealed class NavScreen(val route: String, val label: String) {
    data object Home : NavScreen("home", "INICIO")
    data object Ranking : NavScreen("ranking", "RANKING")
    data object Friends : NavScreen("friends", "AMIGOS")
    data object Profile : NavScreen("profile", "PERFIL")
    data object MissionDetail : NavScreen("mission/{missionId}", "MISION") {
        fun createRoute(id: String) = "mission/$id"
    }
}

@Composable
fun SlfNavigation(
    userProfile: com.sololeveling.fitness.data.model.UserProfile,
    dailyMissions: List<com.sololeveling.fitness.data.model.Mission>,
    streakMultiplier: Double,
    globalRanking: List<com.sololeveling.fitness.data.model.RankingEntry>,
    friendsRanking: List<com.sololeveling.fitness.data.model.RankingEntry>,
    friends: List<com.sololeveling.fitness.data.model.Friendship>,
    achievements: List<com.sololeveling.fitness.data.model.Achievement>,
    viewModel: GameViewModel
) {
    val navController = rememberNavController()
    val bottomItems = listOf(NavScreen.Home, NavScreen.Ranking, NavScreen.Friends, NavScreen.Profile)
    val labels = listOf("INICIO", "RANK", "AMIGOS", "PERFIL")

    Scaffold(
        containerColor = BgPrimary,
        bottomBar = {
            NavigationBar(
                containerColor = BgSecondary,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = AccentCyan.copy(alpha = 0.15f)
                )
            ) {
                val current by navController.currentBackStackEntryAsState()
                val route = current?.destination?.route
                bottomItems.forEachIndexed { i, screen ->
                    NavigationBarItem(
                        icon = { Text(labels[i].take(1), fontSize = 18.sp, color = if (route == screen.route) AccentCyan else TextTertiary) },
                        label = { Text(labels[i], style = MaterialTheme.typography.labelSmall, color = if (route == screen.route) AccentCyan else TextTertiary) },
                        selected = route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentCyan,
                            selectedTextColor = AccentCyan,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = AccentCyan.copy(alpha = 0.08f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = NavScreen.Home.route, modifier = Modifier.padding(padding)) {
            composable(NavScreen.Home.route) {
                HomeScreen(
                    userProfile = userProfile,
                    dailyMissions = dailyMissions,
                    streakMultiplier = streakMultiplier,
                    onMissionClick = { mission -> navController.navigate(NavScreen.MissionDetail.createRoute(mission.id)) }
                )
            }
            composable(NavScreen.Ranking.route) {
                RankingScreen(globalRanking, friendsRanking, userProfile.id)
            }
            composable(NavScreen.Friends.route) {
                FriendsScreen(
                    friends = friends,
                    myFriendCode = userProfile.friendCode,
                    onAddFriend = { viewModel.addFriend(it) },
                    onChallenge = {}
                )
            }
            composable(NavScreen.Profile.route) {
                ProfileScreen(userProfile, achievements, {})
            }
            composable(NavScreen.MissionDetail.route) { backStack ->
                val mid = backStack.arguments?.getString("missionId") ?: return@composable
                val mission = dailyMissions.find { it.id == mid } ?: return@composable
                MissionDetailScreen(
                    mission = mission,
                    streakMultiplier = streakMultiplier,
                    onComplete = { m -> viewModel.completeMission(m) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
