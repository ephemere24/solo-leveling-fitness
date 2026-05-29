package com.sololeveling.fitness.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.fitness.data.model.*
import com.sololeveling.fitness.data.repository.GameRepository
import com.sololeveling.fitness.util.GameEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel principal — gestión de estado del juego.
 * Todo local con DataStore, sin dependencias de red.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _dailyMissions = MutableStateFlow<List<Mission>>(emptyList())
    val dailyMissions: StateFlow<List<Mission>> = _dailyMissions.asStateFlow()

    // Ranking local (mock data — con Firebase vendría del server)
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

    private val _lastResult = MutableStateFlow<GameEngine.MissionResult?>(null)
    val lastResult: StateFlow<GameEngine.MissionResult?> = _lastResult.asStateFlow()

    private val _showWelcome = MutableStateFlow(true)
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

    // Notificación de evento (snackbar/eventos UI)
    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage: SharedFlow<String> = _eventMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            _userProfile.value = profile

            if (profile.id.isNotBlank()) {
                _showWelcome.value = false
                checkDaily()
                loadData()
            }
            _isLoading.value = false
        }
    }

    fun createProfile(name: String) {
        viewModelScope.launch {
            val profile = repository.initializeUser(name)
            _userProfile.value = profile
            _showWelcome.value = false
            generateDailyMissions()
            loadData()
        }
    }

    fun checkDaily() {
        viewModelScope.launch {
            val streakResult = repository.checkDailyStreak()
            _streakMultiplier.value = streakResult.multiplier
            val profile = repository.getUserProfile()
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

    private fun generateDailyMissions() {
        val profile = _userProfile.value
        _dailyMissions.value = GameEngine.generateDailyMissions(profile.level, profile.stats)
    }

    fun completeMission(mission: Mission) {
        viewModelScope.launch {
            val result = repository.completeMission(mission)
            _lastResult.value = result

            val profile = repository.getUserProfile()
            _userProfile.value = profile

            _dailyMissions.value = _dailyMissions.value.map {
                if (it.id == mission.id) mission else it
            }

            updateAchievements()

            if (result.levelsGained > 0) {
                _eventMessage.emit("🎉 ¡Has subido al Nivel ${result.newLevel}!")
            }

            loadData()
        }
    }

    private fun updateAchievements() {
        val profile = _userProfile.value
        _achievements.value = GameEngine.checkAchievements(
            profile,
            friendsCount = _friends.value.size
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            updateAchievements()
            generateMockRanking()
        }
    }

    // ═══ MOCK RANKING — hasta conectar Firebase ═══
    private fun generateMockRanking() {
        val profile = _userProfile.value
        val mockUsers = listOf(
            RankingEntry("mock1", "Kratos", 87, 245000, HunterRank.forLevel(87), 1250),
            RankingEntry("mock2", "Sung Jin-Woo", 76, 198000, HunterRank.forLevel(76), 980),
            RankingEntry("mock3", "Guts", 65, 156000, HunterRank.forLevel(65), 756),
            RankingEntry("mock4", "Naruto", 54, 120000, HunterRank.forLevel(54), 540),
            RankingEntry("mock5", "Goku", 48, 98000, HunterRank.forLevel(48), 420),
            RankingEntry("mock6", "Eren", 42, 85000, HunterRank.forLevel(42), 350),
            RankingEntry("mock7", "Levi", 38, 72000, HunterRank.forLevel(38), 290),
            RankingEntry("mock8", "Kaneki", 30, 55000, HunterRank.forLevel(30), 200),
            RankingEntry("mock9", "Saitama", 25, 42000, HunterRank.forLevel(25), 150),
            RankingEntry("mock10", "Deku", 18, 28000, HunterRank.forLevel(18), 95),
        )

        val me = RankingEntry(
            userId = profile.id,
            name = profile.name,
            level = profile.level,
            totalXP = profile.totalXP,
            rank = profile.rank,
            missionsCompleted = profile.missionsCompleted,
            currentStreak = profile.currentStreak
        )

        val all = (mockUsers + me).sortedByDescending { it.totalXP }
        _globalRanking.value = all
        _friendsRanking.value = listOf(me) // Solo yo de momento
    }

    // ═══ AMIGOS (mock por ahora) ═══
    fun addFriend(code: String) {
        viewModelScope.launch {
            if (code.isBlank()) {
                _eventMessage.emit("⚠️ Introduce un código válido")
                return@launch
            }

            // Mock: siempre "encuentra" un amigo aleatorio
            val mockNames = listOf("Kratos", "Jin-Woo", "Guts", "Naruto", "Goku", "Saitama")
            val mockName = mockNames.random()
            val friend = Friendship(
                userId = _userProfile.value.id,
                friendId = "friend_${code.take(4)}",
                friendName = mockName,
                friendLevel = (5..80).random()
            )
            _friends.value = _friends.value + friend

            updateAchievements()
            generateMockRanking()
            _eventMessage.emit("✅ ¡$mockName añadido como amigo!")
        }
    }

    fun clearLastResult() {
        _lastResult.value = null
    }
}
