package com.example.badmintonshuffle.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class PlayerStanding(
    val player: Player,
    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val points: Int // 1 point per win, 0 for loss
) {
    val winRatePercent: Int
        get() = if (gamesPlayed > 0) Math.round((wins.toDouble() / gamesPlayed) * 100).toInt() else 0
}

@Serializable
data class SessionRound(
    val roundNumber: Int,
    val matches: List<DoublesMatch>,
    val restingPlayers: List<Player> = emptyList(),
    val timestampMillis: Long = System.currentTimeMillis()
)

@Serializable
data class BadmintonSession(
    val id: String,
    val title: String,
    val dateFormatted: String,
    val courtCount: Int = 1, // 1 or 2 courts
    val sessionCode: String = generateSessionCode(), // 4-digit check-in validation code
    val adminPin: String = "8888", // Restrict member editing to host/admin
    val checkedInPlayerIds: Set<String> = emptySet(),
    val pausedPlayerIds: Set<String> = emptySet(),
    val playerGamesPlayed: Map<String, Int> = emptyMap(),
    val playerWins: Map<String, Int> = emptyMap(), // 1 point per win
    val playerLosses: Map<String, Int> = emptyMap(), // 0 points for loss
    val partnerMatrix: Map<String, Map<String, Int>> = emptyMap(),
    val opponentMatrix: Map<String, Map<String, Int>> = emptyMap(),
    val currentRound: SessionRound? = null,
    val roundHistory: List<SessionRound> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val activeAvailablePlayerIds: Set<String>
        get() = checkedInPlayerIds - pausedPlayerIds

    val hasMinimumPlayers: Boolean
        get() = activeAvailablePlayerIds.size >= 4

    val neededPlayersCount: Int
        get() = (4 - activeAvailablePlayerIds.size).coerceAtLeast(0)

    val maxCapacityThisSession: Int
        get() = courtCount * 4

    fun calculateStandings(allPlayers: List<Player>): List<PlayerStanding> {
        val checkedIn = allPlayers.filter { checkedInPlayerIds.contains(it.id) }
        return checkedIn.map { player ->
            val played = playerGamesPlayed[player.id] ?: 0
            val wins = playerWins[player.id] ?: 0
            val losses = playerLosses[player.id] ?: 0
            PlayerStanding(
                player = player,
                gamesPlayed = played,
                wins = wins,
                losses = losses,
                points = wins // Win = 1 point, Loss = 0 points
            )
        }.sortedWith(
            compareByDescending<PlayerStanding> { it.points }
                .thenByDescending { it.winRatePercent }
                .thenByDescending { it.player.calculatedRating }
        )
    }

    companion object {
        fun generateSessionCode(): String {
            return Random.nextInt(1000, 9999).toString()
        }
    }
}
