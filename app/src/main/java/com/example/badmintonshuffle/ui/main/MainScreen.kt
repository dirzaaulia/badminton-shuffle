package com.example.badmintonshuffle.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.badmintonshuffle.data.LocalPlayerRepository
import com.example.badmintonshuffle.theme.BadmintonGreenPrimary
import com.example.badmintonshuffle.ui.components.*
import com.example.badmintonshuffle.ui.tabs.MembersTab
import com.example.badmintonshuffle.ui.tabs.PlayersTab
import com.example.badmintonshuffle.ui.tabs.ProfileTab
import com.example.badmintonshuffle.ui.tabs.ShuffleTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = rememberLocalPlayerRepository(context)
    val viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(repository) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Badminton Shuffle 🏸",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp
                        )
                    }
                },
                actions = {
                    // Current User Indicator Pill / Switcher
                    state.currentUser?.let { user ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { viewModel.openUserSwitcher() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(user.avatarColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = user.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch User",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.currentTab == AppTab.SHUFFLE,
                    onClick = { viewModel.setTab(AppTab.SHUFFLE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Session"
                        )
                    },
                    label = { Text("Session", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BadmintonGreenPrimary,
                        selectedTextColor = BadmintonGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7)
                    )
                )

                NavigationBarItem(
                    selected = state.currentTab == AppTab.PLAYERS,
                    onClick = { viewModel.setTab(AppTab.PLAYERS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Skills"
                        )
                    },
                    label = { Text("Skills", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BadmintonGreenPrimary,
                        selectedTextColor = BadmintonGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7)
                    )
                )

                NavigationBarItem(
                    selected = state.currentTab == AppTab.MEMBERS,
                    onClick = { viewModel.setTab(AppTab.MEMBERS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Members"
                        )
                    },
                    label = { Text("Members", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BadmintonGreenPrimary,
                        selectedTextColor = BadmintonGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7)
                    )
                )

                NavigationBarItem(
                    selected = state.currentTab == AppTab.PROFILE,
                    onClick = { viewModel.setTab(AppTab.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "My Profile"
                        )
                    },
                    label = { Text("My Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BadmintonGreenPrimary,
                        selectedTextColor = BadmintonGreenPrimary,
                        indicatorColor = Color(0xFFDCFCE7)
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.currentTab) {
                AppTab.SHUFFLE -> ShuffleTab(state = state, viewModel = viewModel)
                AppTab.PLAYERS -> PlayersTab(state = state, viewModel = viewModel)
                AppTab.MEMBERS -> MembersTab(state = state, viewModel = viewModel)
                AppTab.PROFILE -> ProfileTab(state = state, viewModel = viewModel)
            }
        }
    }

    // Rate Player Dialog
    state.ratingTargetPlayer?.let { target ->
        RatePlayerDialog(
            player = target,
            currentUserId = state.currentUser?.id,
            isSelf = target.id == state.currentUser?.id,
            onDismiss = { viewModel.closeRateDialog() },
            onSubmit = { rating ->
                viewModel.submitRating(rating)
            }
        )
    }

    // Session Check-In PIN Dialog
    state.checkingInPlayer?.let { player ->
        SessionCheckInDialog(
            player = player,
            sessionCode = state.activeSession.sessionCode,
            onDismiss = { viewModel.closeCheckInDialog() },
            onConfirmCheckIn = { codeEntered ->
                viewModel.validateAndCheckIn(player.id, codeEntered)
            },
            onDirectCheckIn = {
                viewModel.checkInPlayerDirect(player.id)
            }
        )
    }

    // Player Refusal / Substitute Dialog
    val refusingPlayer = state.substitutingPlayer
    val courtNumber = state.substitutingPlayerCourtNumber
    if (refusingPlayer != null && courtNumber != null) {
        val availableBench = state.activeSession.currentRound?.restingPlayers?.filterNot { it.id == refusingPlayer.id } ?: emptyList()
        SubstitutePlayerDialog(
            courtNumber = courtNumber,
            refusingPlayer = refusingPlayer,
            availableSubstitutes = availableBench,
            onDismiss = { viewModel.closeSubstituteDialog() },
            onConfirmSubstitute = { subId ->
                viewModel.substitutePlayer(subId)
            },
            onTakeBreak = {
                viewModel.putRefusingPlayerOnBreak()
            }
        )
    }

    // Add Player Dialog
    if (state.showAddPlayerDialog) {
        AddPlayerDialog(
            onDismiss = { viewModel.closeAddPlayerDialog() },
            onAdd = { name, selfRating, style ->
                viewModel.addPlayer(name, selfRating, style)
            }
        )
    }

    // User Switcher Dialog
    if (state.showUserSwitcherDialog) {
        UserSwitcherDialog(
            players = state.players,
            currentUserId = state.currentUser?.id,
            onDismiss = { viewModel.closeUserSwitcher() },
            onSelectUser = { playerId ->
                viewModel.selectCurrentUser(playerId)
            }
        )
    }
}

@androidx.compose.runtime.Composable
private fun rememberLocalPlayerRepository(context: android.content.Context): LocalPlayerRepository {
    return androidx.compose.runtime.remember(context) {
        LocalPlayerRepository(context.applicationContext)
    }
}
