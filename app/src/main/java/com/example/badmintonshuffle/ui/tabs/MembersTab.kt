package com.example.badmintonshuffle.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.badmintonshuffle.model.PlayStyle
import com.example.badmintonshuffle.model.Player
import com.example.badmintonshuffle.theme.BadmintonGreenPrimary
import com.example.badmintonshuffle.ui.main.BadmintonUiState
import com.example.badmintonshuffle.ui.main.MainScreenViewModel

@Composable
fun MembersTab(
    state: BadmintonUiState,
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    var playerToEdit by remember { mutableStateOf<Player?>(null) }
    var playerToDelete by remember { mutableStateOf<Player?>(null) }
    var showAdminUnlockDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (state.isAdminModeUnlocked) {
                        viewModel.openAddPlayerDialog()
                    } else {
                        showAdminUnlockDialog = true
                    }
                },
                containerColor = if (state.isAdminModeUnlocked) BadmintonGreenPrimary else Color(0xFF64748B),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (state.isAdminModeUnlocked) Icons.Default.PersonAdd else Icons.Default.Lock,
                        contentDescription = "Add Member"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (state.isAdminModeUnlocked) "Add Member" else "Unlock Admin",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (state.isAdminModeUnlocked) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (state.isAdminModeUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (state.isAdminModeUnlocked) BadmintonGreenPrimary else Color(0xFF64748B)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Club Member Directory",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (state.isAdminModeUnlocked) "🔓 Admin Mode Active (Editing Allowed)" else "🔒 Protected (Admin PIN Required to Edit)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (state.isAdminModeUnlocked) BadmintonGreenPrimary else Color(0xFFD97706),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Lock / Unlock Button
                            FilledTonalButton(
                                onClick = {
                                    if (state.isAdminModeUnlocked) {
                                        viewModel.lockAdminMode()
                                    } else {
                                        showAdminUnlockDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(if (state.isAdminModeUnlocked) "Lock" else "Unlock", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "To protect member records from unauthorized changes, adding, modifying, or deleting members is restricted to club organizers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        if (state.isAdminModeUnlocked) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.openAddPlayerDialog() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BadmintonGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Register New Member", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Member list title
            item {
                Text(
                    text = "Registered Members (${state.players.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Players List
            items(state.players, key = { it.id }) { player ->
                val isCurrentUser = player.id == state.currentUser?.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentUser) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(player.avatarColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = player.name.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = player.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isCurrentUser) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = BadmintonGreenPrimary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "YOU",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "ID: ${player.id} • ${player.playStyle.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Self: ${player.selfRating} • Avg: %.1f (${player.totalReviewCount} ratings)".format(player.calculatedRating),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = BadmintonGreenPrimary
                            )
                        }

                        // Actions (Only active when Admin Mode is unlocked)
                        if (state.isAdminModeUnlocked) {
                            Row {
                                IconButton(onClick = { playerToEdit = player }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Member",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { playerToDelete = player }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Member",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Admin Unlock PIN Dialog
    if (showAdminUnlockDialog) {
        AdminUnlockDialog(
            onDismiss = { showAdminUnlockDialog = false },
            onUnlock = { pin ->
                val success = viewModel.unlockAdminMode(pin)
                if (success) {
                    showAdminUnlockDialog = false
                }
                success
            }
        )
    }

    // Edit Member Dialog
    playerToEdit?.let { player ->
        EditPlayerDialog(
            player = player,
            onDismiss = { playerToEdit = null },
            onSave = { newName, newSelfRating, newStyle ->
                viewModel.updatePlayer(player.id, newName, newSelfRating, newStyle)
                playerToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    playerToDelete?.let { player ->
        AlertDialog(
            onDismissRequest = { playerToDelete = null },
            title = { Text("Delete ${player.name}?") },
            text = { Text("Are you sure you want to remove this player from the club directory? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlayer(player.id)
                        playerToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminUnlockDialog(
    onDismiss: () -> Unit,
    onUnlock: (pin: String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = BadmintonGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Access",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Enter the 4-digit Admin PIN to manage and edit members (Default PIN: 8888).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4) {
                            pin = it
                            isError = false
                        }
                    },
                    label = { Text("Admin PIN") },
                    placeholder = { Text("8888") },
                    isError = isError,
                    supportingText = {
                        if (isError) Text("Incorrect PIN. Try 8888", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val ok = onUnlock(pin)
                            if (!ok) isError = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BadmintonGreenPrimary)
                    ) {
                        Text("Unlock")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditPlayerDialog(
    player: Player,
    onDismiss: () -> Unit,
    onSave: (name: String, selfRating: Double, playStyle: PlayStyle) -> Unit
) {
    var name by remember { mutableStateOf(player.name) }
    var selfRating by remember { mutableFloatStateOf(player.selfRating.toFloat()) }
    var style by remember { mutableStateOf(player.playStyle) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Edit Member Info",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Self-Rating:", style = MaterialTheme.typography.bodyMedium)
                    Text("%.1f / 10.0".format(selfRating), fontWeight = FontWeight.Bold, color = BadmintonGreenPrimary)
                }

                Slider(
                    value = selfRating,
                    onValueChange = { selfRating = Math.round(it * 10f) / 10f },
                    valueRange = 1.0f..10.0f
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(name, selfRating.toDouble(), style) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BadmintonGreenPrimary)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
