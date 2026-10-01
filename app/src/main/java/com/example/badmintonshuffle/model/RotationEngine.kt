package com.example.badmintonshuffle.model

import kotlin.math.abs

object RotationEngine {

    /**
     * Generates the next round of doubles matches respecting:
     * 1. Fair playtime (players who rested last round get priority)
     * 2. Lowest game count priority
     * 3. Partner & Opponent variety (avoids repeated partners/opponents)
     * 4. Skill balancing (|Team A - Team B| is minimized)
     */
    fun generateRound(
        session: BadmintonSession,
        allPlayers: List<Player>,
        roundNumber: Int
    ): SessionRound? {
        val availablePlayers = allPlayers.filter { session.activeAvailablePlayerIds.contains(it.id) }
        if (availablePlayers.size < 4) return null

        // Court capacity: 1 court = 4, 2 courts = 8
        val desiredActiveCount = (session.courtCount * 4).coerceAtMost(availablePlayers.size)
        // Active court players must be a multiple of 4
        val totalActiveCourtPlayers = (desiredActiveCount / 4) * 4
        if (totalActiveCourtPlayers < 4) return null

        // Priority sorting for who gets to play:
        // 1. Players who rested in the immediate previous round MUST play (Anti-bench rule)
        val lastRestedIds = session.currentRound?.restingPlayers?.map { it.id }?.toSet() ?: emptySet()

        val sortedCandidates = availablePlayers.sortedWith(
            compareByDescending<Player> { player ->
                if (lastRestedIds.contains(player.id)) 1 else 0
            }.thenBy { player ->
                session.playerGamesPlayed[player.id] ?: 0
            }.thenBy { player ->
                player.id.hashCode() + roundNumber
            }
        )

        val activeCourtPlayers = sortedCandidates.take(totalActiveCourtPlayers)
        val restingPlayers = sortedCandidates.drop(totalActiveCourtPlayers)

        val courtCount = totalActiveCourtPlayers / 4
        val matches = mutableListOf<DoublesMatch>()

        if (courtCount == 1) {
            val (teamA, teamB) = optimizeQuartetPairing(
                quartet = activeCourtPlayers,
                partnerMatrix = session.partnerMatrix,
                opponentMatrix = session.opponentMatrix
            )
            matches.add(DoublesMatch(courtNumber = 1, teamA = teamA, teamB = teamB))
        } else {
            // Multi-court (2 courts):
            // Distribute players across courts to balance skill tiers, then optimize each court
            val sortedBySkill = activeCourtPlayers.sortedByDescending { it.calculatedRating }
            // Court 1 gets [0, 2, 5, 7], Court 2 gets [1, 3, 4, 6] (snake distribution for balanced courts)
            val court1Players = listOf(sortedBySkill[0], sortedBySkill[2], sortedBySkill[5], sortedBySkill[7])
            val court2Players = listOf(sortedBySkill[1], sortedBySkill[3], sortedBySkill[4], sortedBySkill[6])

            val (c1TeamA, c1TeamB) = optimizeQuartetPairing(court1Players, session.partnerMatrix, session.opponentMatrix)
            val (c2TeamA, c2TeamB) = optimizeQuartetPairing(court2Players, session.partnerMatrix, session.opponentMatrix)

            matches.add(DoublesMatch(courtNumber = 1, teamA = c1TeamA, teamB = c1TeamB))
            matches.add(DoublesMatch(courtNumber = 2, teamA = c2TeamA, teamB = c2TeamB))
        }

        return SessionRound(
            roundNumber = roundNumber,
            matches = matches,
            restingPlayers = restingPlayers
        )
    }

    /**
     * Evaluates all 3 possible doubles pairings of 4 players using a multi-objective cost function:
     * Cost = (SkillDiff * 2.5) + (PartnerRepeat * 8.0) + (OpponentRepeat * 3.0)
     */
    fun optimizeQuartetPairing(
        quartet: List<Player>,
        partnerMatrix: Map<String, Map<String, Int>>,
        opponentMatrix: Map<String, Map<String, Int>>
    ): Pair<DoublesTeam, DoublesTeam> {
        require(quartet.size == 4)

        val p0 = quartet[0]
        val p1 = quartet[1]
        val p2 = quartet[2]
        val p3 = quartet[3]

        // Candidate 1: (0, 1) vs (2, 3)
        val cost1 = evaluatePairingCost(p0, p1, p2, p3, partnerMatrix, opponentMatrix)
        // Candidate 2: (0, 2) vs (1, 3)
        val cost2 = evaluatePairingCost(p0, p2, p1, p3, partnerMatrix, opponentMatrix)
        // Candidate 3: (0, 3) vs (1, 2)
        val cost3 = evaluatePairingCost(p0, p3, p1, p2, partnerMatrix, opponentMatrix)

        return when {
            cost1 <= cost2 && cost1 <= cost3 -> DoublesTeam(p0, p1) to DoublesTeam(p2, p3)
            cost2 <= cost3 -> DoublesTeam(p0, p2) to DoublesTeam(p1, p3)
            else -> DoublesTeam(p0, p3) to DoublesTeam(p1, p2)
        }
    }

    private fun evaluatePairingCost(
        tA1: Player,
        tA2: Player,
        tB1: Player,
        tB2: Player,
        partnerMatrix: Map<String, Map<String, Int>>,
        opponentMatrix: Map<String, Map<String, Int>>
    ): Double {
        val skillDiff = abs((tA1.calculatedRating + tA2.calculatedRating) - (tB1.calculatedRating + tB2.calculatedRating))

        // Partner repeat penalties
        val partnerRepeatA = partnerMatrix[tA1.id]?.get(tA2.id) ?: 0
        val partnerRepeatB = partnerMatrix[tB1.id]?.get(tB2.id) ?: 0
        val totalPartnerPenalty = (partnerRepeatA + partnerRepeatB) * 8.0

        // Opponent repeat penalties
        val opp1 = opponentMatrix[tA1.id]?.get(tB1.id) ?: 0
        val opp2 = opponentMatrix[tA1.id]?.get(tB2.id) ?: 0
        val opp3 = opponentMatrix[tA2.id]?.get(tB1.id) ?: 0
        val opp4 = opponentMatrix[tA2.id]?.get(tB2.id) ?: 0
        val totalOpponentPenalty = (opp1 + opp2 + opp3 + opp4) * 2.5

        return (skillDiff * 3.0) + totalPartnerPenalty + totalOpponentPenalty
    }

    /**
     * Negative Case handler:
     * When a scheduled player refuses/can't play (exhausted, cramp, break):
     * Replaces them with the substitute player and re-balances the court!
     */
    fun substitutePlayer(
        currentRound: SessionRound,
        courtNumber: Int,
        refusingPlayerId: String,
        substitutePlayer: Player,
        partnerMatrix: Map<String, Map<String, Int>>,
        opponentMatrix: Map<String, Map<String, Int>>
    ): SessionRound {
        val updatedMatches = currentRound.matches.map { match ->
            if (match.courtNumber == courtNumber) {
                val currentCourtPlayers = listOf(
                    match.teamA.player1,
                    match.teamA.player2,
                    match.teamB.player1,
                    match.teamB.player2
                )
                // Replace refusing player with substitute
                val newQuartet = currentCourtPlayers.map { p ->
                    if (p.id == refusingPlayerId) substitutePlayer else p
                }
                val (newTeamA, newTeamB) = optimizeQuartetPairing(newQuartet, partnerMatrix, opponentMatrix)
                match.copy(teamA = newTeamA, teamB = newTeamB)
            } else {
                match
            }
        }

        // Move refusing player to resting list and remove substitute from resting list
        val updatedResting = currentRound.restingPlayers.filterNot { it.id == substitutePlayer.id }.toMutableList()
        val refusingPlayer = currentRound.matches
            .flatMap { listOf(it.teamA.player1, it.teamA.player2, it.teamB.player1, it.teamB.player2) }
            .find { it.id == refusingPlayerId }

        if (refusingPlayer != null && !updatedResting.any { it.id == refusingPlayer.id }) {
            updatedResting.add(refusingPlayer)
        }

        return currentRound.copy(
            matches = updatedMatches,
            restingPlayers = updatedResting
        )
    }

    /**
     * Updates session interaction history after a round completes
     */
    fun applyRoundResultsToSession(
        session: BadmintonSession,
        round: SessionRound
    ): BadmintonSession {
        val updatedGamesPlayed = session.playerGamesPlayed.toMutableMap()
        val updatedPartner = session.partnerMatrix.mapValues { it.value.toMutableMap() }.toMutableMap()
        val updatedOpponent = session.opponentMatrix.mapValues { it.value.toMutableMap() }.toMutableMap()

        round.matches.forEach { match ->
            val teamA = listOf(match.teamA.player1, match.teamA.player2)
            val teamB = listOf(match.teamB.player1, match.teamB.player2)

            // Increment games played
            (teamA + teamB).forEach { p ->
                updatedGamesPlayed[p.id] = (updatedGamesPlayed[p.id] ?: 0) + 1
            }

            // Partner records
            fun recordPartner(p1: String, p2: String) {
                val m1 = updatedPartner.getOrPut(p1) { mutableMapOf() }
                m1[p2] = (m1[p2] ?: 0) + 1
                val m2 = updatedPartner.getOrPut(p2) { mutableMapOf() }
                m2[p1] = (m2[p1] ?: 0) + 1
            }
            recordPartner(match.teamA.player1.id, match.teamA.player2.id)
            recordPartner(match.teamB.player1.id, match.teamB.player2.id)

            // Opponent records
            fun recordOpponent(p1: String, p2: String) {
                val m1 = updatedOpponent.getOrPut(p1) { mutableMapOf() }
                m1[p2] = (m1[p2] ?: 0) + 1
                val m2 = updatedOpponent.getOrPut(p2) { mutableMapOf() }
                m2[p1] = (m2[p1] ?: 0) + 1
            }
            teamA.forEach { a ->
                teamB.forEach { b ->
                    recordOpponent(a.id, b.id)
                }
            }
        }

        val updatedHistory = session.roundHistory + round
        return session.copy(
            playerGamesPlayed = updatedGamesPlayed,
            partnerMatrix = updatedPartner,
            opponentMatrix = updatedOpponent,
            currentRound = round,
            roundHistory = updatedHistory
        )
    }
}
