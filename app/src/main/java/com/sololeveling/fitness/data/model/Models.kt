package com.sololeveling.fitness.data.model

import com.sololeveling.fitness.util.GameEngine
import com.sololeveling.fitness.util.GameEngine.xpForLevelFast

import java.util.UUID

/**
 * Tipos de ejercicio / misión disponibles.
 * Cada uno define cuántos puntos aporta a cada stat al completarse.
 */
enum class ExerciseType(
    val displayName: String,
    val statGains: Map<StatCategory, Int>
) {
    PUSHUPS("Flexiones", mapOf(
        StatCategory.STRENGTH to 2,
        StatCategory.ENDURANCE to 1
    )),
    ABDOMINALS("Abdominales", mapOf(
        StatCategory.STRENGTH to 1,
        StatCategory.ENDURANCE to 1,
        StatCategory.SPEED to 1
    )),
    SQUATS("Sentadillas", mapOf(
        StatCategory.STRENGTH to 2,
        StatCategory.ENDURANCE to 1,
        StatCategory.FLEXIBILITY to 1
    )),
    PULLUPS("Dominadas", mapOf(
        StatCategory.STRENGTH to 3,
        StatCategory.ENDURANCE to 1
    )),
    SPRINT_100M("Sprint 100m", mapOf(
        StatCategory.SPEED to 3
    )),
    RUNNING_3KM("Carrera 3km", mapOf(
        StatCategory.SPEED to 1,
        StatCategory.ENDURANCE to 3
    )),
    LUNGES("Zancadas", mapOf(
        StatCategory.STRENGTH to 1,
        StatCategory.ENDURANCE to 1,
        StatCategory.FLEXIBILITY to 1,
        StatCategory.SPEED to 1
    )),
    JUMP_ROPE("Saltar cuerda 5min", mapOf(
        StatCategory.SPEED to 1,
        StatCategory.ENDURANCE to 2,
        StatCategory.STAMINA to 2
    )),
    STRETCHING("Estiramientos 3min", mapOf(
        StatCategory.FLEXIBILITY to 3
    ));

    /** Stat principal (el que más puntos aporta) para compatibilidad */
    val primaryStat: StatCategory
        get() = statGains.maxByOrNull { it.value }?.key ?: StatCategory.STRENGTH

    companion object {
        /** Las 9 misiones diarias en orden fijo */
        fun dailyMissionTypes(userLevel: Int = 1): List<ExerciseType> {
            return listOf(
                ExerciseType.PUSHUPS,       // 1. Flexiones
                ExerciseType.ABDOMINALS,    // 2. Abdominales
                ExerciseType.SQUATS,        // 3. Sentadillas
                ExerciseType.PULLUPS,       // 4. Dominadas
                ExerciseType.SPRINT_100M,   // 5. Sprint 100m
                ExerciseType.RUNNING_3KM,   // 6. Carrera 3km
                ExerciseType.LUNGES,        // 7. Zancadas
                ExerciseType.STRETCHING,    // 8. Estiramientos
                ExerciseType.JUMP_ROPE,     // 9. Saltar cuerda
            )
        }
    }
}

/**
 * Categorías de estadísticas que van subiendo
 */
enum class StatCategory(val displayName: String) {
    STRENGTH("Fuerza"),
    SPEED("Velocidad"),
    ENDURANCE("Resistencia"),
    STAMINA("Aguante"),
    FLEXIBILITY("Flexibilidad");
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
    val targetCount: Int,
    val baseXP: Int,
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

    /** Aplica múltiples ganancias de stats de golpe */
    fun applyGains(gains: Map<StatCategory, Int>): PlayerStats {
        var result = this
        for ((category, amount) in gains) {
            result = result.increment(category, amount)
        }
        return result
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
    val xp: Int = 0,
    val totalXP: Int = 0,
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

    val xpForNextLevel: Int
        get() {
            val neededThisLevel = xpForLevelFast(level + 1) - xpForLevelFast(level)
            return neededThisLevel.coerceAtLeast(100)
        }

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

data class RankingEntry(
    val userId: String,
    val name: String,
    val level: Int,
    val totalXP: Int,
    val rank: HunterRank,
    val missionsCompleted: Int,
    val currentStreak: Int = 0
)

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

data class DailyLog(
    val date: Long,
    val missionsCompleted: Int = 0,
    val missionsTotal: Int = 5,
    val xpEarned: Int = 0,
    val isActive: Boolean = false
)
