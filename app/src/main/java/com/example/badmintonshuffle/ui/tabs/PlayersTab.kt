package com.example.badmintonshuffle.ui.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.badmintonshuffle.model.PlayStyle
import com.example.badmintonshuffle.theme.BadmintonGreenPrimary
import com.example.badmintonshuffle.ui.components.PlayerCard
import com.example.badmintonshuffle.ui.main.BadmintonUiState
import com.example.badmintonshuffle.ui.main.MainScreenViewModel

@Composable
fun PlayersTab(
    state: BadmintonUiState,
    viewModel: MainScreenViewModel,
    modifier: Modifier = Modifier
) {
    val filteredPlayers = state.players.filter { player ->
        val matchesQuery = state.searchQuery.isBlank() ||
                player.name.contains(state.searchQuery, ignoreCase = true) ||
                player.id.contains(state.searchQuery, ignoreCase = true)
        val matchesStyle = state.playStyleFilter == null || player.playStyle == state.playStyleFilter
        matchesQuery && matchesStyle
    }.sortedByDescending { it.calculatedRating }

    val clubAverageSkill = if (state.players.isNotEmpty()) {
        Math.round(state.players.map { it.calculatedRating }.average() * 10.0) / 10.0
    } else 0.0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddPlayerDialog() },
                containerColor = BadmintonGreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Player")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Player", fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Club Summary Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BadmintonGreenPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.players.size}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Club Members",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(36.dp),
                            color = Color.White.copy(alpha = 0.3f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⭐ $clubAverageSkill",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFEF08A)
                            )
                            Text(
                                text = "Avg Club Skill",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(36.dp),
                            color = Color.White.copy(alpha = 0.3f)
                        )

                        val totalReviews = state.players.sumOf { it.totalReviewCount }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalReviews",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Peer Reviews",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by player name or ID...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            }

            // Play Style Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.playStyleFilter == null,
                        onClick = { viewModel.setPlayStyleFilter(null) },
                        label = { Text("All (${state.players.size})") }
                    )

                    PlayStyle.entries.forEach { style ->
                        val count = state.players.count { it.playStyle == style }
                        FilterChip(
                            selected = state.playStyleFilter == style,
                            onClick = {
                                viewModel.setPlayStyleFilter(
                                    if (state.playStyleFilter == style) null else style
                                )
                            },
                            label = { Text("${style.icon} ${style.displayName} ($count)") }
                        )
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = "Player Showcase & Peer Ratings (${filteredPlayers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Player Cards
            items(filteredPlayers, key = { it.id }) { player ->
                PlayerCard(
                    player = player,
                    currentUserId = state.currentUser?.id,
                    onRateClick = { viewModel.openRateDialog(player) }
                )
            }
        }
    }
}
