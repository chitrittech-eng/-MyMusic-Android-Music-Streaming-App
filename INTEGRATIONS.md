# MyMusic — Integrations Reference

A complete reference of every third-party library, SDK, and framework integrated into the **MyMusic** Android application.

---

## Table of Contents

- [Build & Tooling Plugins](#-build--tooling-plugins)
- [Firebase (Google)](#-firebase-google)
- [Jetpack Compose & AndroidX UI](#-jetpack-compose--androidx-ui)
- [Dependency Injection — Hilt](#-dependency-injection--hilt)
- [Media Playback — Media3 / ExoPlayer](#-media-playback--media3--exoplayer)
- [Local Database — Room](#-local-database--room)
- [Image Loading — Coil](#-image-loading--coil)
- [Background Work — WorkManager](#-background-work--workmanager)
- [Kotlin Coroutines](#-kotlin-coroutines)
- [Accompanist](#-accompanist)
- [Lottie Animations](#-lottie-animations)
- [Vico Charts](#-vico-charts)
- [Navigation](#-navigation)
- [Testing](#-testing)
- [Architecture Overview](#-architecture-overview)
- [Version Summary](#-version-summary)

---

## 🔧 Build & Tooling Plugins

| Plugin | ID | Version |
|---|---|---|
| Android Gradle Plugin | `com.android.application` | `8.7.3` |
| Kotlin Android | `org.jetbrains.kotlin.android` | `2.0.21` |
| Kotlin Compose Compiler | `org.jetbrains.kotlin.plugin.compose` | `2.0.21` |
| Hilt Android | `com.google.dagger.hilt.android` | `2.52` |
| KSP (Kotlin Symbol Processing) | `com.google.devtools.ksp` | `2.0.21-1.0.28` |
| Google Services | `com.google.gms.google-services` | `4.4.2` |

**Configuration:**
- `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`
- Java / JVM target: `17`
- `buildConfig = true` enabled for compile-time constants
- `isMinifyEnabled = true` in release (ProGuard enabled)

---

## 🔥 Firebase (Google)

> **BOM Version:** `33.7.0`
> **Config file:** `app/google-services.json`
> **Setup guide:** [FIREBASE_SETUP.md](./FIREBASE_SETUP.md)

### Firebase Authentication
- **Artifact:** `com.google.firebase:firebase-auth-ktx`
- **Purpose:** User sign-in / sign-up
- **Sign-in methods:** Email + Password, Google Sign-In
- **Used in:** `AuthViewModel.kt`
- **DI Provider:** `FirebaseModule.kt` → `FirebaseAuth.getInstance()`

### Cloud Firestore
- **Artifact:** `com.google.firebase:firebase-firestore-ktx`
- **Purpose:** Primary remote database for all app data
- **Offline persistence:** ✅ Enabled via `setPersistenceEnabled(true)`
- **DI Provider:** `FirebaseModule.kt`

**Collections used:**

| Collection | Description |
|---|---|
| `users` | User profiles and roles (`listener`, `artist`, `moderator`, `admin`) |
| `songs` | Song metadata, approval status, play/like counts |
| `albums` | Album metadata and song references |
| `playlists` | User-created playlists (public/private) |
| `artists` | Artist profiles, followers, monthly listeners |
| `categories` | Browse categories (admin-managed) |
| `reports` | Content moderation reports |
| `notifications` | In-app notification records |
| `users/{userId}/liked_songs` | Per-user liked songs subcollection |

**Repositories:** `SongRepository`, `AlbumRepository`, `ArtistRepository`, `PlaylistRepository`, `UserRepository`, `CategoryRepository`, `ReportRepository`

### Firebase Storage
- **Artifact:** `com.google.firebase:firebase-storage-ktx`
- **Purpose:** Binary file storage for audio and images
- **DI Provider:** `FirebaseModule.kt` → `FirebaseStorage.getInstance()`
- **Used in:** `StorageRepository.kt`, `UploadViewModel.kt`

**Storage paths:**

| Path | Purpose | Size Limit |
|---|---|---|
| `music/{songId}/` | Audio files | 50 MB |
| `covers/songs/` | Song cover art | 5 MB |
| `covers/albums/` | Album cover art | 5 MB |
| `covers/playlists/` | Playlist cover art | 5 MB |
| `profiles/{userId}/` | User profile photos | 5 MB |

### Firebase Analytics
- **Artifact:** `com.google.firebase:firebase-analytics-ktx`
- **Purpose:** User behaviour analytics and event tracking

### Google Play Services Auth
- **Artifact:** `com.google.android.gms:play-services-auth:21.3.0`
- **Purpose:** Google Sign-In credential provider for Firebase Auth

---

## 🎨 Jetpack Compose & AndroidX UI

> **Compose BOM:** `2024.12.01`

| Library | Group / Artifact | Purpose |
|---|---|---|
| Compose UI | `androidx.compose.ui:ui` | Core UI rendering |
| Compose UI Graphics | `androidx.compose.ui:ui-graphics` | Drawing primitives |
| Material 3 | `androidx.compose.material3:material3` | Design system components |
| Material Icons Extended | `androidx.compose.material:material-icons-extended` | Full icon set |
| UI Tooling Preview | `androidx.compose.ui:ui-tooling-preview` | `@Preview` support |
| UI Tooling (debug) | `androidx.compose.ui:ui-tooling` | Layout inspector |
| Core KTX | `androidx.core:core-ktx:1.15.0` | Kotlin extensions for Android |
| Lifecycle Runtime KTX | `androidx.lifecycle:lifecycle-runtime-ktx:2.8.7` | `lifecycleScope`, `repeatOnLifecycle` |
| Lifecycle ViewModel Compose | `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7` | `viewModel()` composable |
| Lifecycle Runtime Compose | `androidx.lifecycle:lifecycle-runtime-compose:2.8.7` | `collectAsStateWithLifecycle` |
| Activity Compose | `androidx.activity:activity-compose:1.9.3` | `ComponentActivity` + Compose host |

---

## 💉 Dependency Injection — Hilt

> **Version:** `2.52`

| Artifact | Role |
|---|---|
| `com.google.dagger:hilt-android` | Runtime DI container |
| `com.google.dagger:hilt-android-compiler` (KSP) | Annotation processor |
| `androidx.hilt:hilt-navigation-compose:1.2.0` | `hiltViewModel()` composable |
| `androidx.hilt:hilt-work:1.2.0` | Hilt + WorkManager bridge |
| `androidx.hilt:hilt-compiler:1.2.0` (KSP) | Hilt-Work processor |

**DI Modules:**
- `FirebaseModule` — Provides `FirebaseAuth`, `FirebaseFirestore`, `FirebaseStorage`
- `DatabaseModule` — Provides `AppDatabase` and DAOs
- `PlayerModule` — Provides `MusicController`

---

## 🎵 Media Playback — Media3 / ExoPlayer

> **Version:** `1.5.0`

| Artifact | Purpose |
|---|---|
| `androidx.media3:media3-exoplayer` | Core audio/video playback engine |
| `androidx.media3:media3-ui` | Pre-built player UI components |
| `androidx.media3:media3-session` | Media session & notification controls |
| `androidx.media3:media3-datasource-okhttp` | OkHttp-backed network data source |

**Key files:**
- `MusicService.kt` — Foreground `MediaSessionService`
- `MusicController.kt` — `MediaController` wrapper (play, pause, seek, queue)
- `PlayerViewModel.kt` — Exposes `PlayerState` to UI
- `NowPlayingScreen.kt` — Full-screen player UI
- `MiniPlayer.kt` — Persistent bottom bar player

**Player capabilities:**
- Foreground service with system media notification
- Lock screen / notification controls
- Queue management (up to 200 songs)
- Repeat modes: OFF / ONE / ALL
- Shuffle support
- Crossfade support (configurable duration)
- Volume normalisation
- Equalizer presets

---

## 🗄️ Local Database — Room

> **Version:** `2.6.1`

| Artifact | Role |
|---|---|
| `androidx.room:room-runtime` | SQLite abstraction layer |
| `androidx.room:room-ktx` | Kotlin coroutines + Flow support |
| `androidx.room:room-compiler` (KSP) | DAO and entity code generation |

**Database:** `AppDatabase` (version 1)

**Entities & DAOs:**

| Entity | Table | DAO | Description |
|---|---|---|---|
| `RecentlyPlayedEntity` | `recently_played` | `RecentlyPlayedDao` | Last 50 played songs |
| `DownloadedSongEntity` | `downloaded_songs` | `DownloadedSongDao` | Offline downloaded tracks |
| `CachedSongEntity` | `cached_songs` | `CachedSongDao` | Temporary Firestore song cache |

---

## 🖼️ Image Loading — Coil

> **Version:** `2.7.0`

- **Artifact:** `io.coil-kt:coil-compose`
- **Purpose:** Async image loading in Compose via `AsyncImage` composable
- **Use cases:** Song covers, album art, artist photos, user profile pictures
- **Source:** Firebase Storage URLs

---

## ⚙️ Background Work — WorkManager

> **Version:** `2.10.0`

| Artifact | Role |
|---|---|
| `androidx.work:work-runtime-ktx` | Deferrable, constraint-aware background tasks |
| `androidx.hilt:hilt-work:1.2.0` | Hilt injection into Workers |

**Workers:**
- `DownloadWorker` — Downloads songs from Firebase Storage to local device storage
  - Tag: `song_download`
  - Constraint: Network required
  - Handles progress notification

---

## 🔄 Kotlin Coroutines

> **Version:** `1.9.0`

| Artifact | Purpose |
|---|---|
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Main-thread dispatcher, `lifecycleScope` |
| `org.jetbrains.kotlinx:kotlinx-coroutines-play-services` | `.await()` extension for Firebase Tasks |

Used throughout repositories and ViewModels for non-blocking Firebase and database operations.

---

## 🎭 Accompanist

> **Version:** `0.36.0`

| Artifact | Purpose |
|---|---|
| `com.google.accompanist:accompanist-permissions` | Runtime permission request UI in Compose |
| `com.google.accompanist:accompanist-systemuicontroller` | Dynamic status/navigation bar color theming |

**Use cases:**
- Storage / audio permissions for file access and downloads
- Edge-to-edge immersive player UI

---

## ✨ Lottie Animations

> **Version:** `6.6.0`

- **Artifact:** `com.airbnb.android:lottie-compose`
- **Purpose:** JSON-based vector animations in Compose
- **Use cases:** Splash screen animation, loading states, empty state illustrations

---

## 📊 Vico Charts

> **Version:** `1.15.0`

| Artifact | Purpose |
|---|---|
| `com.patrykandpatrick.vico:compose` | Chart rendering in Compose |
| `com.patrykandpatrick.vico:compose-m3` | Material 3 themed chart styles |

**Use cases:**
- Artist/Admin dashboard analytics (stream counts, play trends)
- Visualising listening statistics

---

## 🧭 Navigation

> **Version:** `2.8.5`

- **Artifact:** `androidx.navigation:navigation-compose`
- **Purpose:** Type-safe in-app navigation graph

**Deep link scheme:** `mymusic://`

| Deep Link | Target |
|---|---|
| `mymusic://song` | Song detail / player |
| `mymusic://playlist` | Playlist screen |
| `mymusic://artist` | Artist profile |
| `mymusic://album` | Album detail |

**Screens / Features:**
`auth` · `home` · `search` · `library` · `player` · `playlist` · `album` · `artist` · `download` · `upload` · `settings` · `admin` · `moderator` · `dashboard`

---

## 🧪 Testing

| Artifact | Scope | Purpose |
|---|---|---|
| `junit:junit:4.13.2` | `testImplementation` | Unit tests |
| `androidx.test.ext:junit:1.2.1` | `androidTestImplementation` | Instrumentation tests |
| `androidx.test.espresso:espresso-core:3.6.1` | `androidTestImplementation` | UI interaction tests |
| `androidx.compose.ui:ui-test-junit4` | `androidTestImplementation` | Compose UI tests |
| `androidx.compose.ui:ui-test-manifest` | `debugImplementation` | Compose test manifest |

---

## 🏗️ Architecture Overview

```
┌────────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                      │
│  Screens · ViewModels · Navigation · Lottie · Vico · Coil      │
│  Accompanist · Material 3                                       │
└────────────────────┬───────────────────────────────────────────┘
                     │ StateFlow / collectAsStateWithLifecycle
┌────────────────────▼───────────────────────────────────────────┐
│                      Domain Layer                              │
│  Models · Result<T> · PlayerState · Business Logic             │
└─────────────┬──────────────────────────────┬───────────────────┘
              │                              │
┌─────────────▼──────────┐   ┌──────────────▼────────────────────┐
│   Remote Data Layer    │   │       Local Data Layer             │
│  Firebase Auth         │   │  Room Database (AppDatabase)       │
│  Cloud Firestore       │   │  ├─ recently_played               │
│  Firebase Storage      │   │  ├─ downloaded_songs              │
│  Firebase Analytics    │   │  └─ cached_songs                  │
└────────────────────────┘   └────────────────────────────────────┘
                                           │
              ┌────────────────────────────▼───────────────────────┐
              │              Background Services                    │
              │  Media3 MusicService (foreground playback)         │
              │  WorkManager DownloadWorker                        │
              │  Hilt DI (SingletonComponent)                      │
              └────────────────────────────────────────────────────┘
```

---

## 📋 Version Summary

| Integration | Version |
|---|---|
| Android Gradle Plugin | `8.7.3` |
| Kotlin | `2.0.21` |
| KSP | `2.0.21-1.0.28` |
| Compose BOM | `2024.12.01` |
| Hilt | `2.52` |
| Firebase BOM | `33.7.0` |
| Play Services Auth | `21.3.0` |
| Media3 / ExoPlayer | `1.5.0` |
| Room | `2.6.1` |
| WorkManager | `2.10.0` |
| Coil | `2.7.0` |
| Kotlin Coroutines | `1.9.0` |
| Accompanist | `0.36.0` |
| Lottie | `6.6.0` |
| Vico Charts | `1.15.0` |
| Navigation Compose | `2.8.5` |
| Lifecycle | `2.8.7` |
| Core KTX | `1.15.0` |

---

*Last updated: September 2026 · MyMusic v1.0*
