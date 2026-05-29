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
 * ViewModel principal del juego — Toda la gestión de estado aquí
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    // ═══ ESTADO ═══

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

    private val _lastResult = MutableStateFlow<GameEngine.MissionResult?>(null)
    val lastResult: StateFlow<GameEngine.MissionResult?> = _lastResult.asStateFlow()

    private val _showWelcome = MutableStateFlow(true)
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

    // ══════════════════════════════════════════
    //  INICIALIZACIÓN
    // ══════════════════════════════════════════

    init {
        viewModelScope.launch {
            // Cargar perfil local
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

    // ══════════════════════════════════════════
    //  INICIO DE USUARIO
    // ══════════════════════════════════════════

    fun createProfile(name: String) {
        viewModelScope.launch {
            val profile = repository.initializeUser(name)
            _userProfile.value = profile
            _showWelcome.value = false
            generateDailyMissions()
            repository.syncProfileToFirestore(profile)
        }
    }

    // ══════════════════════════════════════════
    //  CHECK DIARIO
    // ══════════════════════════════════════════

    fun checkDaily() {
        viewModelScope.launch {
            val streakResult = repository.checkDailyStreak()
            _streakMultiplier.value = streakResult.multiplier
            val profile = repository.getUserProfile()
            _userProfile.value = profile

            // Verificar si hay que generar misiones nuevas
            val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            val lastDay = if (profile.lastActiveDate > 0) {
                java.util.Calendar.getInstance().apply {
                    timeInMillis = profile.lastActiveDate
                }.get(java.util.Calendar.DAY_OF_YEAR)
            } else 0

            if (today != lastDay || _dailyMissions.value.isEmpty()) {
                generateDailyMissions()
            }
        }
    }

    // ══════════════════════════════════════════
    //  MISIONES
    // ══════════════════════════════════════════

    private fun generateDailyMissions() {
        val profile = _userProfile.value
        val missions = GameEngine.generateDailyMissions(profile.level, profile.stats)
        _dailyMissions.value = missions
    }

    fun completeMission(mission: Mission) {
        viewModelScope.launch {
            val result = repository.completeMission(mission)
            _lastResult.value = result

            // Actualizar perfil
            val profile = repository.getUserProfile()
            _userProfile.value = profile

            // Actualizar misión en lista
            _dailyMissions.value = _dailyMissions.value.map {
                if (it.id == mission.id) mission else it
            }

            // Actualizar logros
            updateAchievements()

            // Sync ranking
            repository.updateRanking(profile)
            repository.syncProfileToFirestore(profile)
        }
    }

    private fun updateAchievements() {
        val profile = _userProfile.value
        _achievements.value = GameEngine.checkAchievements(
            profile,
            friendsCount = _friends.value.size
        )
    }

    // ══════════════════════════════════════════
    //  RANKING
    // ══════════════════════════════════════════

    fun loadRanking() {
        viewModelScope.launch {
            _globalRanking.value = repository.getGlobalRanking(50)
        }
    }

    fun loadFriendsRanking() {
        viewModelScope.launch {
            val profile = _userProfile.value
            val friendsList = repository.getFriends(profile.id)
            _friends.value = friendsList

            if (friendsList.isNotEmpty()) {
                val friendIds = friendsList.map { it.friendId }
                _friendsRanking.value = repository.getFriendsRanking(friendIds, profile.id)
            }
        }
    }

    // ══════════════════════════════════════════
    //  AMIGOS
    // ══════════════════════════════════════════

    fun addFriend(code: String) {
        viewModelScope.launch {
            val profile = _userProfile.value
            val result = repository.addFriendByCode(code, profile)
            result.onSuccess {
                loadFriendsRanking()
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            loadRanking()
            loadFriendsRanking()
            updateAchievements()
        }
    }

    fun clearLastResult() {
        _lastResult.value = null
    }
}
