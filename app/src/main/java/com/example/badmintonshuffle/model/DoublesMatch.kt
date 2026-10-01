package com.example.badmintonshuffle.model

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@Serializable
enum class MatchWinner {
    NONE,
    TEAM_A,
    TEAM_B
}

@Serializable
data class DoublesTeam(
    val player1: Player,
    val player2: Player
) {
    val totalSkill: Double
        get() = Math.round((player1.calculatedRating + player2.calculatedRating) * 10.0) / 10.0

    val averageSkill: Double
        get() = Math.round((totalSkill / 2.0) * 10.0) / 10.0
}

@Serializable
data class DoublesMatch(
    val courtNumber: Int,
    val teamA: DoublesTeam,
    val teamB: DoublesTeam,
    val winner: MatchWinner = MatchWinner.NONE
) {
    val skillDifference: Double
        get() = Math.round(abs(teamA.totalSkill - teamB.totalSkill) * 10.0) / 10.0

    /**
     * Percentage showing how evenly matched the two sides are.
     * Difference of 0.0 = 100%, 0.5 = ~93%, 1.0 = ~85%, 2.0 = ~70%
     */
    val fairnessPercent: Int
        get() {
            val score = 100 - (skillDifference * 15.0).roundToInt()
            return max(30, score).coerceAtMost(100)
        }

    val balanceVerdict: String
        get() = when {
            skillDifference <= 0.3 -> "Perfect Match"
            skillDifference <= 0.8 -> "Very Balanced"
            skillDifference <= 1.5 -> "Competitive"
            else -> "Challenger Game"
        }
}

@Serializable
data class ShuffleResult(
    val matches: List<DoublesMatch>,
    val benchPlayers: List<Player> = emptyList(),
    val mode: ShuffleMode = ShuffleMode.BALANCED
)

enum class ShuffleMode(val title: String, val description: String) {
    BALANCED(
        title = "Skill Balanced",
        description = "Minimizes skill difference between Team A and Team B for the closest games"
    ),
    HANDICAP_MENTOR(
        title = "Mentor / Handicap",
        description = "Pairs the highest rated players with lower rated players to teach & balance"
    ),
    RANDOM_BALANCED(
        title = "Mixed Shuffle",
        description = "Randomly groups players onto courts, then balances the teams within each court"
    )
}
