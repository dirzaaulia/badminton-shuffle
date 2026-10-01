package com.example.badmintonshuffle.data

import com.example.badmintonshuffle.model.*
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val players: StateFlow<List<Player>>
    val currentUser: StateFlow<Player?>
    val activeSession: StateFlow<BadmintonSession>
    val sessionsHistory: StateFlow<List<BadmintonSession>>
    val isAdminModeUnlocked: StateFlow<Boolean>

    fun selectCurrentUser(playerId: String)
    fun ratePlayer(raterId: String, targetPlayerId: String, rating: Double)
    fun updateSelfRating(playerId: String, rating: Double)

    // Admin Access to Restrict Member Editing
    fun unlockAdminMode(pin: String): Boolean
    fun lockAdminMode()

    // Member Management (Restricted to Admin/Host)
    fun addPlayer(name: String, selfRating: Double, playStyle: PlayStyle): Player
    fun updatePlayer(id: String, name: String, selfRating: Double, playStyle: PlayStyle)
    fun deletePlayer(playerId: String)
    fun resetToDefaultClub()

    // Session Operations
    fun startNewSession(title: String, courtCount: Int)
    fun setCourtCount(courtCount: Int)
    fun validateAndCheckIn(playerId: String, enteredCode: String): Boolean
    fun checkInPlayerDirect(playerId: String)
    fun checkOutPlayer(playerId: String)
    fun togglePlayerPause(playerId: String)
    fun checkInAllPlayers()
    fun clearCheckedInPlayers()

    // Match Winner & Scoreboard
    fun recordMatchWinner(courtNumber: Int, winner: MatchWinner)

    // Round Rotation Progression & Negative Case
    fun advanceToNextRound()
    fun substitutePlayer(courtNumber: Int, refusingPlayerId: String, substitutePlayerId: String)
}
