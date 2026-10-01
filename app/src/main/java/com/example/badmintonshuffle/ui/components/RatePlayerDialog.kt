package com.example.badmintonshuffle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import com.example.badmintonshuffle.model.Player
import com.example.badmintonshuffle.theme.BadmintonGreenPrimary

@Composable
fun RatePlayerDialog(
    player: Player,
    currentUserId: String?,
    isSelf: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (rating: Double) -> Unit
) {
    val existingRating = if (isSelf) {
        player.selfRating
    } else {
        currentUserId?.let { player.peerRatings[it] } ?: 6.0
    }

    var ratingValue by remember { mutableFloatStateOf(existingRating.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSelf) "Update Self Rating" else "Rate ${player.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isSelf) "Set your honest skill baseline" else "Help balance doubles matches",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Score Display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%.1f".format(ratingValue),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " / 10.0",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = getRatingDescription(ratingValue.toDouble()),
                        fontWeight = FontWeight.Bold,
                        color = BadmintonGreenPrimary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Slider
                Slider(
                    value = ratingValue,
                    onValueChange = { ratingValue = Math.round(it * 10f) / 10f },
                    valueRange = 1.0f..10.0f,
                    steps = 89,
                    colors = SliderDefaults.colors(
                        thumbColor = BadmintonGreenPrimary,
                        activeTrackColor = BadmintonGreenPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1.0 (Beginner)", style = MaterialTheme.typography.labelSmall)
                    Text("5.5 (Intermediate)", style = MaterialTheme.typography.labelSmall)
                    Text("10.0 (Elite)", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSubmit(ratingValue.toDouble())
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BadmintonGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Rating", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun getRatingDescription(rating: Double): String {
    return when {
        rating >= 9.0 -> "Elite / Tournament Pro Level"
        rating >= 8.0 -> "Advanced Competitor (Strong consistency & attack)"
        rating >= 6.5 -> "Strong Intermediate (Reliable clears & rallies)"
        rating >= 5.0 -> "Regular Intermediate (Good rallies, improving footwork)"
        rating >= 3.5 -> "Novice / Casual (Learning doubles tactics)"
        else -> "Beginner (New to badminton)"
    }
}
