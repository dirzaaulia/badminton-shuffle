package com.example.badmintonshuffle.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.badmintonshuffle.data.PlayerRepository
import com.example.badmintonshuffle.model.*
import kotlinx.coroutines.flow.*

enum class AppTab(val title: String) {
    SHUFFLE("Live Session"),
    PLAYERS("Skills & Rating"),
    MEMBERS("Members"),
    PROFILE("My Profile")
}

data class BadmintonUiState(
    val players: List<Player> = emptyList(),
    val currentUser: Player? = null,
    val activeSession: BadmintonSession = BadmintonSession("default", "Today's Session", ""),
    val isAdminModeUnlocked: Boolean = false,
    val currentTab: AppTab = AppTab.SHUFFLE,
    val ratingTargetPlayer: Player? = null,
    val showAddPlayerDialog: Boolean = false,
    val showUserSwitcherDialog: Boolean = false,
    val checkingInPlayer: Player? = null,
    val substitutingPlayerCourtNumber: Int? = null,
    val substitutingPlayer: Player? = null,
    val searchQuery: String = "",
    val playStyleFilter: PlayStyle? = null
)

class MainScreenViewModel(
    private val repository: PlayerRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.SHUFFLE)
    private val _ratingTargetPlayer = MutableStateFlow<Player?>(null)
    private val _showAddPlayerDialog = MutableStateFlow(false)
    private val _showUserSwitcherDialog = MutableStateFlow(false)
    private val _checkingInPlayer = MutableStateFlow<Player?>(null)
    private val _substitutingPlayerCourtNumber = MutableStateFlow<Int?>(null)
    private val _substitutingPlayer = MutableStateFlow<Player?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _playStyleFilter = MutableStateFlow<PlayStyle?>(null)

    val uiState: StateFlow<BadmintonUiState> = combine(
        repository.players,
        repository.currentUser,
        repository.activeSession,
        repository.isAdminModeUnlocked,
        _currentTab,
        _ratingTargetPlayer,
        _showAddPlayerDialog,
        _showUserSwitcherDialog,
        _checkingInPlayer,
        _substitutingPlayerCourtNumber,
        _substitutingPlayer,
        _searchQuery,
        _playStyleFilter
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val players = args[0] as List<Player>
        val currentUser = args[1] as? Player
        val activeSession = args[2] as BadmintonSession
        val isAdmin = args[3] as Boolean
        val tab = args[4] as AppTab
        val ratingTarget = args[5] as? Player
        val showAdd = args[6] as Boolean
        val showSwitcher = args[7] as Boolean
        val checkInTarget = args[8] as? Player
        val subCourtNum = args[9] as? Int
        val subTarget = args[10] as? Player
        val query = args[11] as String
        val filter = args[12] as? PlayStyle

        BadmintonUiState(
            players = players,
            currentUser = currentUser,
            activeSession = activeSession,
            isAdminModeUnlocked = isAdmin,
            currentTab = tab,
            ratingTargetPlayer = ratingTarget,
            showAddPlayerDialog = showAdd,
            showUserSwitcherDialog = showSwitcher,
            checkingInPlayer = checkInTarget,
            substitutingPlayerCourtNumber = subCourtNum,
            substitutingPlayer = subTarget,
            searchQuery = query,
            playStyleFilter = filter
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BadmintonUiState()
    )

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Admin Access Control ---

    fun unlockAdminMode(pin: String): Boolean {
        return repository.unlockAdminMode(pin)
    }

    fun lockAdminMode() {
        repository.lockAdminMode()
    }

    // --- Session Operations ---

    fun setCourtCount(courtCount: Int) {
        repository.setCourtCount(courtCount)
    }

    fun startNewSession(title: String, courtCount: Int) {
        repository.startNewSession(title, courtCount)
    }

    fun openCheckInDialog(player: Player) {
        _checkingInPlayer.value = player
    }

    fun closeCheckInDialog() {
        _checkingInPlayer.value = null
    }

    fun validateAndCheckIn(playerId: String, code: String): Boolean {
        return repository.validateAndCheckIn(playerId, code)
    }

    fun checkInPlayerDirect(playerId: String) {
        repository.checkInPlayerDirect(playerId)
    }

    fun checkOutPlayer(playerId: String) {
        repository.checkOutPlayer(playerId)
    }

    fun togglePlayerPause(playerId: String) {
        repository.togglePlayerPause(playerId)
    }

    fun checkInAll() {
        repository.checkInAllPlayers()
    }

    fun clearCheckIns() {
        repository.clearCheckedInPlayers()
    }

    // --- Match Winner Scoring (Win = +1 pt, Loss = 0 pt) ---

    fun recordMatchWinner(courtNumber: Int, winner: MatchWinner) {
        repository.recordMatchWinner(courtNumber, winner)
    }

    // --- Round Progression & Substitution ---

    fun advanceToNextRound() {
        repository.advanceToNextRound()
    }

    fun openSubstituteDialog(courtNumber: Int, player: Player) {
        _substitutingPlayerCourtNumber.value = courtNumber
        _substitutingPlayer.value = player
    }

    fun closeSubstituteDialog() {
        _substitutingPlayerCourtNumber.value = null
        _substitutingPlayer.value = null
    }

    fun substitutePlayer(substitutePlayerId: String) {
        val courtNum = _substitutingPlayerCourtNumber.value ?: return
        val refusingPlayer = _substitutingPlayer.value ?: return
        repository.substitutePlayer(courtNum, refusingPlayer.id, substitutePlayerId)
        closeSubstituteDialog()
    }

    fun putRefusingPlayerOnBreak() {
        val refusingPlayer = _substitutingPlayer.value ?: return
        repository.togglePlayerPause(refusingPlayer.id)
        closeSubstituteDialog()
    }

    // --- Rating Actions ---

    fun openRateDialog(targetPlayer: Player) {
        _ratingTargetPlayer.value = targetPlayer
    }

    fun closeRateDialog() {
        _ratingTargetPlayer.value = null
    }

    fun submitRating(rating: Double) {
        val target = _ratingTargetPlayer.value ?: return
        val current = repository.currentUser.value ?: return

        if (target.id == current.id) {
            repository.updateSelfRating(current.id, rating)
        } else {
            repository.ratePlayer(current.id, target.id, rating)
        }
        closeRateDialog()
    }

    fun updateSelfRating(rating: Double) {
        val current = repository.currentUser.value ?: return
        repository.updateSelfRating(current.id, rating)
    }

    // --- Member Actions (Admin Protected) ---

    fun openAddPlayerDialog() {
        _showAddPlayerDialog.value = true
    }

    fun closeAddPlayerDialog() {
        _showAddPlayerDialog.value = false
    }

    fun addPlayer(name: String, selfRating: Double, playStyle: PlayStyle) {
        repository.addPlayer(name, selfRating, playStyle)
        closeAddPlayerDialog()
    }

    fun updatePlayer(id: String, name: String, selfRating: Double, playStyle: PlayStyle) {
        repository.updatePlayer(id, name, selfRating, playStyle)
    }

    fun deletePlayer(playerId: String) {
        repository.deletePlayer(playerId)
    }

    fun openUserSwitcher() {
        _showUserSwitcherDialog.value = true
    }

    fun closeUserSwitcher() {
        _showUserSwitcherDialog.value = false
    }

    fun selectCurrentUser(playerId: String) {
        repository.selectCurrentUser(playerId)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPlayStyleFilter(style: PlayStyle?) {
        _playStyleFilter.value = style
    }

    fun resetToDefaults() {
        repository.resetToDefaultClub()
    }

    fun formatLineupForSharing(): String {
        val session = repository.activeSession.value
        val round = session.currentRound ?: return "No round active."
        val sb = StringBuilder()
        sb.append("🏸 *BADMINTON DOUBLES - ROUND ${round.roundNumber}* 🏸\n")
        sb.append("Session: ${session.title}\n\n")

        round.matches.forEach { match ->
            val winnerText = when (match.winner) {
                MatchWinner.TEAM_A -> " [🏆 Team A Won]"
                MatchWinner.TEAM_B -> " [🏆 Team B Won]"
                MatchWinner.NONE -> ""
            }
            sb.append("━━━━━━━━━━━━━━━━━━━\n")
            sb.append("🏟️ *COURT ${match.courtNumber}*$winnerText (${match.balanceVerdict} - ${match.fairnessPercent}% Fair)\n")
            sb.append("🔵 Team A (Skill: ${match.teamA.totalSkill}):\n")
            sb.append("   • ${match.teamA.player1.name} (%.1f)\n".format(match.teamA.player1.calculatedRating))
            sb.append("   • ${match.teamA.player2.name} (%.1f)\n".format(match.teamA.player2.calculatedRating))
            sb.append("🔴 Team B (Skill: ${match.teamB.totalSkill}):\n")
            sb.append("   • ${match.teamB.player1.name} (%.1f)\n".format(match.teamB.player1.calculatedRating))
            sb.append("   • ${match.teamB.player2.name} (%.1f)\n".format(match.teamB.player2.calculatedRating))
            sb.append("⚖️ Skill Difference: ${match.skillDifference} pts\n\n")
        }

        if (round.restingPlayers.isNotEmpty()) {
            sb.append("⏳ *Next Rotation / Bench:*\n")
            sb.append(round.restingPlayers.joinToString(", ") { "${it.name} (%.1f)".format(it.calculatedRating) })
            sb.append("\n\n")
        }

        // Add Standings Leaderboard to Share
        val standings = session.calculateStandings(repository.players.value)
        if (standings.any { it.points > 0 }) {
            sb.append("🏆 *SESSION STANDINGS (1 pt per win)*:\n")
            standings.take(5).forEachIndexed { i, s ->
                val medal = when (i) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "${i + 1}." }
                sb.append("$medal ${s.player.name}: ${s.points} pts (${s.wins}W - ${s.losses}L, ${s.winRatePercent}%)\n")
            }
        }

        sb.append("\nGenerated with Badminton Shuffle App 🏸")
        return sb.toString()
    }
}
