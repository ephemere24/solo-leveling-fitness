package com.sololeveling.fitness.util

import com.sololeveling.fitness.data.model.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Motor de juego Solo Leveling Fitness.
 * Toda la lógica de XP, niveles, multiplicadores y penalizaciones.
 */
object GameEngine {

    // ══════════════════════════════════════════
    //  XP & NIVELES
    // ══════════════════════════════════════════

    /** XP total acumulado necesario para alcanzar un nivel dado (suma progresiva) */
    fun xpForLevel(level: Int): Int =
        if (level <= 1) 0
        else {
            var total = 0
            for (lvl in 2..level) {
                total += (lvl * lvl * 50 + lvl * 100)
            }
            total
        }

    /** Versión optimizada sin loop */
    fun xpForLevelFast(level: Int): Int =
        if (level <= 1) 0
        else {
            // Suma de i²*50 + i*100 desde i=2 hasta level
            // = 50*sum(i²) + 100*sum(i) desde 2..level
            val n = level.toLong()
            val sumI = (n * (n + 1) / 2) - 1          // sum(2..n) = sum(1..n) - 1
            val sumI2 = (n * (n + 1) * (2 * n + 1) / 6) - 1  // sum(2..i²) = sum(1..n²) - 1
            (50 * sumI2 + 100 * sumI).toInt()
        }

    /** Calcula el nivel actual dado el XP total */
    fun levelFromTotalXP(totalXP: Int): Int {
        if (totalXP <= 0) return 1
        // Buscar el máximo nivel donde xpForLevelFast(level) <= totalXP
        var level = 1
        while (xpForLevelFast(level + 1) <= totalXP) {
            level++
        }
        return level
    }

    /** XP en el nivel actual (resto después de restar nivel previo) */
    fun xpInCurrentLevel(totalXP: Int, level: Int): Int {
        val accumulatedForPrevLevel = xpForLevelFast(level - 1)
        return (totalXP - accumulatedForPrevLevel).coerceAtLeast(0)
    }

    // ══════════════════════════════════════════
    //  MISIONES DIARIAS
    // ══════════════════════════════════════════

    /** Genera las misiones diarias para un día dado */
    fun generateDailyMissions(
        userLevel: Int,
        stats: PlayerStats,
        dayOfWeek: Int = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    ): List<Mission> {
        val baseTypes = ExerciseType.dailyMissionTypes().toMutableList()

        // A partir de nivel 10 se añade variedad
        if (userLevel >= 10) baseTypes.add(ExerciseType.BURPEES)
        if (userLevel >= 20) baseTypes.add(ExerciseType.RUNNING)
        if (userLevel >= 30) baseTypes.add(ExerciseType.MOUNTAIN_CLIMBERS)

        // Seleccionar 5 misiones variadas
        val selectedTypes = if (baseTypes.size >= 5) {
            baseTypes.shuffled().take(5)
        } else {
            baseTypes
        }

        return selectedTypes.map { type ->
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
            ExerciseType.PLANK -> 60  // segundos
            ExerciseType.BURPEES -> 15
            ExerciseType.RUNNING -> 180  // segundos (3 min)
            ExerciseType.MOUNTAIN_CLIMBERS -> 30
            ExerciseType.JUMPING_JACKS -> 40
            ExerciseType.STRETCHING -> 120
        }
        // Escala con nivel: +2% por nivel
        val multiplier = 1.0 + (level - 1) * 0.02
        return ceil(base * multiplier).roundToInt().coerceAtLeast(1)
    }

    private fun calculateBaseXP(type: ExerciseType, level: Int): Int {
        val base = when (type) {
            ExerciseType.PUSHUPS -> 30
            ExerciseType.SQUATS -> 25
            ExerciseType.PULLUPS -> 50     // más difícil
            ExerciseType.ABDOMINALS -> 25
            ExerciseType.PLANK -> 35
            ExerciseType.BURPEES -> 45
            ExerciseType.RUNNING -> 40
            ExerciseType.MOUNTAIN_CLIMBERS -> 30
            ExerciseType.JUMPING_JACKS -> 20
            ExerciseType.STRETCHING -> 15
        }
        return (base * (1 + level * 0.05)).roundToInt()
    }

    // ══════════════════════════════════════════
    //  SISTEMA DE MULTIPLICADOR (CONSTANCIA)
    // ══════════════════════════════════════════

    /**
     * Calcula el multiplicador por racha.
     * Cada día consecutivo suma un bonus.
     */
    fun streakMultiplier(streak: Int): Double = when {
        streak <= 0 -> 1.0
        streak <= 3 -> 1.0 + streak * 0.10     // 10-30%
        streak <= 7 -> 1.3 + (streak - 3) * 0.15 // 30-90%
        streak <= 14 -> 1.9 + (streak - 7) * 0.10 // 90-160%
        streak <= 30 -> 2.6 + (streak - 14) * 0.05 // 160-240%
        else -> 3.0 + (streak - 30) * 0.02           // 240%+ (cap soft)
    }.coerceAtMost(5.0) // Cap máximo 5x

    /**
     * Calcula XP final de una misión:
     * XP_base × multiplicador_racha × bonificador_nivel_misión
     */
    fun calculateMissionXP(mission: Mission, streak: Int): Int {
        val mult = streakMultiplier(streak)
        return (mission.baseXP * mult).roundToInt()
    }

    // ══════════════════════════════════════════
    //  PENALIZACIÓN POR INACTIVIDAD
    // ══════════════════════════════════════════

    /**
     * Calcula la penalización por días sin entrenar.
     * - Día 1 sin entrenar: aviso, sin penalización
     * - Día 2: -10% XP total (1 día de gracia gastado)
     * - Día 3: -25% XP total
     * - Día 4+: -50% XP total + pérdida de racha
     *
     * @param daysInactive días sin hacer misiones
     * @param streakProtection días de gracia disponibles
     * @return Par de (penalización_xp, días_gracia_restantes, nueva_racha)
     */
    fun calculateInactivityPenalty(
        daysInactive: Int,
        currentStreak: Int,
        streakProtection: Int = 1
    ): Triple<Double, Int, Int> {
        if (daysInactive <= 0) return Triple(1.0, streakProtection, currentStreak)

        return when (daysInactive) {
            1 -> Triple(1.0, streakProtection, currentStreak)  // Solo aviso
            2 -> {
                if (streakProtection > 0) {
                    Triple(1.0, streakProtection - 1, currentStreak) // Usa protección
                } else {
                    Triple(0.90, 0, currentStreak)  // -10%
                }
            }
            3 -> Triple(0.75, 0, currentStreak)  // -25%
            else -> Triple(0.50, 0, 0)           // -50%, racha perdida
        }
    }

    // ══════════════════════════════════════════
    //  SISTEMA DE PROGRESO POR ESTADÍSTICA
    // ══════════════════════════════════════════

    /** XP de stat ganado al completar una misión */
    fun statXPForMission(mission: Mission, streak: Int): Int =
        (mission.baseXP * 0.02 * streakMultiplier(streak)).roundToInt().coerceAtLeast(1)

    /** Actualiza stats tras completar una misión */
    fun updateStatsOnMission(stats: PlayerStats, mission: Mission): PlayerStats {
        return stats.increment(mission.type.statCategory, 1)
    }

    // ══════════════════════════════════════════
    //  LOGROS
    // ══════════════════════════════════════════

    fun getAllAchievements(): List<Achievement> = listOf(
        Achievement("first_mission", "Primera Caza", "Completa tu primera misión", "⚔️", 1),
        Achievement("missions_10", "Cazador Novato", "Completa 10 misiones", "🏅", 10),
        Achievement("missions_50", "Cazador Experto", "Completa 50 misiones", "🥉", 50),
        Achievement("missions_100", "Cazador Veterano", "Completa 100 misiones", "🥈", 100),
        Achievement("missions_500", "Cazador Legendario", "Completa 500 misiones", "🥇", 500),
        Achievement("missions_1000", "Shadow Monarch", "Completa 1000 misiones", "👑", 1000),
        Achievement("streak_3", "Constante", "Mantén una racha de 3 días", "🔥", 3),
        Achievement("streak_7", "Dedicado", "Mantén una racha de 7 días", "🔥", 7),
        Achievement("streak_30", "Imparable", "Mantén una racha de 30 días", "💥", 30),
        Achievement("streak_100", "Voluntad de Hierro", "Mantén una racha de 100 días", "⚡", 100),
        Achievement("level_10", "Cazador D-Rank", "Alcanza el nivel 10", "🔵", 10),
        Achievement("level_25", "Cazador C-Rank", "Alcanza el nivel 25", "🟢", 25),
        Achievement("level_50", "Cazador A-Rank", "Alcanza el nivel 50", "🟠", 50),
        Achievement("level_75", "Cazador S-Rank", "Alcanza el nivel 75", "🔴", 75),
        Achievement("level_100", "Shadow Monarch", "Alcanza el nivel máximo", "👑", 100),
        Achievement("stat_total_50", "Fuerza Interior", "Alcanza 50 puntos de stat total", "💪", 50),
        Achievement("stat_total_200", "Poder del Monarca", "Alcanza 200 puntos de stat total", "💎", 200),
        Achievement("friends_5", "Líder de Escuadrón", "Ten 5 amigos", "👥", 5),
        Achievement("challenge_win_10", "Campeón", "Gana 10 retos", "🏆", 10)
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
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"  // Sin 0,O,I,L para evitar confusión
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
        val statGained: StatCategory
    )

    fun completeMission(
        mission: Mission,
        userProfile: UserProfile
    ): MissionResult {
        val mult = streakMultiplier(userProfile.currentStreak)
        val earnedXP = (mission.baseXP * mult).roundToInt()
        val oldLevel = userProfile.level

        // Sumar XP y recalcular nivel
        var newTotalXP = userProfile.totalXP + earnedXP
        var newLevel = levelFromTotalXP(newTotalXP)

        // Asegurar bajar nivel si hay penalización
        if (newLevel < oldLevel) newLevel = oldLevel

        return MissionResult(
            baseXP = mission.baseXP,
            multiplier = mult,
            earnedXP = earnedXP,
            levelsGained = (newLevel - oldLevel).coerceAtLeast(0),
            oldLevel = oldLevel,
            newLevel = newLevel,
            statGained = mission.type.statCategory
        )
    }
}
