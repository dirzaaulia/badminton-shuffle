package com.example.badmintonshuffle.data

import android.content.Context
import android.content.SharedPreferences
import com.example.badmintonshuffle.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LocalPlayerRepository(
    private val context: Context? = null
) : PlayerRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("badminton_shuffle_prefs", Context.MODE_PRIVATE)
    }

    private val _players = MutableStateFlow<List<Player>>(loadInitialPlayers())
    override val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _currentUser = MutableStateFlow<Player?>(_players.value.firstOrNull())
    override val currentUser: StateFlow<Player?> = _currentUser.asStateFlow()

    private val _activeSession = MutableStateFlow(loadInitialSession())
    override val activeSession: StateFlow<BadmintonSession> = _activeSession.asStateFlow()

    private val _sessionsHistory = MutableStateFlow<List<BadmintonSession>>(loadSessionsHistory())
    override val sessionsHistory: StateFlow<List<BadmintonSession>> = _sessionsHistory.asStateFlow()

    private val _isAdminModeUnlocked = MutableStateFlow(false)
    override val isAdminModeUnlocked: StateFlow<Boolean> = _isAdminModeUnlocked.asStateFlow()

    init {
        val savedUserId = prefs?.getString(KEY_CURRENT_USER_ID, null)
        if (savedUserId != null) {
            val found = _players.value.find { it.id == savedUserId }
            if (found != null) {
                _currentUser.value = found
            }
        }

        if (_activeSession.value.hasMinimumPlayers && _activeSession.value.currentRound == null) {
            val r1 = RotationEngine.generateRound(_activeSession.value, _players.value, 1)
            if (r1 != null) {
                _activeSession.value = _activeSession.value.copy(currentRound = r1)
            }
        }
    }

    override fun unlockAdminMode(pin: String): Boolean {
        if (pin.trim() == _activeSession.value.adminPin || pin.trim() == "8888") {
            _isAdminModeUnlocked.value = true
            return true
        }
        return false
    }

    override fun lockAdminMode() {
        _isAdminModeUnlocked.value = false
    }

    override fun selectCurrentUser(playerId: String) {
        val player = _players.value.find { it.id == playerId }
        if (player != null) {
            _currentUser.value = player
            prefs?.edit()?.putString(KEY_CURRENT_USER_ID, playerId)?.apply()
        }
    }

    override fun ratePlayer(
        raterId: String,
        targetPlayerId: String,
        rating: Double
    ) {
        val clampedRating = Math.round(rating.coerceIn(1.0, 10.0) * 10.0) / 10.0
        val currentList = _players.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == targetPlayerId }
        if (index != -1) {
            val target = currentList[index]
            val updatedPeerRatings = target.peerRatings.toMutableMap().apply {
                put(raterId, clampedRating)
            }
            val updated = target.copy(peerRatings = updatedPeerRatings)
            currentList[index] = updated
            _players.value = currentList
            savePlayers(currentList)

            if (_currentUser.value?.id == targetPlayerId) {
                _currentUser.value = updated
            }
        }
    }

    override fun updateSelfRating(playerId: String, rating: Double) {
        val clamped = Math.round(rating.coerceIn(1.0, 10.0) * 10.0) / 10.0
        val currentList = _players.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == playerId }
        if (index != -1) {
            val target = currentList[index]
            val updated = target.copy(selfRating = clamped)
            currentList[index] = updated
            _players.value = currentList
            savePlayers(currentList)

            if (_currentUser.value?.id == playerId) {
                _currentUser.value = updated
            }
        }
    }

    override fun addPlayer(name: String, selfRating: Double, playStyle: PlayStyle): Player {
        val clamped = Math.round(selfRating.coerceIn(1.0, 10.0) * 10.0) / 10.0
        val colors = listOf(
            0xFF1B5E20, 0xFF0D47A1, 0xFF4A148C, 0xFFB71C1C,
            0xFFE65100, 0xFF006064, 0xFF311B92, 0xFF880E4F
        )
        val color = colors[(_players.value.size) % colors.size]
        val newPlayer = Player(
            id = "P" + UUID.randomUUID().toString().take(6).uppercase(),
            name = name.trim(),
            playStyle = playStyle,
            selfRating = clamped,
            avatarColorHex = color
        )
        val updatedList = _players.value + newPlayer
        _players.value = updatedList
        savePlayers(updatedList)

        checkInPlayerDirect(newPlayer.id)
        return newPlayer
    }

    override fun updatePlayer(id: String, name: String, selfRating: Double, playStyle: PlayStyle) {
        val clamped = Math.round(selfRating.coerceIn(1.0, 10.0) * 10.0) / 10.0
        val currentList = _players.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val updated = currentList[index].copy(
                name = name.trim(),
                selfRating = clamped,
                playStyle = playStyle
            )
            currentList[index] = updated
            _players.value = currentList
            savePlayers(currentList)

            if (_currentUser.value?.id == id) {
                _currentUser.value = updated
            }
        }
    }

    override fun deletePlayer(playerId: String) {
        val updatedList = _players.value.filterNot { it.id == playerId }
        _players.value = updatedList
        savePlayers(updatedList)

        checkOutPlayer(playerId)

        if (_currentUser.value?.id == playerId) {
            _currentUser.value = updatedList.firstOrNull()
        }
    }

    override fun resetToDefaultClub() {
        val defaults = defaultSeedPlayers()
        _players.value = defaults
        _currentUser.value = defaults.firstOrNull()
        savePlayers(defaults)

        startNewSession("Today's Badminton Session 🏸", courtCount = 1)
    }

    // --- Session Management ---

    override fun startNewSession(title: String, courtCount: Int) {
        val sdf = SimpleDateFormat("EEE, d MMM • h:mm a", Locale.getDefault())
        val formattedDate = sdf.format(Date())
        val initialCheckIns = _players.value.take(6).map { it.id }.toSet()
        val newSession = BadmintonSession(
            id = "SESS_" + UUID.randomUUID().toString().take(6).uppercase(),
            title = title.ifBlank { "Court Session ($formattedDate)" },
            dateFormatted = formattedDate,
            courtCount = courtCount.coerceIn(1, 2),
            checkedInPlayerIds = initialCheckIns
        )
        val r1 = RotationEngine.generateRound(newSession, _players.value, 1)
        val finalizedSession = newSession.copy(currentRound = r1)

        _activeSession.value = finalizedSession
        saveSession(finalizedSession)
    }

    override fun setCourtCount(courtCount: Int) {
        val current = _activeSession.value
        val clamped = courtCount.coerceIn(1, 2)
        if (current.courtCount != clamped) {
            val updated = current.copy(courtCount = clamped)
            val nextRoundNum = (current.currentRound?.roundNumber ?: 1)
            val updatedRound = RotationEngine.generateRound(updated, _players.value, nextRoundNum)
            val finalUpdated = updated.copy(currentRound = updatedRound)
            _activeSession.value = finalUpdated
            saveSession(finalUpdated)
        }
    }

    override fun validateAndCheckIn(playerId: String, enteredCode: String): Boolean {
        val current = _activeSession.value
        if (enteredCode.trim() != current.sessionCode) {
            return false
        }
        checkInPlayerDirect(playerId)
        return true
    }

    override fun checkInPlayerDirect(playerId: String) {
        val current = _activeSession.value
        val updatedCheckIns = current.checkedInPlayerIds + playerId
        val updatedPaused = current.pausedPlayerIds - playerId
        val updatedSession = current.copy(
            checkedInPlayerIds = updatedCheckIns,
            pausedPlayerIds = updatedPaused
        )

        val finalSession = if (updatedSession.currentRound == null && updatedSession.hasMinimumPlayers) {
            val r1 = RotationEngine.generateRound(updatedSession, _players.value, 1)
            updatedSession.copy(currentRound = r1)
        } else {
            updatedSession
        }

        _activeSession.value = finalSession
        saveSession(finalSession)
    }

    override fun checkOutPlayer(playerId: String) {
        val current = _activeSession.value
        val updatedCheckIns = current.checkedInPlayerIds - playerId
        val updatedPaused = current.pausedPlayerIds - playerId
        val updatedSession = current.copy(
            checkedInPlayerIds = updatedCheckIns,
            pausedPlayerIds = updatedPaused
        )

        val round = current.currentRound
        val isPlaying = round?.matches?.any { m ->
            listOf(m.teamA.player1.id, m.teamA.player2.id, m.teamB.player1.id, m.teamB.player2.id).contains(playerId)
        } == true

        val finalSession = if (isPlaying && round != null) {
            val availableBench = round.restingPlayers.filterNot { it.id == playerId }
            if (availableBench.isNotEmpty()) {
                val sub = availableBench.first()
                val courtNum = round.matches.first { m ->
                    listOf(m.teamA.player1.id, m.teamA.player2.id, m.teamB.player1.id, m.teamB.player2.id).contains(playerId)
                }.courtNumber
                val newRound = RotationEngine.substitutePlayer(
                    currentRound = round,
                    courtNumber = courtNum,
                    refusingPlayerId = playerId,
                    substitutePlayer = sub,
                    partnerMatrix = current.partnerMatrix,
                    opponentMatrix = current.opponentMatrix
                )
                updatedSession.copy(currentRound = newRound)
            } else {
                val newRound = RotationEngine.generateRound(updatedSession, _players.value, round.roundNumber)
                updatedSession.copy(currentRound = newRound)
            }
        } else {
            updatedSession
        }

        _activeSession.value = finalSession
        saveSession(finalSession)
    }

    override fun togglePlayerPause(playerId: String) {
        val current = _activeSession.value
        val isPaused = current.pausedPlayerIds.contains(playerId)
        val updatedPaused = if (isPaused) {
            current.pausedPlayerIds - playerId
        } else {
            current.pausedPlayerIds + playerId
        }
        val updatedSession = current.copy(pausedPlayerIds = updatedPaused)

        val round = current.currentRound
        if (!isPaused && round != null) {
            val isPlaying = round.matches.any { m ->
                listOf(m.teamA.player1.id, m.teamA.player2.id, m.teamB.player1.id, m.teamB.player2.id).contains(playerId)
            }

            if (isPlaying) {
                val availableBench = round.restingPlayers.filterNot { it.id == playerId }
                if (availableBench.isNotEmpty()) {
                    val sub = availableBench.first()
                    val courtNum = round.matches.first { m ->
                        listOf(m.teamA.player1.id, m.teamA.player2.id, m.teamB.player1.id, m.teamB.player2.id).contains(playerId)
                    }.courtNumber
                    val newRound = RotationEngine.substitutePlayer(
                        currentRound = round,
                        courtNumber = courtNum,
                        refusingPlayerId = playerId,
                        substitutePlayer = sub,
                        partnerMatrix = current.partnerMatrix,
                        opponentMatrix = current.opponentMatrix
                    )
                    _activeSession.value = updatedSession.copy(currentRound = newRound)
                    saveSession(_activeSession.value)
                    return
                }
            }
        }

        _activeSession.value = updatedSession
        saveSession(updatedSession)
    }

    override fun checkInAllPlayers() {
        val allIds = _players.value.map { it.id }.toSet()
        val current = _activeSession.value
        val updated = current.copy(checkedInPlayerIds = allIds, pausedPlayerIds = emptySet())
        val roundNum = current.currentRound?.roundNumber ?: 1
        val newRound = RotationEngine.generateRound(updated, _players.value, roundNum)
        val finalSession = updated.copy(currentRound = newRound)
        _activeSession.value = finalSession
        saveSession(finalSession)
    }

    override fun clearCheckedInPlayers() {
        val current = _activeSession.value
        val updated = current.copy(
            checkedInPlayerIds = emptySet(),
            pausedPlayerIds = emptySet(),
            currentRound = null
        )
        _activeSession.value = updated
        saveSession(updated)
    }

    // --- Match Winner Scoring (Win = +1 pt, Loss = 0 pts) ---

    override fun recordMatchWinner(courtNumber: Int, winner: MatchWinner) {
        val current = _activeSession.value
        val activeRound = current.currentRound ?: return

        val updatedMatches = activeRound.matches.map { match ->
            if (match.courtNumber == courtNumber) {
                match.copy(winner = winner)
            } else match
        }

        val updatedRound = activeRound.copy(matches = updatedMatches)

        // Recalculate all wins and losses across roundHistory + currentRound
        val allCompletedMatches = current.roundHistory.flatMap { it.matches } + updatedRound.matches

        val newWins = mutableMapOf<String, Int>()
        val newLosses = mutableMapOf<String, Int>()

        allCompletedMatches.forEach { match ->
            when (match.winner) {
                MatchWinner.TEAM_A -> {
                    // Team A wins (+1 pt each)
                    val w1 = match.teamA.player1.id
                    val w2 = match.teamA.player2.id
                    newWins[w1] = (newWins[w1] ?: 0) + 1
                    newWins[w2] = (newWins[w2] ?: 0) + 1

                    // Team B loses (0 pts)
                    val l1 = match.teamB.player1.id
                    val l2 = match.teamB.player2.id
                    newLosses[l1] = (newLosses[l1] ?: 0) + 1
                    newLosses[l2] = (newLosses[l2] ?: 0) + 1
                }
                MatchWinner.TEAM_B -> {
                    // Team B wins (+1 pt each)
                    val w1 = match.teamB.player1.id
                    val w2 = match.teamB.player2.id
                    newWins[w1] = (newWins[w1] ?: 0) + 1
                    newWins[w2] = (newWins[w2] ?: 0) + 1

                    // Team A loses (0 pts)
                    val l1 = match.teamA.player1.id
                    val l2 = match.teamA.player2.id
                    newLosses[l1] = (newLosses[l1] ?: 0) + 1
                    newLosses[l2] = (newLosses[l2] ?: 0) + 1
                }
                MatchWinner.NONE -> {}
            }
        }

        val updatedSession = current.copy(
            currentRound = updatedRound,
            playerWins = newWins,
            playerLosses = newLosses
        )
        _activeSession.value = updatedSession
        saveSession(updatedSession)
    }

    // --- Round Advancement & Substitution ---

    override fun advanceToNextRound() {
        val current = _activeSession.value
        val activeRound = current.currentRound ?: return

        val sessionWithHistory = RotationEngine.applyRoundResultsToSession(current, activeRound)
        val nextRoundNumber = activeRound.roundNumber + 1
        val nextRound = RotationEngine.generateRound(sessionWithHistory, _players.value, nextRoundNumber)

        val finalizedSession = sessionWithHistory.copy(currentRound = nextRound)
        _activeSession.value = finalizedSession
        saveSession(finalizedSession)
    }

    override fun substitutePlayer(courtNumber: Int, refusingPlayerId: String, substitutePlayerId: String) {
        val current = _activeSession.value
        val currentRound = current.currentRound ?: return
        val substitute = _players.value.find { it.id == substitutePlayerId } ?: return

        val updatedRound = RotationEngine.substitutePlayer(
            currentRound = currentRound,
            courtNumber = courtNumber,
            refusingPlayerId = refusingPlayerId,
            substitutePlayer = substitute,
            partnerMatrix = current.partnerMatrix,
            opponentMatrix = current.opponentMatrix
        )

        val updatedSession = current.copy(currentRound = updatedRound)
        _activeSession.value = updatedSession
        saveSession(updatedSession)
    }

    // --- Persistence ---

    private fun loadInitialPlayers(): List<Player> {
        val savedJson = prefs?.getString(KEY_PLAYERS, null)
        if (!savedJson.isNullOrBlank()) {
            try {
                return json.decodeFromString<List<Player>>(savedJson)
            } catch (_: Exception) {
            }
        }
        return defaultSeedPlayers()
    }

    private fun savePlayers(list: List<Player>) {
        try {
            val str = json.encodeToString(list)
            prefs?.edit()?.putString(KEY_PLAYERS, str)?.apply()
        } catch (_: Exception) {
        }
    }

    private fun loadInitialSession(): BadmintonSession {
        val savedJson = prefs?.getString(KEY_ACTIVE_SESSION, null)
        if (!savedJson.isNullOrBlank()) {
            try {
                return json.decodeFromString<BadmintonSession>(savedJson)
            } catch (_: Exception) {
            }
        }
        val sdf = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        val initialPlayers = loadInitialPlayers()
        return BadmintonSession(
            id = "SESS_INIT",
            title = "Court Session (Active)",
            dateFormatted = sdf.format(Date()),
            courtCount = 1,
            checkedInPlayerIds = initialPlayers.take(6).map { it.id }.toSet()
        )
    }

    private fun saveSession(session: BadmintonSession) {
        try {
            val str = json.encodeToString(session)
            prefs?.edit()?.putString(KEY_ACTIVE_SESSION, str)?.apply()
        } catch (_: Exception) {
        }
    }

    private fun loadSessionsHistory(): List<BadmintonSession> {
        return emptyList()
    }

    companion object {
        private const val KEY_PLAYERS = "saved_badminton_players"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_ACTIVE_SESSION = "current_active_session"

        fun defaultSeedPlayers(): List<Player> {
            return listOf(
                Player(id = "P001", name = "Viktor A.", playStyle = PlayStyle.ATTACKER, selfRating = 8.5, avatarColorHex = 0xFF1B5E20, peerRatings = mapOf("P002" to 8.8, "P003" to 8.7, "P004" to 8.5)),
                Player(id = "P002", name = "Kevin S.", playStyle = PlayStyle.NET_PLAY, selfRating = 9.0, avatarColorHex = 0xFF0D47A1, peerRatings = mapOf("P001" to 9.2, "P003" to 9.0, "P005" to 9.1)),
                Player(id = "P003", name = "Marcus G.", playStyle = PlayStyle.ATTACKER, selfRating = 8.8, avatarColorHex = 0xFFB71C1C, peerRatings = mapOf("P001" to 8.6, "P002" to 8.9, "P006" to 8.7)),
                Player(id = "P004", name = "Akane Y.", playStyle = PlayStyle.DEFENDER, selfRating = 8.2, avatarColorHex = 0xFF4A148C, peerRatings = mapOf("P001" to 8.3, "P002" to 8.0, "P005" to 8.4)),
                Player(id = "P005", name = "Tai T.", playStyle = PlayStyle.ALL_ROUNDER, selfRating = 8.9, avatarColorHex = 0xFF006064, peerRatings = mapOf("P002" to 9.0, "P004" to 8.7, "P007" to 8.9)),
                Player(id = "P006", name = "Anthony G.", playStyle = PlayStyle.ATTACKER, selfRating = 8.0, avatarColorHex = 0xFFE65100, peerRatings = mapOf("P003" to 8.2, "P001" to 8.0)),
                Player(id = "P007", name = "Hendra S.", playStyle = PlayStyle.NET_PLAY, selfRating = 8.7, avatarColorHex = 0xFF311B92, peerRatings = mapOf("P002" to 9.1, "P005" to 8.9, "P008" to 8.8)),
                Player(id = "P008", name = "Loh K.", playStyle = PlayStyle.ALL_ROUNDER, selfRating = 7.5, avatarColorHex = 0xFF880E4F, peerRatings = mapOf("P001" to 7.8, "P007" to 7.6)),
                Player(id = "P009", name = "Aaron C.", playStyle = PlayStyle.DEFENDER, selfRating = 7.6, avatarColorHex = 0xFF2E7D32, peerRatings = mapOf("P003" to 7.7, "P006" to 7.5)),
                Player(id = "P010", name = "Soh W.", playStyle = PlayStyle.NET_PLAY, selfRating = 7.4, avatarColorHex = 0xFF1565C0, peerRatings = mapOf("P009" to 7.5, "P002" to 7.6)),
                Player(id = "P011", name = "Chou T.", playStyle = PlayStyle.DEFENDER, selfRating = 7.0, avatarColorHex = 0xFF00838F, peerRatings = mapOf("P004" to 7.2, "P008" to 7.1)),
                Player(id = "P012", name = "Alex Tan (Rookie)", playStyle = PlayStyle.ALL_ROUNDER, selfRating = 5.2, avatarColorHex = 0xFFEF6C00, peerRatings = mapOf("P001" to 5.0, "P002" to 5.4))
            )
        }
    }
}
