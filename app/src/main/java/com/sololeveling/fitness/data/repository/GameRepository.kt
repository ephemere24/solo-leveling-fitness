package com.sololeveling.fitness.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sololeveling.fitness.data.model.*
import com.sololeveling.fitness.util.GameEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "slf_prefs")

/**
 * Repositorio del juego — 100% local con DataStore (offline-first).
 * Persistente entre sesiones, no requiere Firebase.
 */
class GameRepository(private val context: Context) {

    private object PrefsKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val LEVEL = intPreferencesKey("level")
        val TOTAL_XP = longPreferencesKey("total_xp")
        val CURRENT_XP = longPreferencesKey("current_xp")
        val MISSIONS_COMPLETED = intPreferencesKey("missions_completed")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
        val DAYS_ACTIVE = intPreferencesKey("days_active")
        val LAST_ACTIVE = longPreferencesKey("last_active")
        val STREAK_PROTECTION = intPreferencesKey("streak_protection")
        val FRIEND_CODE = stringPreferencesKey("friend_code")

        val STAT_STRENGTH = intPreferencesKey("stat_strength")
        val STAT_SPEED = intPreferencesKey("stat_speed")
        val STAT_ENDURANCE = intPreferencesKey("stat_endurance")
        val STAT_STAMINA = intPreferencesKey("stat_stamina")
        val STAT_FLEXIBILITY = intPreferencesKey("stat_flexibility")

        val MISSIONS_DATE = longPreferencesKey("missions_date")

        // Friends local (stored as JSON strings)
        val FRIENDS_COUNT = intPreferencesKey("friends_count")
        val CHALLENGE_WINS = intPreferencesKey("challenge_wins")
    }

    // ══════════════════════════════════════════
    //  PERFIL LOCAL
    // ══════════════════════════════════════════

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            id = prefs[PrefsKeys.USER_ID] ?: "",
            name = prefs[PrefsKeys.USER_NAME] ?: "Cazador",
            friendCode = prefs[PrefsKeys.FRIEND_CODE] ?: "",
            level = prefs[PrefsKeys.LEVEL] ?: 1,
            totalXP = prefs[PrefsKeys.TOTAL_XP]?.toInt() ?: 0,
            xp = prefs[PrefsKeys.CURRENT_XP]?.toInt() ?: 0,
            stats = PlayerStats(
                strength = prefs[PrefsKeys.STAT_STRENGTH] ?: 1,
                speed = prefs[PrefsKeys.STAT_SPEED] ?: 1,
                endurance = prefs[PrefsKeys.STAT_ENDURANCE] ?: 1,
                stamina = prefs[PrefsKeys.STAT_STAMINA] ?: 1,
                flexibility = prefs[PrefsKeys.STAT_FLEXIBILITY] ?: 1,
            ),
            missionsCompleted = prefs[PrefsKeys.MISSIONS_COMPLETED] ?: 0,
            daysActive = prefs[PrefsKeys.DAYS_ACTIVE] ?: 0,
            currentStreak = prefs[PrefsKeys.CURRENT_STREAK] ?: 0,
            longestStreak = prefs[PrefsKeys.LONGEST_STREAK] ?: 0,
            lastActiveDate = prefs[PrefsKeys.LAST_ACTIVE] ?: 0L,
            streakProtection = prefs[PrefsKeys.STREAK_PROTECTION] ?: 1,
        )
    }

    suspend fun getUserProfile(): UserProfile = userProfileFlow.first()

    // ══════════════════════════════════════════
    //  INICIALIZACIÓN
    // ══════════════════════════════════════════

    suspend fun initializeUser(name: String, userId: String? = null): UserProfile {
        val id = userId ?: java.util.UUID.randomUUID().toString()
        val friendCode = GameEngine.generateFriendCode()
        val now = System.currentTimeMillis()

        updatePrefs { prefs ->
            prefs[PrefsKeys.USER_ID] = id
            prefs[PrefsKeys.USER_NAME] = name
            prefs[PrefsKeys.FRIEND_CODE] = friendCode
            prefs[PrefsKeys.LAST_ACTIVE] = now
            prefs[PrefsKeys.STREAK_PROTECTION] = 1
        }

        return getUserProfile()
    }

    // ══════════════════════════════════════════
    //  CHECK DIARIO
    // ══════════════════════════════════════════

    suspend fun checkDailyStreak(): StreakResult {
        val profile = getUserProfile()
        val now = System.currentTimeMillis()
        val lastActive = profile.lastActiveDate

        if (lastActive == 0L) return StreakResult(profile.currentStreak, 1.0, false)

        val daysSinceActive = TimeUnit.MILLISECONDS.toDays(now - lastActive).toInt()

        val newProfile = if (daysSinceActive <= 1) {
            if (daysSinceActive == 1) {
                val newStreak = profile.currentStreak + 1
                val longest = maxOf(profile.longestStreak, newStreak)
                updatePrefs {
                    it[PrefsKeys.CURRENT_STREAK] = newStreak
                    it[PrefsKeys.LONGEST_STREAK] = longest
                    it[PrefsKeys.LAST_ACTIVE] = now
                    it[PrefsKeys.DAYS_ACTIVE] = profile.daysActive + 1
                }
                profile.copy(
                    currentStreak = newStreak,
                    longestStreak = longest,
                    lastActiveDate = now,
                    daysActive = profile.daysActive + 1
                )
            } else {
                profile
            }
        } else {
            val (penalty, protection, newStreak) = GameEngine.calculateInactivityPenalty(
                daysSinceActive, profile.currentStreak, profile.streakProtection
            )
            updatePrefs {
                it[PrefsKeys.CURRENT_STREAK] = newStreak
                it[PrefsKeys.STREAK_PROTECTION] = protection
                it[PrefsKeys.LAST_ACTIVE] = now
            }
            profile.copy(
                currentStreak = newStreak,
                streakProtection = protection,
                lastActiveDate = now
            )
        }

        return StreakResult(
            streak = newProfile.currentStreak,
            multiplier = GameEngine.streakMultiplier(newProfile.currentStreak),
            wasReset = daysSinceActive > 2
        )
    }

    data class StreakResult(
        val streak: Int,
        val multiplier: Double,
        val wasReset: Boolean
    )

    // ══════════════════════════════════════════
    //  COMPLETAR MISIÓN
    // ══════════════════════════════════════════

    suspend fun completeMission(mission: Mission): GameEngine.MissionResult {
        val profile = getUserProfile()
        val result = GameEngine.completeMission(mission, profile)
        val newStats = GameEngine.updateStatsOnMission(profile.stats, mission)

        updatePrefs {
            val newTotalXP = profile.totalXP + result.earnedXP
            it[PrefsKeys.TOTAL_XP] = newTotalXP.toLong()
            it[PrefsKeys.LEVEL] = result.newLevel
            it[PrefsKeys.MISSIONS_COMPLETED] = profile.missionsCompleted + 1
            it[PrefsKeys.CURRENT_XP] =
                GameEngine.xpInCurrentLevel(newTotalXP, result.newLevel).toLong()
            it[PrefsKeys.STAT_STRENGTH] = newStats.strength
            it[PrefsKeys.STAT_SPEED] = newStats.speed
            it[PrefsKeys.STAT_ENDURANCE] = newStats.endurance
            it[PrefsKeys.STAT_STAMINA] = newStats.stamina
            it[PrefsKeys.STAT_FLEXIBILITY] = newStats.flexibility
        }

        return result
    }

    // ══════════════════════════════════════════
    //  STATS EXTRA: Challenge wins
    // ══════════════════════════════════════════

    suspend fun incrementChallengeWins() {
        val profile = getUserProfile()
        // Could store wins count, for now just tracking achievement
    }

    // ══════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════

    private suspend fun updatePrefs(update: (MutablePreferences) -> Unit) {
        context.dataStore.edit { prefs -> update(prefs) }
    }
}
