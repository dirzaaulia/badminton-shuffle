package com.example.badmintonshuffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.badmintonshuffle.model.PlayerStanding
import com.example.badmintonshuffle.theme.AccentGold
import com.example.badmintonshuffle.theme.BadmintonGreenPrimary

@Composable
fun SessionScoreboardCard(
    standings: List<PlayerStanding>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Session Scoreboard 🏆",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Win = 1 Point • Loss = 0 Points",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "Hide" else "Show", fontSize = 12.sp)
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                if (standings.isEmpty()) {
                    Text(
                        text = "No games played or scored yet in this session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(28.dp)
                        )
                        Text(
                            text = "PLAYER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "P",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = "W",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = "L",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = "PTS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BadmintonGreenPrimary,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = "WIN%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Standings Rows
                    standings.forEachIndexed { index, standing ->
                        val isLeader = index == 0 && standing.points > 0
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (index) {
                                0 if standing.points > 0 -> Color(0xFFFEF9C3).copy(alpha = 0.5f)
                                else -> if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color.Transparent
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank Badge
                                Box(
                                    modifier = Modifier.width(28.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = when (index) {
                                            0 if standing.points > 0 -> "🥇"
                                            1 if standing.points > 0 -> "🥈"
                                            2 if standing.points > 0 -> "🥉"
                                            else -> "${index + 1}"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                // Player Info
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(standing.player.avatarColorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = standing.player.name.firstOrNull()?.uppercase() ?: "?",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = standing.player.name,
                                        fontWeight = if (isLeader) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }

                                // Games Played
                                Text(
                                    text = "${standing.gamesPlayed}",
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(24.dp)
                                )

                                // Wins
                                Text(
                                    text = "${standing.wins}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.width(24.dp)
                                )

                                // Losses
                                Text(
                                    text = "${standing.losses}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF991B1B),
                                    modifier = Modifier.width(24.dp)
                                )

                                // Points
                                Text(
                                    text = "${standing.points}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BadmintonGreenPrimary,
                                    modifier = Modifier.width(36.dp)
                                )

                                // Win Rate %
                                Text(
                                    text = "${standing.winRatePercent}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(38.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}
