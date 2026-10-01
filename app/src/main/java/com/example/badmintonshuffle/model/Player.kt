package com.example.badmintonshuffle.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlayStyle(val displayName: String, val icon: String) {
    ALL_ROUNDER("All-Rounder", "🎯"),
    ATTACKER("Attacker", "⚡"),
    DEFENDER("Defender", "🛡️"),
    NET_PLAY("Net Player", "🏸")
}

@Serializable
data class Player(
    val id: String,
    val name: String,
    val playStyle: PlayStyle = PlayStyle.ALL_ROUNDER,
    val selfRating: Double, // 1.0 to 10.0
    val peerRatings: Map<String, Double> = emptyMap(), // raterPlayerId -> rating (1.0 to 10.0)
    val avatarColorHex: Long = 0xFF1B5E20
) {
    /**
     * Peer average rating (excludes self-rating)
     */
    val peerAverageRating: Double?
        get() = if (peerRatings.isNotEmpty()) {
            Math.round(peerRatings.values.average() * 10.0) / 10.0
        } else null

    /**
     * Overall calculated average rating:
     * Balanced weighted average of peer reviews + self evaluation.
     */
    val calculatedRating: Double
        get() {
            if (peerRatings.isEmpty()) return selfRating
            val total = selfRating + peerRatings.values.sum()
            val count = 1 + peerRatings.size
            return Math.round((total / count) * 10.0) / 10.0
        }

    val totalReviewCount: Int
        get() = peerRatings.size

    val skillTier: String
        get() = when {
            calculatedRating >= 8.5 -> "Elite"
            calculatedRating >= 7.0 -> "Advanced"
            calculatedRating >= 5.0 -> "Intermediate"
            calculatedRating >= 3.0 -> "Novice"
            else -> "Beginner"
        }
}
