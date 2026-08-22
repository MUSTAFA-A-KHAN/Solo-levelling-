# Firebase Integration Plan — Solo Leveling System

## Phase 1 — Firebase Foundation
**Objective:** Configure Gradle to recognize Firebase and its dependencies without changing runtime behavior.
**Files to change:**
- `build.gradle.kts` (root) — add Google Services plugin
- `app/build.gradle.kts` — apply Google Services plugin, add Firebase BOM + Auth + Firestore
- `AndroidManifest.xml` — add `INTERNET` permission
**Approach:**
1. Add `com.google.gms.google-services` plugin (v4.5.0) to root `plugins` block with `apply false`.
2. Apply the plugin in the app module.
3. Add `com.google.firebase:firebase-bom:32.8.1` as a platform, then `firebase-auth` and `firebase-firestore` using the BOM.
4. Verify `app/google-services.json` package name matches `com.sololeveling.system`.
**Verification:** `./gradlew clean assembleDebug` succeeds.

## Phase 2 — Firebase Authentication
**Objective:** Add an authentication data layer that wraps Firebase Auth and exposes the current user.
**Files to change:**
- `data/remote/auth/AuthRepository.kt` (new)
- `data/remote/auth/AuthRepositoryImpl.kt` (new)
- `di/AppModule.kt` — bind new repository
**Approach:**
1. Create `AuthRepository` interface with: `getCurrentUser()`, `signInWithGoogle()`, `signOut()`.
2. Implement using `FirebaseAuth.getInstance()`.
3. Add Google Sign-In intent launcher integration (Google Sign-In client → Firebase credential).
4. Expose auth state via a `StateFlow` or callback so ViewModels can react.
**Verification:** Unit test auth state transitions; sign-in/sign-out behavior.

## Phase 3 — Authentication UI
**Objective:** Add an initial Welcome/Auth screen without redesigning existing screens.
**Files to change:**
- `presentation/auth/WelcomeScreen.kt` (new)
- `MainActivity.kt` — adjust start destination logic
- `presentation/profile/ProfileScreen.kt` — add sign-out / account section
**Approach:**
1. First launch: show `WelcomeScreen` with "Sign in with Google" and "Continue without signing in".
2. If Firebase session already exists, skip Welcome and proceed directly to the existing flow.
3. If user skips or signs out, retain existing local `Awakening → CommandCenter` flow.
4. ProfileScreen shows account info and a sign-out button when authenticated.
**Verification:** UI smoke test; navigation flow preserves existing behavior.

## Phase 4 — Firebase Account Document
**Objective:** Create/update `users/{uid}/account` after successful authentication.
**Files to change:**
- `data/remote/firebase/FirestoreUserDataSource.kt` (new)
- `data/repository/UserRepositoryImpl.kt` (new or extend existing)
**Approach:**
1. On sign-in, write: `uid`, `displayName`, `email`, `photoUrl`, `createdAt`, `lastLoginAt`.
2. On subsequent launches, update `lastLoginAt`.
3. Do NOT store passwords, OAuth tokens, or API keys.
**Verification:** Firestore console shows correct document after sign-in.

## Phase 5 — Existing Local Data Mapping
**Objective:** Document current Room/DataStore user-specific state.
**Files to inspect:**
- `data/local/entity/PlayerEntity.kt`
- `data/local/entity/QuestEntity.kt`
- `data/local/SystemPreferences.kt`
- `domain/model/Player.kt`
- `domain/model/Quest.kt`
- `domain/model/SystemEvent.kt`
**Findings (actual codebase inspection):**

### 1. `PlayerEntity` (`player_table`) — single player, id = "PLAYER_1"
Maps to `users/{uid}/player/currentPlayer`.

| Room field | Firestore field | Notes |
|---|---|---|
| `id: String` (PK) | `id` | Stored as field; doc id = `currentPlayer` |
| `name: String` | `name` | |
| `title: String?` | `title` | nullable |
| `level: Int` | `level` | |
| `xp: Long` | `xp` | |
| `nextLevelXp: Long` | `nextLevelXp` | |
| `rank: String` (enum name E/D/C/B/A/S) | `rank` | store enum `.name` |
| `strength: Double` | `strength` | attribute |
| `agility: Double` | `agility` | attribute |
| `vitality: Double` | `vitality` | attribute |
| `intelligence: Double` | `intelligence` | attribute |
| `discipline: Double` | `discipline` | attribute |
| `endurance: Double` | `endurance` | attribute |
| `availableAttributePoints: Int` | `availableAttributePoints` | |
| `lastSyncTime: Long` | `lastSyncTime` | Health Connect sync cursor |

Domain: `Player` + `PlayerAttributes` (same fields). `Rank` enum: E(1), D(2), C(3), B(4), A(5), S(6).

### 2. `QuestEntity` (`quest_table`) — list of quests
Maps to `users/{uid}/quests/{questId}` (doc id = `id`).

| Room field | Firestore field | Notes |
|---|---|---|
| `id: String` (PK) | _doc id_ + `id` | |
| `title: String` | `title` | |
| `description: String` | `description` | |
| `difficulty: String` (enum name) | `difficulty` | E/D/C/B/A/S |
| `type: String` (enum name) | `type` | DAILY/WEEKLY/MAIN/SIDE/HIDDEN |
| `isCompleted: Boolean` | `isCompleted` | |
| `xpReward: Long` | `xpReward` | |
| `attributeRewardsJson: String` | `attributeRewards` | JSON `Map<AttributeType,Double>` (Gson) |
| `requiredActivityJson: String?` | `requiredActivity` | JSON `ActivityRequirement?` (Gson) |

Domain: `Quest`, `AttributeType` (STRENGTH/AGILITY/VITALITY/INTELLIGENCE/DISCIPLINE/ENDURANCE), `ActivityRequirement` (activityType, targetValue, currentValue), `ActivityType` (STEPS/WORKOUT_DURATION_MINUTES/RUNNING_DISTANCE_METERS/STUDY_MINUTES).

### 3. `SystemPreferences` (DataStore `system_preferences`)
App-level prefs. Currently: `lastDailyQuestDate`, `lastWeeklyQuestDate`, `welcomeShown`. NOT user-scoped — must NOT be uploaded to Firestore per-user. Keep local-only.

### 4. `SystemEvent` (domain model, `SystemEventRepository` interface)
Interface defined but **NOT implemented** — no `SystemEventEntity`, `SystemEventDao`, or `SystemEventRepositoryImpl` exists. No events table in `SystemDatabase` (entities = `[PlayerEntity, QuestEntity]` only). **Not synced.**

**Conclusion:** Only two user-progress entities exist locally — **Player** and **Quests**. Phase 8 (achievements/inventory/skills/streaks) has **no corresponding local entities** and must be skipped (do not invent models).

**Deliverable:** Exact field mappings documented above for Player → `player/currentPlayer` and Quest → `quests/{questId}`.

## Phase 6 — Player Synchronization
**Objective:** Sync Player between Room and `users/{uid}/player/currentPlayer`.
**Files to change:**
- `data/remote/firebase/FirestorePlayerDataSource.kt` (new)
- `data/repository/PlayerRepositoryImpl.kt` — add sync logic
- `presentation/commandcenter/CommandCenterViewModel.kt` — trigger sync after auth
**Rules:**
- Firebase progress wins if it already exists.
- Show confirmation dialog before replacing meaningful local progress with Firebase progress.
- If Firebase has no progress but local has meaningful progress, upload local to Firebase.
- If neither exists, use existing Awakening flow.
**Verification:** Conflict scenario unit tests; happy-path sync tests.

## Phase 7 — Quest Synchronization
**Objective:** Sync Quest between Room and `users/{uid}/quests/{questId}`.
**Files to change:**
- `data/remote/firebase/FirestoreQuestDataSource.kt` (new)
- `data/repository/QuestRepositoryImpl.kt` — add sync logic
**Approach:**
1. Map `QuestEntity` directly to Firestore document (no parallel model).
2. Sync active quests on auth; listen for remote changes when online.
3. Preserve existing local quest behavior when offline.
**Verification:** Quest completion syncs to Firestore; offline quests queue and sync on reconnect.

## Phase 8 — Additional Game Progress
**Objective:** Sync any other user-specific entities found during inspection.
**Files to change (only if entities exist):**
- Achievements → `users/{uid}/achievements/{id}`
- Inventory → `users/{uid}/inventory/{id}`
- Skills → `users/{uid}/skills/{id}`
- Streaks/Activity rewards → `users/{uid}/streaks/{id}`
**Approach:** Only implement collections for entities that actually exist in the codebase. Do not invent models.
**Verification:** Each collection tested independently.

## Phase 9 — Offline Synchronization
**Objective:** Keep Room as local source of truth; use Firestore offline persistence.
**Files to change:**
- `data/remote/firebase/FirebaseInitializer.kt` (enable persistence)
- `data/repository/*` — add network-observer-driven sync triggers
**Rules:**
- Room remains the single source of truth for UI/gameplay.
- Firestore SDK handles offline caching automatically.
- No WorkManager unless architecture genuinely requires it.
- Sync Room → Firestore and Firestore → Room on connectivity changes.
**Verification:** Airplane-mode test; data syncs correctly when connectivity returns.

## Phase 10 — Firestore Security
**Objective:** Lock down Firestore so users only access their own data.
**Rules to deploy:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /account { allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /player/{playerId} { allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /quests/{questId} { allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /achievements/{id} { allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /inventory/{id} { allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /skills/{id} { allow read, write: if request.auth != null && request.auth.uid == userId; }
      match /streaks/{id} { allow read, write: if request.auth != null && request.auth.uid == userId; }
    }
  }
}
```
**Verification:** Attempt cross-user read/write from emulator; confirm denial.

## Phase 11 — Final Verification
**Objective:** Comprehensive test coverage and clean build.
**Tests to add:**
- Anonymous/local gameplay (existing tests must still pass)
- Google authentication flow (mocked Firebase Auth)
- Session restoration on app restart
- Local progress upload when Firebase is empty
- Firebase progress restoration when local is empty/meaningful
- Conflict handling (Firebase wins with confirmation)
- Quest sync (add, complete, delete)
- Offline behavior (Room reads/writes blocked from Firestore)
- Reinstall/cross-device restore (UID-based data recovery)
- Sign-out clears local Firebase cache but preserves Room
- Security rules enforced (no public access)
**Build:** `./gradlew clean test assembleDebug`
**Deliverable:** All tests green, no lint errors, APK builds successfully.
