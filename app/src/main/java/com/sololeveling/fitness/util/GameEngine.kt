package com.sololeveling.fitness.util

import com.sololeveling.fitness.data.model.*
import kotlin.math.ceil
import kotlin.math.roundToInt

object GameEngine {

    // ══════════════════════════════════════════
    //  XP & NIVELES
    // ══════════════════════════════════════════

    fun xpForLevel(level: Int): Int =
        if (level <= 1) 0
        else {
            var total = 0
            for (lvl in 2..level) {
                total += (lvl * lvl * 50 + lvl * 100)
            }
            total
        }

    fun xpForLevelFast(level: Int): Int =
        if (level <= 1) 0
        else {
            val n = level.toLong()
            val sumI = (n * (n + 1) / 2) - 1
            val sumI2 = (n * (n + 1) * (2 * n + 1) / 6) - 1
            (50 * sumI2 + 100 * sumI).toInt()
        }

    fun levelFromTotalXP(totalXP: Int): Int {
        if (totalXP <= 0) return 1
        var level = 1
        while (xpForLevelFast(level + 1) <= totalXP) {
            level++
        }
        return level
    }

    fun xpInCurrentLevel(totalXP: Int, level: Int): Int {
        val accumulatedForPrevLevel = xpForLevelFast(level - 1)
        return (totalXP - accumulatedForPrevLevel).coerceAtLeast(0)
    }

    // ══════════════════════════════════════════
    //  MISIONES DIARIAS
    // ══════════════════════════════════════════

    fun generateDailyMissions(
        userLevel: Int,
        stats: PlayerStats,
        dayOfWeek: Int = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    ): List<Mission> {
        val types = ExerciseType.dailyMissionTypes(userLevel)

        return types.map { type ->
            val target = calculateTarget(type, userLevel)
            val baseXP = calculateBaseXP(type, userLevel)
            Mission(
                type = type,
                targetCount = target,
                baseXP = baseXP
            )
        }
    }

    private fun calculateTarget(type: ExerciseType, level: Int): Int {
        val base = when (type) {
            ExerciseType.PUSHUPS -> 20
            ExerciseType.SQUATS -> 25
            ExerciseType.PULLUPS -> 8
            ExerciseType.ABDOMINALS -> 30
            ExerciseType.SPRINT_100M -> 1        // 1 sprint
            ExerciseType.RUNNING_3KM -> 1       // 1 carrera
            ExerciseType.LUNGES -> 20
            ExerciseType.JUMP_ROPE -> 1         // 5 min session
            ExerciseType.STRETCHING -> 1        // 3 min session
        }
        val multiplier = 1.0 + (level - 1) * 0.02
        return ceil(base * multiplier).roundToInt().coerceAtLeast(1)
    }

    private fun calculateBaseXP(type: ExerciseType, level: Int): Int {
        val base = when (type) {
            ExerciseType.PUSHUPS -> 30
            ExerciseType.SQUATS -> 25
            ExerciseType.PULLUPS -> 50
            ExerciseType.ABDOMINALS -> 25
            ExerciseType.SPRINT_100M -> 35
            ExerciseType.RUNNING_3KM -> 40
            ExerciseType.LUNGES -> 30
            ExerciseType.JUMP_ROPE -> 35
            ExerciseType.STRETCHING -> 15
        }
        return (base * (1 + level * 0.05)).roundToInt()
    }

    // ══════════════════════════════════════════
    //  SISTEMA DE MULTIPLICADOR (CONSTANCIA)
    // ══════════════════════════════════════════

    fun streakMultiplier(streak: Int): Double = when {
        streak <= 0 -> 1.0
        streak <= 3 -> 1.0 + streak * 0.10
        streak <= 7 -> 1.3 + (streak - 3) * 0.15
        streak <= 14 -> 1.9 + (streak - 7) * 0.10
        streak <= 30 -> 2.6 + (streak - 14) * 0.05
        else -> 3.0 + (streak - 30) * 0.02
    }.coerceAtMost(5.0)

    fun calculateMissionXP(mission: Mission, streak: Int): Int {
        val mult = streakMultiplier(streak)
        return (mission.baseXP * mult).roundToInt()
    }

    // ══════════════════════════════════════════
    //  PENALIZACIÓN POR INACTIVIDAD
    // ══════════════════════════════════════════

    fun calculateInactivityPenalty(
        daysInactive: Int,
        currentStreak: Int,
        streakProtection: Int = 1
    ): Triple<Double, Int, Int> {
        if (daysInactive <= 0) return Triple(1.0, streakProtection, currentStreak)

        return when (daysInactive) {
            1 -> Triple(1.0, streakProtection, currentStreak)
            2 -> {
                if (streakProtection > 0) {
                    Triple(1.0, streakProtection - 1, currentStreak)
                } else {
                    Triple(0.90, 0, currentStreak)
                }
            }
            3 -> Triple(0.75, 0, currentStreak)
            else -> Triple(0.50, 0, 0)
        }
    }

    // ══════════════════════════════════════════
    //  SISTEMA DE PROGRESO POR ESTADÍSTICA
    // ══════════════════════════════════════════

    /** Aplica los statGains de la misión a los stats del jugador */
    fun updateStatsOnMission(stats: PlayerStats, mission: Mission): PlayerStats {
        return stats.applyGains(mission.type.statGains)
    }

    // ══════════════════════════════════════════
    //  LOGROS
    // ══════════════════════════════════════════

    fun getAllAchievements(): List<Achievement> = listOf(
        Achievement("first_mission", "Primera Caza", "Completa tu primera misión", "play_arrow", 1),
        Achievement("missions_10", "Cazador Novato", "Completa 10 misiones", "sports_score", 10),
        Achievement("missions_50", "Cazador Experto", "Completa 50 misiones", "star", 50),
        Achievement("missions_100", "Cazador Veterano", "Completa 100 misiones", "star_half", 100),
        Achievement("missions_500", "Cazador Legendario", "Completa 500 misiones", "star_border", 500),
        Achievement("missions_1000", "Shadow Monarch", "Completa 1000 misiones", "workspace_premium", 1000),
        Achievement("streak_3", "Constante", "Mantén una racha de 3 días", "local_fire_department", 3),
        Achievement("streak_7", "Dedicado", "Mantén una racha de 7 días", "local_fire_department", 7),
        Achievement("streak_30", "Imparable", "Mantén una racha de 30 días", "whatshot", 30),
        Achievement("streak_100", "Voluntad de Hierro", "Mantén una racha de 100 días", "bolt", 100),
        Achievement("level_10", "Cazador D-Rank", "Alcanza el nivel 10", "trending_up", 10),
        Achievement("level_25", "Cazador C-Rank", "Alcanza el nivel 25", "trending_up", 25),
        Achievement("level_50", "Cazador A-Rank", "Alcanza el nivel 50", "rocket_launch", 50),
        Achievement("level_75", "Cazador S-Rank", "Alcanza el nivel 75", "military_tech", 75),
        Achievement("level_100", "Shadow Monarch", "Alcanza el nivel máximo", "workspace_premium", 100),
        Achievement("stat_total_50", "Fuerza Interior", "Alcanza 50 puntos de stat total", "fitness_center", 50),
        Achievement("stat_total_200", "Poder del Monarca", "Alcanza 200 puntos de stat total", "diamond", 200),
        Achievement("friends_5", "Líder de Escuadrón", "Ten 5 amigos", "group", 5),
        Achievement("challenge_win_10", "Campeón", "Gana 10 retos", "emoji_events", 10)
    )

    fun checkAchievements(
        userProfile: UserProfile,
        completedMissions: Int = userProfile.missionsCompleted,
        totalStats: Int = userProfile.stats.total,
        friendsCount: Int = 0,
        challengeWins: Int = 0
    ): List<Achievement> {
        val all = getAllAchievements()
        return all.map { ach ->
            val progress = when (ach.id) {
                "first_mission", "missions_10", "missions_50", "missions_100",
                "missions_500", "missions_1000" -> completedMissions
                "streak_3", "streak_7", "streak_30", "streak_100" -> userProfile.currentStreak
                "level_10", "level_25", "level_50", "level_75", "level_100" -> userProfile.level
                "stat_total_50", "stat_total_200" -> totalStats
                "friends_5" -> friendsCount
                "challenge_win_10" -> challengeWins
                else -> 0
            }
            ach.copy(
                currentProgress = progress,
                isUnlocked = progress >= ach.requirement
            )
        }
    }

    // ══════════════════════════════════════════
    //  GENERADOR DE CÓDIGO DE AMIGO
    // ══════════════════════════════════════════

    fun generateFriendCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    // ══════════════════════════════════════════
    //  RESUMEN: calcular XP total al completar una misión
    // ══════════════════════════════════════════

    data class MissionResult(
        val baseXP: Int,
        val multiplier: Double,
        val earnedXP: Int,
        val levelsGained: Int,
        val oldLevel: Int,
        val newLevel: Int,
        val statGained: StatCategory  // primary stat for compat
    )

    fun completeMission(
        mission: Mission,
        userProfile: UserProfile
    ): MissionResult {
        val mult = streakMultiplier(userProfile.currentStreak)
        val earnedXP = (mission.baseXP * mult).roundToInt()
        val oldLevel = userProfile.level

        var newTotalXP = userProfile.totalXP + earnedXP
        var newLevel = levelFromTotalXP(newTotalXP)

        if (newLevel < oldLevel) newLevel = oldLevel

        return MissionResult(
            baseXP = mission.baseXP,
            multiplier = mult,
            earnedXP = earnedXP,
            levelsGained = (newLevel - oldLevel).coerceAtLeast(0),
            oldLevel = oldLevel,
            newLevel = newLevel,
            statGained = mission.type.primaryStat
        )
    }
}
