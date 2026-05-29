package com.sololeveling.fitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sololeveling.fitness.ui.screens.*
import com.sololeveling.fitness.ui.theme.SoloLevelingTheme
import com.sololeveling.fitness.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoloLevelingTheme {
                SoloLevelingApp()
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    data object Home : Screen("home", Icons.Default.Home, "Inicio")
    data object Missions : Screen("missions", Icons.Default.FitnessCenter, "Misiones")
    data object Ranking : Screen("ranking", Icons.Default.EmojiEvents, "Ranking")
    data object Friends : Screen("friends", Icons.Default.Group, "Amigos")
    data object Profile : Screen("profile", Icons.Default.Person, "Perfil")
    data object MissionDetail : Screen("mission/{missionId}", Icons.Default.FitnessCenter, "Misión") {
        fun createRoute(missionId: String) = "mission/$missionId"
    }
}

@Composable
fun SoloLevelingApp() {
    val viewModel: GameViewModel = viewModel()
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showWelcome by viewModel.showWelcome.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            // Splash simple
            androidx.compose.material3.CircularProgressIndicator(
                color = com.sololeveling.fitness.ui.theme.AccentCyan
            )
        }
        return
    }

    if (showWelcome) {
        WelcomeScreen(
            onStart = { name -> viewModel.createProfile(name) }
        )
        return
    }

    MainScreen(viewModel)
}

@Composable
fun MainScreen(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val userProfile by viewModel.userProfile.collectAsState()
    val dailyMissions by viewModel.dailyMissions.collectAsState()
    val streakMultiplier by viewModel.streakMultiplier.collectAsState()
    val globalRanking by viewModel.globalRanking.collectAsState()
    val friendsRanking by viewModel.friendsRanking.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val bottomNavItems = listOf(Screen.Home, Screen.Ranking, Screen.Friends, Screen.Profile)

    // Barra inferior personalizada
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = com.sololeveling.fitness.ui.theme.BgSecondary,
                contentColor = com.sololeveling.fitness.ui.theme.TextPrimary
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.sololeveling.fitness.ui.theme.AccentCyan,
                            selectedTextColor = com.sololeveling.fitness.ui.theme.AccentCyan,
                            unselectedIconColor = com.sololeveling.fitness.ui.theme.TextTertiary,
                            unselectedTextColor = com.sololeveling.fitness.ui.theme.TextTertiary,
                            indicatorColor = com.sololeveling.fitness.ui.theme.AccentCyan.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    userProfile = userProfile,
                    dailyMissions = dailyMissions,
                    streakMultiplier = streakMultiplier,
                    onMissionClick = { mission ->
                        navController.navigate(Screen.MissionDetail.createRoute(mission.id))
                    }
                )
            }
            composable(Screen.Ranking.route) {
                RankingScreen(
                    globalRanking = globalRanking,
                    friendsRanking = friendsRanking,
                    myUserId = userProfile.id
                )
            }
            composable(Screen.Friends.route) {
                FriendsScreen(
                    friends = friends,
                    myFriendCode = userProfile.friendCode,
                    onAddFriend = { code -> viewModel.addFriend(code) },
                    onChallenge = { /* TODO: implementar retos */ }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    userProfile = userProfile,
                    achievements = achievements,
                    onLogout = { /* TODO: logout */ }
                )
            }
            composable(Screen.MissionDetail.route) { backStackEntry ->
                val missionId = backStackEntry.arguments?.getString("missionId") ?: return@composable
                val mission = dailyMissions.find { it.id == missionId } ?: return@composable

                MissionDetailScreen(
                    mission = mission,
                    streakMultiplier = streakMultiplier,
                    onComplete = { completedMission ->
                        viewModel.completeMission(completedMission)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private val Box: @Composable (modifier: Modifier) -> Unit get() = { modifier ->
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize())
}
