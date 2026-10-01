package com.example.badmintonshuffle.data

/**
 * Firebase Firestore Architecture Guide:
 *
 * To connect this app directly to your Firebase Firestore cloud database:
 * 1. Create a Firebase project at https://console.firebase.google.com
 * 2. Add an Android app with package name: com.example.badmintonshuffle
 * 3. Download google-services.json and place it in the app/ directory
 * 4. In build.gradle.kts (root):
 *      plugins {
 *          id("com.google.gms.google-services") version "4.4.2" apply false
 *      }
 * 5. In app/build.gradle.kts:
 *      plugins {
 *          id("com.google.gms.google-services")
 *      }
 *      dependencies {
 *          implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
 *          implementation("com.google.firebase:firebase-firestore-ktx")
 *          implementation("com.google.firebase:firebase-auth-ktx")
 *      }
 *
 * Firestore Document Schema:
 * Collection: "players"
 * Document ID: playerId (e.g. "P001")
 * Fields:
 *  - name: String
 *  - playStyle: String ("ALL_ROUNDER", "ATTACKER", etc.)
 *  - selfRating: Double (e.g. 8.5)
 *  - avatarColorHex: Long
 *  - peerRatings: Map<String, Double> (raterId -> score)
 *  - peerTags: Map<String, List<String>> (raterId -> tags)
 */
object FirebaseSetupGuide {
    const val PLAYERS_COLLECTION = "players"
    const val SESSIONS_COLLECTION = "sessions"
}
