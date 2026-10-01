package com.example.badmintonshuffle.model

import kotlin.math.abs

object Matchmaker {

    /**
     * Given a list of active players, generates balanced doubles matches for courts of 4 players each.
     * Any remainder players are placed on the bench / waiting for next rotation.
     */
    fun shuffle(
        players: List<Player>,
        mode: ShuffleMode = ShuffleMode.BALANCED
    ): ShuffleResult {
        if (players.size < 4) {
            return ShuffleResult(
                matches = emptyList(),
                benchPlayers = players,
                mode = mode
            )
        }

        val courtCount = players.size / 4
        val totalActiveCourtPlayers = courtCount * 4

        return when (mode) {
            ShuffleMode.BALANCED -> {
                // Tiered balanced: sort by skill so players on each court are in similar competitive tier
                val sorted = players.sortedByDescending { it.calculatedRating }
                val courtPlayerPool = sorted.take(totalActiveCourtPlayers)
                val bench = sorted.drop(totalActiveCourtPlayers)

                val matches = courtPlayerPool
                    .chunked(4)
                    .mapIndexed { index, quartet ->
                        val (teamA, teamB) = balanceQuartet(quartet)
                        DoublesMatch(
                            courtNumber = index + 1,
                            teamA = teamA,
                            teamB = teamB
                        )
                    }
                ShuffleResult(matches = matches, benchPlayers = bench, mode = mode)
            }

            ShuffleMode.HANDICAP_MENTOR -> {
                // Pair strongest with weakest to mentor
                val sorted = players.sortedByDescending { it.calculatedRating }.toMutableList()
                val courtPlayerPool = sorted.take(totalActiveCourtPlayers)
                val bench = sorted.drop(totalActiveCourtPlayers)

                val matches = courtPlayerPool
                    .chunked(4)
                    .mapIndexed { index, quartet ->
                        val (teamA, teamB) = mentorQuartet(quartet)
                        DoublesMatch(
                            courtNumber = index + 1,
                            teamA = teamA,
                            teamB = teamB
                        )
                    }
                ShuffleResult(matches = matches, benchPlayers = bench, mode = mode)
            }

            ShuffleMode.RANDOM_BALANCED -> {
                // Randomly assign players to courts, but balance the two teams within each court
                val shuffled = players.shuffled()
                val courtPlayerPool = shuffled.take(totalActiveCourtPlayers)
                val bench = shuffled.drop(totalActiveCourtPlayers)

                val matches = courtPlayerPool
                    .chunked(4)
                    .mapIndexed { index, quartet ->
                        val (teamA, teamB) = balanceQuartet(quartet)
                        DoublesMatch(
                            courtNumber = index + 1,
                            teamA = teamA,
                            teamB = teamB
                        )
                    }
                ShuffleResult(matches = matches, benchPlayers = bench, mode = mode)
            }
        }
    }

    /**
     * For 4 players, evaluates all 3 possible doubles pairings and selects the one
     * that minimizes the skill difference between Team A and Team B.
     */
    fun balanceQuartet(players: List<Player>): Pair<DoublesTeam, DoublesTeam> {
        require(players.size == 4) { "Quartet must contain exactly 4 players" }
        val p0 = players[0]
        val p1 = players[1]
        val p2 = players[2]
        val p3 = players[3]

        // Candidate 1: (0, 1) vs (2, 3)
        val diff1 = abs((p0.calculatedRating + p1.calculatedRating) - (p2.calculatedRating + p3.calculatedRating))
        // Candidate 2: (0, 2) vs (1, 3)
        val diff2 = abs((p0.calculatedRating + p2.calculatedRating) - (p1.calculatedRating + p3.calculatedRating))
        // Candidate 3: (0, 3) vs (1, 2)
        val diff3 = abs((p0.calculatedRating + p3.calculatedRating) - (p1.calculatedRating + p2.calculatedRating))

        return when {
            diff1 <= diff2 && diff1 <= diff3 -> {
                DoublesTeam(p0, p1) to DoublesTeam(p2, p3)
            }
            diff2 <= diff3 -> {
                DoublesTeam(p0, p2) to DoublesTeam(p1, p3)
            }
            else -> {
                DoublesTeam(p0, p3) to DoublesTeam(p1, p2)
            }
        }
    }

    /**
     * For 4 players sorted by skill: [Highest, 2nd, 3rd, Lowest]
     * Pairs (Highest + Lowest) vs (2nd + 3rd)
     */
    private fun mentorQuartet(players: List<Player>): Pair<DoublesTeam, DoublesTeam> {
        val sorted = players.sortedByDescending { it.calculatedRating }
        val tA = DoublesTeam(sorted[0], sorted[3])
        val tB = DoublesTeam(sorted[1], sorted[2])
        return tA to tB
    }
}
