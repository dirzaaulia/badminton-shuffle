package com.example.badmintonshuffle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.badmintonshuffle.model.DoublesMatch
import com.example.badmintonshuffle.model.DoublesTeam
import com.example.badmintonshuffle.model.MatchWinner
import com.example.badmintonshuffle.model.Player
import com.example.badmintonshuffle.theme.*

@Composable
fun CourtCard(
    match: DoublesMatch,
    modifier: Modifier = Modifier,
    onPlayerSubstituteClick: ((courtNumber: Int, player: Player) -> Unit)? = null,
    onRecordWinner: ((courtNumber: Int, winner: MatchWinner) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Court Header & Balance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = BadmintonGreenPrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Court ${match.courtNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = match.balanceVerdict,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (match.skillDifference <= 0.5) BadmintonGreenPrimary else Color(0xFFD97706),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Balance meter pill
                Surface(
                    color = if (match.skillDifference <= 0.5) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = "Balance",
                            tint = if (match.skillDifference <= 0.5) Color(0xFF15803D) else Color(0xFFB45309),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${match.fairnessPercent}% Fair (Δ ${match.skillDifference})",
                            color = if (match.skillDifference <= 0.5) Color(0xFF15803D) else Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // The Badminton Court Field
            BadmintonCourtGraphic(
                courtNumber = match.courtNumber,
                teamA = match.teamA,
                teamB = match.teamB,
                onPlayerSubstituteClick = onPlayerSubstituteClick,
                modifier = Modifier.fillMaxWidth()
            )

            // Winner Selection (Scoreboard: Win = 1 pt, Loss = 0 pt)
            if (onRecordWinner != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val newWinner = if (match.winner == MatchWinner.TEAM_A) MatchWinner.NONE else MatchWinner.TEAM_A
                            onRecordWinner(match.courtNumber, newWinner)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (match.winner == MatchWinner.TEAM_A) Color(0xFFDBEAFE) else Color.Transparent,
                            contentColor = if (match.winner == MatchWinner.TEAM_A) TeamBlue else MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (match.winner == MatchWinner.TEAM_A) TeamBlue else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = if (match.winner == MatchWinner.TEAM_A) "🏆 Team A Won (+1)" else "Team A Won",
                            fontWeight = if (match.winner == MatchWinner.TEAM_A) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val newWinner = if (match.winner == MatchWinner.TEAM_B) MatchWinner.NONE else MatchWinner.TEAM_B
                            onRecordWinner(match.courtNumber, newWinner)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (match.winner == MatchWinner.TEAM_B) Color(0xFFFEE2E2) else Color.Transparent,
                            contentColor = if (match.winner == MatchWinner.TEAM_B) TeamRed else MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (match.winner == MatchWinner.TEAM_B) TeamRed else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = if (match.winner == MatchWinner.TEAM_B) "🏆 Team B Won (+1)" else "Team B Won",
                            fontWeight = if (match.winner == MatchWinner.TEAM_B) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadmintonCourtGraphic(
    courtNumber: Int,
    teamA: DoublesTeam,
    teamB: DoublesTeam,
    onPlayerSubstituteClick: ((courtNumber: Int, player: Player) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BadmintonCourtGreen)
            .border(2.dp, BadmintonCourtBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val lineColor = BadmintonCourtLine.copy(alpha = 0.5f)
            val strokeW = 2.dp.toPx()

            // Outer doubles boundary
            drawRect(
                color = lineColor,
                size = size,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
            )

            // Net line (center horizontal)
            val netY = h / 2f
            drawLine(
                color = Color.White,
                start = Offset(0f, netY),
                end = Offset(w, netY),
                strokeWidth = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
            )

            // Center service line
            drawLine(
                color = lineColor,
                start = Offset(w / 2f, 0f),
                end = Offset(w / 2f, h),
                strokeWidth = strokeW
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Team A Side (Blue)
            TeamSection(
                courtNumber = courtNumber,
                team = teamA,
                teamLabel = "TEAM A",
                teamColor = TeamBlue,
                teamContainerColor = Color(0xFF1E3A8A).copy(alpha = 0.85f),
                onPlayerSubstituteClick = onPlayerSubstituteClick
            )

            // Net divider indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🏸 NET 🏸",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }

            // Team B Side (Red/Orange)
            TeamSection(
                courtNumber = courtNumber,
                team = teamB,
                teamLabel = "TEAM B",
                teamColor = TeamRed,
                teamContainerColor = Color(0xFF7F1D1D).copy(alpha = 0.85f),
                onPlayerSubstituteClick = onPlayerSubstituteClick
            )
        }
    }
}

@Composable
private fun TeamSection(
    courtNumber: Int,
    team: DoublesTeam,
    teamLabel: String,
    teamColor: Color,
    teamContainerColor: Color,
    onPlayerSubstituteClick: ((courtNumber: Int, player: Player) -> Unit)? = null
) {
    Surface(
        color = teamContainerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = teamLabel,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Total Skill: ${team.totalSkill} (Avg: ${team.averageSkill})",
                    color = AccentGoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlayerCourtTile(
                    courtNumber = courtNumber,
                    player = team.player1,
                    onPlayerSubstituteClick = onPlayerSubstituteClick,
                    modifier = Modifier.weight(1f)
                )
                PlayerCourtTile(
                    courtNumber = courtNumber,
                    player = team.player2,
                    onPlayerSubstituteClick = onPlayerSubstituteClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PlayerCourtTile(
    courtNumber: Int,
    player: Player,
    onPlayerSubstituteClick: ((courtNumber: Int, player: Player) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(player.avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐ %.1f".format(player.calculatedRating),
                        color = AccentGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Swap Button (Caters for Refusal / Exhaustion / Injury)
            if (onPlayerSubstituteClick != null) {
                IconButton(
                    onClick = { onPlayerSubstituteClick(courtNumber, player) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap or Refuse Game",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
