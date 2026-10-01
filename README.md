# 🏸 Badminton Doubles Rotation & Skill Showcase

A native Android app built with **Jetpack Compose** and **Material 3** for badminton groups, internal clubs, and recreational sessions. It automates court matchmaking, rotates partners and opponents fairly, maintains live session standings, and restricts member administration.

---

## 🌟 Key Features

### 1. 🏸 Dynamic Session & Anti-Ghost Check-In
- **Session PIN Validation**: Generates a 4-digit court code so members cannot check in remotely from home.
- **Organizer Bypass**: Session hosts can tap any arriving member to directly check them in.
- **Minimum 4-Player Threshold**: Below 4 players, members can rally and warm up freely. Once 4 arrive, Round 1 rotation unlocks.
- **1 Court or 2 Courts Toggle**: Supports single court (4 players/round) or 2 courts (8 players/round).

### 2. 🔄 Round-Robin Variety Engine & Fair Playtime
- **Partner & Opponent Diversity**: Evaluates a multi-objective cost function to ensure players play with different partners and against different opponents every round.
- **Strict Anti-Bench Rule**: Anyone who rested on the bench in the previous round has 100% priority to play in the next round.
- **Equal Games Distribution**: Tracks games played so everyone receives equal court time by the end of the session.

### 3. ⚠️ Negative Case: Player Refusal / Cramp Substitution
- 1-tap **Swap icon (`⇄`)** on any court tile.
- Replaces an exhausted/injured player with a waiting bench player and automatically re-balances the 2 teams.
- Option to put the player on a temporary break (pause) to skip upcoming rounds.

### 4. 🏆 Live Session Scoreboard
- Mark match winners directly on the court (`Team A Won 🏆` / `Team B Won 🏆`).
- **Scoring**: Win = +1 Point, Loss = 0 Points.
- Displays live session table: Rank (🥇, 🥈, 🥉), Player, Played (P), Won (W), Lost (L), Points (PTS), Win %.
- 1-tap export to WhatsApp/Telegram groups.

### 5. 👥 Restricted Member Directory (Admin PIN Protected)
- Adding, editing, and deleting members requires entering the Admin PIN (Default: `8888`).
- Casual players can view the directory and their own profile, but cannot tamper with member data.

### 6. ⭐ Skill Rating & Balance
- Clean 1.0 – 10.0 precision rating scale.
- Calculates fair skill rating from self-rating + peer ratings:
  $$\text{Effective Rating} = \frac{\text{Self Rating} + \sum \text{Peer Ratings}}{1 + \text{Total Peer Reviews}}$$

---

## 🚀 Building & Running

### Requirements
- Android Studio Ladybug / Meerkat or later
- JDK 17
- Android SDK 36 (minSdk 24)

### Clone & Build
```bash
git clone https://github.com/dirzaaulia/badminton-shuffle.git
cd badminton-shuffle
./gradlew assembleDebug
```
The debug APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
