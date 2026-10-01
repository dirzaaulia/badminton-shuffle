package com.example.badmintonshuffle.ui.tabs

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.badmintonshuffle.model.MatchWinner
import com.example.badmintonshuffle.model.Player
import com.example.badmintonshuffle.theme.BadmintonGreenPrimary
import com.example.badmintonshuffle.ui.components.CourtCard
import com.example.badmintonshuffle.ui.main.BadmintonUiState
import com.example.badmintonshuffle.ui.main.MainScreenViewModel

@Composable
fun ShuffleTab(
    state: BadmintonUiState,
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session = state.activeSession
    val checkedInCount = session.checkedInPlayerIds.size
    val activeAvailableCount = session.activeAvailablePlayerIds.size
    var showNewSessionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Session Control & Validation Code Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🏸 ${session.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = session.dateFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Session PIN Code Badge (Prevents remote ghost check-ins)
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = BadmintonGreenPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PIN: ${session.sessionCode}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = BadmintonGreenPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = { showNewSessionDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("New", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Court Count Toggle: 1 Court vs 2 Courts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Courts Booked:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = session.courtCount == 1,
                                onClick = { viewModel.setCourtCount(1) },
                                label = { Text("1 Court (4 players)") }
                            )
                            FilterChip(
                                selected = session.courtCount == 2,
                                onClick = { viewModel.setCourtCount(2) },
                                label = { Text("2 Courts (8 players)") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Check-in Attendance Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Court Check-In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (activeAvailableCount < 4) {
                                    "$activeAvailableCount / 4 players needed (play freely until 4 arrive)"
                                } else {
                                    "$activeAvailableCount players active (${session.courtCount * 4} on court, ${activeAvailableCount - (session.courtCount * 4).coerceAtMost(activeAvailableCount)} waiting)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (activeAvailableCount >= 4) BadmintonGreenPrimary else Color(0xFFD97706),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = { viewModel.checkInAll() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Check All", fontSize = 11.sp)
                            }
                            TextButton(
                                onClick = { viewModel.clearCheckIns() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Clear", fontSize = 11.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Player Check-in Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.players.forEach { player ->
                            val isCheckedIn = session.checkedInPlayerIds.contains(player.id)
                            val isPaused = session.pausedPlayerIds.contains(player.id)
                            SessionPlayerChip(
                                player = player,
                                isCheckedIn = isCheckedIn,
                                isPaused = isPaused,
                                onCheckInClick = {
                                    if (isCheckedIn) {
                                        viewModel.checkOutPlayer(player.id)
                                    } else {
                                        viewModel.openCheckInDialog(player)
                                    }
                                },
                                onTogglePause = {
                                    viewModel.togglePlayerPause(player.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Live Round Display or Minimum Player Notice
        val round = session.currentRound

        if (activeAvailableCount < 4 || round == null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏸 Need Minimum 4 Players",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Currently $activeAvailableCount players are checked in. Below 4 players, members can warm up and rally freely! Once 4 players check in using the session PIN (${session.sessionCode}), balanced doubles rounds will begin automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF78350F),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            // Active Round Header & Progression
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = BadmintonGreenPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ROUND ${round.roundNumber}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Active Game",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val shareText = viewModel.formatLineupForSharing()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Lineup via"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.advanceToNextRound() },
                            colors = ButtonDefaults.buttonColors(containerColor = BadmintonGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Next Round ➔", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Courts
            items(round.matches, key = { "${round.roundNumber}_court_${it.courtNumber}" }) { match ->
                CourtCard(
                    match = match,
                    onPlayerSubstituteClick = { courtNum, player ->
                        viewModel.openSubstituteDialog(courtNum, player)
                    },
                    onRecordWinner = { courtNum, winner ->
                        viewModel.recordMatchWinner(courtNum, winner)
                    }
                )
            }

            // Session Scoreboard Table (Win = 1 pt, Loss = 0 pt)
            item {
                com.example.badmintonshuffle.ui.components.SessionScoreboardCard(
                    standings = session.calculateStandings(state.players)
                )
            }

            // Bench / Resting Players Card (Next in rotation)
            if (round.restingPlayers.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = Color(0xFF92400E),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Next in Line for Round ${round.roundNumber + 1} (${round.restingPlayers.size} Resting)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Anti-bench rule: Waiting players have 100% priority to play in the next round.",
                                fontSize = 11.sp,
                                color = Color(0xFF78350F)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                round.restingPlayers.forEach { benchPlayer ->
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${benchPlayer.name} (⭐ %.1f)".format(benchPlayer.calculatedRating),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Session Dialog
    if (showNewSessionDialog) {
        NewSessionDialog(
            onDismiss = { showNewSessionDialog = false },
            onConfirm = { title, courtCount ->
                viewModel.startNewSession(title, courtCount)
                showNewSessionDialog = false
            }
        )
    }
}

@Composable
private fun SessionPlayerChip(
    player: Player,
    isCheckedIn: Boolean,
    isPaused: Boolean,
    onCheckInClick: () -> Unit,
    onTogglePause: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            isPaused -> Color(0xFFFEE2E2)
            isCheckedIn -> Color(0xFFDCFCE7)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isCheckedIn && !isPaused) androidx.compose.foundation.BorderStroke(1.5.dp, BadmintonGreenPrimary) else null,
        modifier = Modifier.clickable { onCheckInClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(player.avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = player.name,
                    fontSize = 12.sp,
                    fontWeight = if (isCheckedIn) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isPaused -> Color(0xFF991B1B)
                        isCheckedIn -> Color(0xFF14532D)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = when {
                        isPaused -> "⏸ Resting / Break"
                        isCheckedIn -> "✓ In (%.1f)".format(player.calculatedRating)
                        else -> "+ Check In"
                    },
                    fontSize = 10.sp,
                    fontWeight = if (isCheckedIn) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isPaused -> Color(0xFF991B1B)
                        isCheckedIn -> BadmintonGreenPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun NewSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, courtCount: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var courts by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Start New Badminton Session 🏸",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Friday Evening Doubles") },
                    label = { Text("Session Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Courts Booked:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = courts == 1,
                        onClick = { courts = 1 },
                        label = { Text("1 Court (4 players)") }
                    )
                    FilterChip(
                        selected = courts == 2,
                        onClick = { courts = 2 },
                        label = { Text("2 Courts (8 players)") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(title, courts) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BadmintonGreenPrimary)
                    ) {
                        Text("Start Session")
                    }
                }
            }
        }
    }
}
