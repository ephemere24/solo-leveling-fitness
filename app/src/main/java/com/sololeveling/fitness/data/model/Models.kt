package com.sololeveling.fitness.data.model

import com.sololeveling.fitness.util.GameEngine
import com.sololeveling.fitness.util.GameEngine.xpForLevelFast

import java.util.UUID

/**
 * Tipos de ejercicio / misión disponibles
 */
enum class ExerciseType(val displayName: String, val statCategory: StatCategory) {
    PUSHUPS("Flexiones", StatCategory.STRENGTH),
    SQUATS("Sentadillas", StatCategory.STRENGTH),
    PULLUPS("Dominadas", StatCategory.STRENGTH),
    ABDOMINALS("Abdominales", StatCategory.STAMINA),
    PLANK("Plancha", StatCategory.STAMINA),
    BURPEES("Burpees", StatCategory.ENDURANCE),
    RUNNING("Carrera", StatCategory.ENDURANCE),
    MOUNTAIN_CLIMBERS("Escaladores", StatCategory.SPEED),
    JUMPING_JACKS("Jumping Jacks", StatCategory.SPEED),
    STRETCHING("Estiramientos", StatCategory.FLEXIBILITY);

    companion object {
        fun dailyMissionTypes(): List<ExerciseType> = listOf(
            PUSHUPS, SQUATS, PULLUPS, ABDOMINALS, PLANK
        )
    }
}

/**
 * Categorías de estadísticas que van subiendo
 */
enum class StatCategory(val displayName: String, val icon: String) {
    STRENGTH("Fuerza", "💪"),
    SPEED("Velocidad", "⚡"),
    ENDURANCE("Resistencia", "🏃"),
    STAMINA("Aguante", "🔥"),
    FLEXIBILITY("Flexibilidad", "🧘");
}

/**
 * Niveles de rango estilo Solo Leveling
 */
enum class HunterRank(
    val displayName: String,
    val minLevel: Int,
    val colorHex: String
) {
    E_RANK("E-Rank", 1, "#6B7280"),
    D_RANK("D-Rank", 10, "#3B82F6"),
    C_RANK("C-Rank", 20, "#22C55E"),
    B_RANK("B-Rank", 35, "#F59E0B"),
    A_RANK("A-Rank", 50, "#F97316"),
    S_RANK("S-Rank", 70, "#EF4444"),
    NATIONAL("National Level", 90, "#D946EF"),
    MONARCH("Shadow Monarch", 100, "#8B5CF6");

    companion object {
        fun forLevel(level: Int): HunterRank {
            return entries.reversed().first { level >= it.minLevel }
        }
    }
}

/**
 * Una misión diaria asignada al jugador
 */
data class Mission(
    val id: String = UUID.randomUUID().toString(),
    val type: ExerciseType,
    val targetCount: Int,          // repeticiones objetivo
    val baseXP: Int,               // XP base
    val completedCount: Int = 0,
    val isCompleted: Boolean = false,
    val assignedDate: Long = System.currentTimeMillis(),
    val completedDate: Long? = null
) {
    val progressPercent: Float
        get() = (completedCount.toFloat() / targetCount).coerceIn(0f, 1f)

    val remaining: Int
        get() = (targetCount - completedCount).coerceAtLeast(0)
}

/**
 * Stats del jugador en cada categoría
 */
data class PlayerStats(
    val strength: Int = 1,
    val speed: Int = 1,
    val endurance: Int = 1,
    val stamina: Int = 1,
    val flexibility: Int = 1
) {
    fun get(category: StatCategory): Int = when (category) {
        StatCategory.STRENGTH -> strength
        StatCategory.SPEED -> speed
        StatCategory.ENDURANCE -> endurance
        StatCategory.STAMINA -> stamina
        StatCategory.FLEXIBILITY -> flexibility
    }

    fun increment(category: StatCategory, amount: Int = 1): PlayerStats = when (category) {
        StatCategory.STRENGTH -> copy(strength = strength + amount)
        StatCategory.SPEED -> copy(speed = speed + amount)
        StatCategory.ENDURANCE -> copy(endurance = endurance + amount)
        StatCategory.STAMINA -> copy(stamina = stamina + amount)
        StatCategory.FLEXIBILITY -> copy(flexibility = flexibility + amount)
    }

    val total: Int get() = strength + speed + endurance + stamina + flexibility
}

/**
 * Usuario / Jugador
 */
data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Cazador",
    val friendCode: String = "",
    val level: Int = 1,
    val xp: Int = 0,                    // XP en el nivel actual
    val totalXP: Int = 0,               // XP total acumulado
    val stats: PlayerStats = PlayerStats(),
    val missionsCompleted: Int = 0,
    val daysActive: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: Long = 0L,
    val streakProtection: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val premium: Boolean = false
) {
    val rank: HunterRank get() = HunterRank.forLevel(level)

    // XP del siguiente nivel (cuánto necesitas ESTE nivel)
    val xpForNextLevel: Int
        get() {
            val neededThisLevel = xpForLevelFast(level + 1) - xpForLevelFast(level)
            return neededThisLevel.coerceAtLeast(100)
        }

    // XP acumulado necesario para el nivel actual
    private val xpAtCurrentLevel: Int
        get() = xpForLevelFast(level)

    val xpProgress: Float
        get() {
            val needed = xpForNextLevel
            val have = xp
            return if (needed <= 0) 1f else (have.toFloat() / needed).coerceIn(0f, 1f)
        }

    val hunterTitle: String
        get() = when (rank) {
            HunterRank.E_RANK -> "Cazador E-Rank"
            HunterRank.D_RANK -> "Cazador D-Rank"
            HunterRank.C_RANK -> "Cazador C-Rank"
            HunterRank.B_RANK -> "Cazador B-Rank"
            HunterRank.A_RANK -> "Cazador A-Rank"
            HunterRank.S_RANK -> "Cazador S-Rank"
            HunterRank.NATIONAL -> "Cazador Nacional"
            HunterRank.MONARCH -> "Shadow Monarch"
        }
}

/**
 * Entrada de ranking
 */
data class RankingEntry(
    val userId: String,
    val name: String,
    val level: Int,
    val totalXP: Int,
    val rank: HunterRank,
    val missionsCompleted: Int,
    val currentStreak: Int = 0
)

/**
 * Relación de amistad
 */
data class Friendship(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val friendId: String,
    val friendName: String = "",
    val friendLevel: Int = 1,
    val status: FriendStatus = FriendStatus.ACCEPTED,
    val createdAt: Long = System.currentTimeMillis()
)

enum class FriendStatus {
    PENDING, ACCEPTED, BLOCKED
}

/**
 * Reto entre amigos
 */
data class Challenge(
    val id: String = UUID.randomUUID().toString(),
    val fromUserId: String,
    val toUserId: String,
    val exerciseType: ExerciseType,
    val targetCount: Int,
    val winnerId: String? = null,
    val fromUserScore: Int = 0,
    val toUserScore: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val status: ChallengeStatus = ChallengeStatus.ACTIVE
)

enum class ChallengeStatus {
    ACTIVE, COMPLETED, EXPIRED
}

/**
 * Logro / Achievement
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val requirement: Int,
    val currentProgress: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
) {
    val progressPercent: Float
        get() = (currentProgress.toFloat() / requirement).coerceIn(0f, 1f)
}

/**
 * Registro diario de actividad
 */
data class DailyLog(
    val date: Long,
    val missionsCompleted: Int = 0,
    val missionsTotal: Int = 5,
    val xpEarned: Int = 0,
    val isActive: Boolean = false
)
