package com.sololeveling.fitness.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.fitness.SoloLevelingApp
import com.sololeveling.fitness.data.model.*
import com.sololeveling.fitness.data.repository.GameRepository
import com.sololeveling.fitness.data.repository.FirestoreRepository
import com.sololeveling.fitness.util.GameEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val localRepo = GameRepository(application)
    private val cloudRepo = FirestoreRepository()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _dailyMissions = MutableStateFlow<List<Mission>>(emptyList())
    val dailyMissions: StateFlow<List<Mission>> = _dailyMissions.asStateFlow()

    private val _globalRanking = MutableStateFlow<List<RankingEntry>>(emptyList())
    val globalRanking: StateFlow<List<RankingEntry>> = _globalRanking.asStateFlow()

    private val _friendsRanking = MutableStateFlow<List<RankingEntry>>(emptyList())
    val friendsRanking: StateFlow<List<RankingEntry>> = _friendsRanking.asStateFlow()

    private val _friends = MutableStateFlow<List<Friendship>>(emptyList())
    val friends: StateFlow<List<Friendship>> = _friends.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _streakMultiplier = MutableStateFlow(1.0)
    val streakMultiplier: StateFlow<Double> = _streakMultiplier.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showWelcome = MutableStateFlow(true)
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage: SharedFlow<String> = _eventMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            try {
                // 1. Sign in anonymously
                if (!cloudRepo.isSignedIn()) {
                    cloudRepo.signInAnonymously()
                }

                // 2. Load local profile
                val profile = localRepo.getUserProfile()
                _userProfile.value = profile

                if (profile.id.isNotBlank()) {
                    _showWelcome.value = false
                    // 3. Sync with cloud
                    syncWithCloud(profile)
                }
            } catch (e: Exception) {
                // Firebase might not be fully set up yet, work offline
                val profile = localRepo.getUserProfile()
                _userProfile.value = profile
                if (profile.id.isNotBlank()) _showWelcome.value = false
            }
            _isLoading.value = false

            // 4. Start listening to cloud data
            listenToCloudData()

            // 5. Load daily missions
            if (!_showWelcome.value) {
                checkDaily()
            }
        }
    }

    private suspend fun syncWithCloud(profile: UserProfile) {
        val userId = SoloLevelingApp.auth.currentUser?.uid ?: return
        val cloudData = cloudRepo.getPlayerProfile(userId)

        if (cloudData != null) {
            // Cloud profile exists — merge (use whichever has more XP)
            val cloudTotalXP = (cloudData["totalXP"] as? Long)?.toInt() ?: 0
            if (cloudTotalXP > profile.totalXP) {
                // Update local from cloud
                updateLocalFromCloud(cloudData)
            } else {
                // Push local to cloud
                pushLocalToCloud(profile)
            }
        } else {
            // No cloud profile — push local
            pushLocalToCloud(profile)
        }
    }

    private suspend fun pushLocalToCloud(profile: UserProfile) {
        val userId = SoloLevelingApp.auth.currentUser?.uid ?: return
        try {
            cloudRepo.updatePlayerProfile(userId, mapOf(
                "name" to profile.name,
                "level" to profile.level,
                "totalXP" to profile.totalXP,
                "currentXP" to profile.xp,
                "friendCode" to profile.friendCode,
                "missionsCompleted" to profile.missionsCompleted,
                "currentStreak" to profile.currentStreak,
                "longestStreak" to profile.longestStreak,
                "stats" to mapOf(
                    "strength" to profile.stats.strength,
                    "speed" to profile.stats.speed,
                    "endurance" to profile.stats.endurance,
                    "stamina" to profile.stats.stamina,
                    "flexibility" to profile.stats.flexibility
                )
            ))
        } catch (_: Exception) { /* offline — will sync later */ }
    }

    private suspend fun updateLocalFromCloud(data: Map<String, Any>) {
        // Sync cloud data to local DataStore
        val stats = data["stats"] as? Map<*, *>
        val profile = UserProfile(
            id = data["id"] as? String ?: "",
            name = data["name"] as? String ?: "Cazador",
            friendCode = data["friendCode"] as? String ?: "",
            level = (data["level"] as? Long)?.toInt() ?: 1,
            xp = (data["currentXP"] as? Long)?.toInt() ?: 0,
            totalXP = (data["totalXP"] as? Long)?.toInt() ?: 0,
            stats = PlayerStats(
                strength = (stats?.get("strength") as? Long)?.toInt() ?: 1,
                speed = (stats?.get("speed") as? Long)?.toInt() ?: 1,
                endurance = (stats?.get("endurance") as? Long)?.toInt() ?: 1,
                stamina = (stats?.get("stamina") as? Long)?.toInt() ?: 1,
                flexibility = (stats?.get("flexibility") as? Long)?.toInt() ?: 1,
            ),
            missionsCompleted = (data["missionsCompleted"] as? Long)?.toInt() ?: 0,
            currentStreak = (data["currentStreak"] as? Long)?.toInt() ?: 0,
            longestStreak = (data["longestStreak"] as? Long)?.toInt() ?: 0,
        )
        _userProfile.value = profile
    }

    private fun listenToCloudData() {
        // Global ranking — top 3 legends + real players from Firestore
        viewModelScope.launch {
            try {
                cloudRepo.globalRankingFlow(100).collect { cloudRanking ->
                    val me = _userProfile.value
                    val meEntry = RankingEntry(me.id, me.name, me.level, me.totalXP, me.rank, me.missionsCompleted, me.currentStreak)

                    val legends = listOf(
                        RankingEntry("legend1", "Goku", 99, 500000, HunterRank.forLevel(99), 9999),
                        RankingEntry("legend2", "Sung Jin-Woo", 97, 450000, HunterRank.forLevel(97), 8888),
                        RankingEntry("legend3", "Kratos", 95, 400000, HunterRank.forLevel(95), 7777),
                    )

                    // Merge: legends first, then cloud players (excluding me), then me
                    val others = cloudRanking.filter { it.userId != me.id }
                    _globalRanking.value = (legends + others + meEntry).sortedByDescending { it.totalXP }
                }
            } catch (_: Exception) {
                generateMockRanking()
            }
        }

        // Friends — real-time from Firestore
        viewModelScope.launch {
            val userId = SoloLevelingApp.auth.currentUser?.uid ?: return@launch
            try {
                cloudRepo.myFriendsFlow(userId).collect { friendList ->
                    _friends.value = friendList
                    // Generate friends ranking
                    val friendsRankingEntries = friendList.map { f ->
                        RankingEntry(
                            userId = f.friendId,
                            name = f.friendName,
                            level = f.friendLevel,
                            totalXP = f.friendLevel * 1000,
                            rank = HunterRank.forLevel(f.friendLevel),
                            missionsCompleted = 0
                        )
                    }
                    val me = _userProfile.value
                    val meEntry = RankingEntry(
                        userId = me.id, name = me.name, level = me.level,
                        totalXP = me.totalXP, rank = me.rank,
                        missionsCompleted = me.missionsCompleted
                    )
                    _friendsRanking.value = (friendsRankingEntries + meEntry)
                        .sortedByDescending { it.totalXP }
                }
            } catch (_: Exception) { }
        }
    }

    fun createProfile(name: String) {
        viewModelScope.launch {
            // Check if Firebase is ready
            if (SoloLevelingApp.auth.currentUser == null) {
                try { cloudRepo.signInAnonymously() } catch (_: Exception) { }
            }
            val userId = SoloLevelingApp.auth.currentUser?.uid ?: java.util.UUID.randomUUID().toString()
            val friendCode = GameEngine.generateFriendCode()

            val profile = localRepo.initializeUser(name, userId, friendCode)
            _userProfile.value = profile
            _showWelcome.value = false

            // Push to cloud
            try {
                cloudRepo.createPlayerProfile(userId, name, friendCode)
            } catch (_: Exception) { }

            generateDailyMissions()
            updateAchievements()
            generateMockRanking() // fallback until cloud syncs
            _eventMessage.emit("🎮 ¡Bienvenido, Cazador $name!")
        }
    }

    fun checkDaily() {
        viewModelScope.launch {
            val streakResult = localRepo.checkDailyStreak()
            _streakMultiplier.value = streakResult.multiplier
            val profile = localRepo.getUserProfile()
            _userProfile.value = profile

            if (streakResult.wasReset) {
                _eventMessage.emit("⚠️ Tu racha se ha reiniciado por inactividad")
            }

            val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            val lastDay = if (profile.lastActiveDate > 0) {
                java.util.Calendar.getInstance().apply {
                    timeInMillis = profile.lastActiveDate
                }.get(java.util.Calendar.DAY_OF_YEAR)
            } else 0

            if (today != lastDay || _dailyMissions.value.isEmpty()) {
                generateDailyMissions()
                if (profile.currentStreak > 0) {
                    _eventMessage.emit("🎯 ¡Nuevas misiones disponibles!")
                }
            }
        }
    }

    private suspend fun generateDailyMissions() {
        val profile = _userProfile.value
        val completedToday = localRepo.getCompletedMissionTypesToday()
        _dailyMissions.value = GameEngine.generateDailyMissions(profile.level, profile.stats).map { mission ->
            if (mission.type.name in completedToday) {
                mission.copy(
                    completedCount = mission.targetCount,
                    isCompleted = true,
                    completedDate = System.currentTimeMillis()
                )
            } else {
                mission
            }
        }
    }

    fun completeMission(mission: Mission) {
        if (mission.isCompleted) return // Ya completada, no hacer nada

        viewModelScope.launch {
            val result = localRepo.completeMission(mission)
            val profile = localRepo.getUserProfile()
            _userProfile.value = profile

            // Update the mission in dailyMissions with completed state
            _dailyMissions.value = _dailyMissions.value.map {
                if (it.id == mission.id) {
                    it.copy(
                        completedCount = mission.targetCount,
                        isCompleted = true,
                        completedDate = System.currentTimeMillis()
                    )
                } else it
            }

            updateAchievements()
            pushLocalToCloud(profile)

            if (result.levelsGained > 0) {
                _eventMessage.emit("Subiste al Nivel ${result.newLevel}!")
            } else {
                _eventMessage.emit("Mision completada! +${result.earnedXP} XP")
            }
        }
    }

    private fun updateAchievements() {
        val profile = _userProfile.value
        _achievements.value = GameEngine.checkAchievements(
            profile,
            friendsCount = _friends.value.size
        )
    }

    fun addFriend(code: String) {
        viewModelScope.launch {
            if (code.isBlank()) {
                _eventMessage.emit("⚠️ Introduce un código válido")
                return@launch
            }

            try {
                val userId = SoloLevelingApp.auth.currentUser?.uid ?: run {
                    _eventMessage.emit("⚠️ Error de conexión. Intenta de nuevo.")
                    return@launch
                }

                val success = cloudRepo.addFriend(userId, code)
                if (success) {
                    _eventMessage.emit("✅ ¡Amigo añadido con éxito!")
                } else {
                    _eventMessage.emit("❌ Código no encontrado. Verifícalo e intenta de nuevo.")
                }
            } catch (e: Exception) {
                _eventMessage.emit("❌ Error de conexión. Intenta más tarde.")
            }
        }
    }

    // Fallback mock ranking for offline
    private fun generateMockRanking() {
        val me = _userProfile.value
        val meEntry = RankingEntry(me.id, me.name, me.level, me.totalXP, me.rank, me.missionsCompleted, me.currentStreak)

        // Top 3 legendarios (siempre aparecen por encima)
        val legends = listOf(
            RankingEntry("legend1", "Goku", 99, 500000, HunterRank.forLevel(99), 9999),
            RankingEntry("legend2", "Sung Jin-Woo", 97, 450000, HunterRank.forLevel(97), 8888),
            RankingEntry("legend3", "Kratos", 95, 400000, HunterRank.forLevel(95), 7777),
        )

        val all = (legends + meEntry).sortedByDescending { it.totalXP }
        _globalRanking.value = all
    }
}
