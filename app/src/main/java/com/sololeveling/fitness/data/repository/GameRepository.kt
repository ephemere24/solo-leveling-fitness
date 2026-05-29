package com.sololeveling.fitness.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sololeveling.fitness.data.model.*
import com.sololeveling.fitness.util.GameEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "slf_prefs")

/**
 * Repositorio principal del juego.
 * Combina DataStore local (offline-first) + Firestore (sync, ranking, amigos).
 */
class GameRepository(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()

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

        // Stats
        val STAT_STRENGTH = intPreferencesKey("stat_strength")
        val STAT_SPEED = intPreferencesKey("stat_speed")
        val STAT_ENDURANCE = intPreferencesKey("stat_endurance")
        val STAT_STAMINA = intPreferencesKey("stat_stamina")
        val STAT_FLEXIBILITY = intPreferencesKey("stat_flexibility")

        // Daily missions
        val MISSIONS_DATE = longPreferencesKey("missions_date")
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
    //  INICIALIZACIÓN DE USUARIO
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
    //  CHECK DIARIO — Racha y penalización
    // ══════════════════════════════════════════

    suspend fun checkDailyStreak(): StreakResult {
        val profile = getUserProfile()
        val now = System.currentTimeMillis()
        val lastActive = profile.lastActiveDate

        if (lastActive == 0L) return StreakResult(profile.currentStreak, 1.0, false)

        val daysSinceActive = TimeUnit.MILLISECONDS.toDays(now - lastActive).toInt()

        val newProfile = if (daysSinceActive <= 1) {
            // Día consecutivo o mismo día
            if (daysSinceActive == 1) {
                val newStreak = profile.currentStreak + 1
                val longest = maxOf(profile.longestStreak, newStreak)
                updatePrefs {
                    it[PrefsKeys.CURRENT_STREAK] = newStreak
                    it[PrefsKeys.LONGEST_STREAK] = longest
                    it[PrefsKeys.LAST_ACTIVE] = now
                }
                profile.copy(
                    currentStreak = newStreak,
                    longestStreak = longest,
                    lastActiveDate = now
                )
            } else {
                profile
            }
        } else {
            // Penalización
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

        // Actualizar perfil
        updatePrefs {
            val newTotalXP = profile.totalXP + result.earnedXP
            it[PrefsKeys.TOTAL_XP] = newTotalXP.toLong()
            it[PrefsKeys.LEVEL] = result.newLevel
            it[PrefsKeys.MISSIONS_COMPLETED] = profile.missionsCompleted + 1
            it[PrefsKeys.CURRENT_XP] = (newTotalXP -
                GameEngine.xpForLevel(result.newLevel - 1)).toLong()
            // Stats
            it[PrefsKeys.STAT_STRENGTH] = newStats.strength
            it[PrefsKeys.STAT_SPEED] = newStats.speed
            it[PrefsKeys.STAT_ENDURANCE] = newStats.endurance
            it[PrefsKeys.STAT_STAMINA] = newStats.stamina
            it[PrefsKeys.STAT_FLEXIBILITY] = newStats.flexibility
        }

        return result
    }

    private suspend fun updatePrefs(update: (MutablePreferences) -> Unit) {
        context.dataStore.edit { prefs -> update(prefs) }
    }

    // ══════════════════════════════════════════
    //  RANKING (Firestore)
    // ══════════════════════════════════════════

    suspend fun updateRanking(profile: UserProfile) {
        try {
            val data = hashMapOf(
                "name" to profile.name,
                "level" to profile.level,
                "totalXP" to profile.totalXP,
                "missionsCompleted" to profile.missionsCompleted,
                "currentStreak" to profile.currentStreak,
                "lastUpdated" to System.currentTimeMillis()
            )
            db.collection("rankings").document(profile.id).set(data).await()
        } catch (_: Exception) {
            // Offline — se sincronizará después
        }
    }

    suspend fun getGlobalRanking(limit: Int = 50): List<RankingEntry> {
        return try {
            val snapshot = db.collection("rankings")
                .orderBy("totalXP", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapIndexed { _, doc ->
                RankingEntry(
                    userId = doc.id,
                    name = doc.getString("name") ?: "???",
                    level = (doc.getLong("level") ?: 1),
                    totalXP = (doc.getLong("totalXP") ?: 0).toInt(),
                    rank = HunterRank.forLevel((doc.getLong("level") ?: 1).toInt()),
                    missionsCompleted = (doc.getLong("missionsCompleted") ?: 0).toInt(),
                    currentStreak = (doc.getLong("currentStreak") ?: 0).toInt()
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ══════════════════════════════════════════
    //  AMIGOS (Firestore)
    // ══════════════════════════════════════════

    suspend fun addFriendByCode(code: String, myProfile: UserProfile): Result<String> {
        return try {
            val friendQuery = db.collection("profiles")
                .whereEqualTo("friendCode", code.uppercase().trim())
                .get()
                .await()

            if (friendQuery.documents.isEmpty()) {
                return Result.failure(Exception("Código no encontrado"))
            }

            val friendDoc = friendQuery.documents.first()
            val friendId = friendDoc.id
            val friendName = friendDoc.getString("name") ?: "???"
            val friendLevel = (friendDoc.getLong("level") ?: 1).toInt()

            if (friendId == myProfile.id) {
                return Result.failure(Exception("No puedes añadirte a ti mismo"))
            }

            // Añadir relación bidireccional
            val friendshipId = "${myProfile.id}_$friendId"

            db.collection("profiles").document(myProfile.id)
                .collection("friends")
                .document(friendId)
                .set(mapOf(
                    "friendId" to friendId,
                    "friendName" to friendName,
                    "friendLevel" to friendLevel,
                    "friendCode" to code.uppercase().trim(),
                    "addedAt" to System.currentTimeMillis()
                )).await()

            db.collection("profiles").document(friendId)
                .collection("friends")
                .document(myProfile.id)
                .set(mapOf(
                    "friendId" to myProfile.id,
                    "friendName" to myProfile.name,
                    "friendLevel" to myProfile.level,
                    "friendCode" to myProfile.friendCode,
                    "addedAt" to System.currentTimeMillis()
                )).await()

            Result.success(friendName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriends(userId: String): List<Friendship> {
        return try {
            val snapshot = db.collection("profiles")
                .document(userId)
                .collection("friends")
                .get()
                .await()

            snapshot.documents.map { doc ->
                Friendship(
                    id = doc.id,
                    userId = userId,
                    friendId = doc.getString("friendId") ?: "",
                    friendName = doc.getString("friendName") ?: "???",
                    friendLevel = (doc.getLong("friendLevel") ?: 1).toInt(),
                    createdAt = doc.getLong("addedAt") ?: 0L
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getFriendsRanking(friendIds: List<String>, myId: String): List<RankingEntry> {
        val allIds = friendIds + myId
        return try {
            val entries = mutableListOf<RankingEntry>()
            // Firestore whereIn max 10, por si acaso recorremos
            allIds.chunked(10).forEach { chunk ->
                val snapshot = db.collection("rankings")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                    .get()
                    .await()

                snapshot.documents.forEach { doc ->
                    entries.add(
                        RankingEntry(
                            userId = doc.id,
                            name = doc.getString("name") ?: "???",
                            level = (doc.getLong("level") ?: 1),
                            totalXP = (doc.getLong("totalXP") ?: 0).toInt(),
                            rank = HunterRank.forLevel((doc.getLong("level") ?: 1).toInt()),
                            missionsCompleted = (doc.getLong("missionsCompleted") ?: 0).toInt(),
                            currentStreak = (doc.getLong("currentStreak") ?: 0).toInt()
                        )
                    )
                }
            }
            entries.sortedByDescending { it.totalXP }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ══════════════════════════════════════════
    //  SYNC PERFIL -> FIRESTORE
    // ══════════════════════════════════════════

    suspend fun syncProfileToFirestore(profile: UserProfile) {
        try {
            val data = hashMapOf(
                "name" to profile.name,
                "level" to profile.level,
                "totalXP" to profile.totalXP,
                "friendCode" to profile.friendCode,
                "missionsCompleted" to profile.missionsCompleted,
                "currentStreak" to profile.currentStreak,
                "stats" to mapOf(
                    "strength" to profile.stats.strength,
                    "speed" to profile.stats.speed,
                    "endurance" to profile.stats.endurance,
                    "stamina" to profile.stats.stamina,
                    "flexibility" to profile.stats.flexibility
                ),
                "lastActive" to System.currentTimeMillis()
            )
            db.collection("profiles").document(profile.id).set(data).await()
        } catch (_: Exception) { /* offline */ }
    }
}
