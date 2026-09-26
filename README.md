# 🎵 MyMusic — Android Music Streaming App

MyMusic is an Android music streaming app built for independent artists to upload and share their music, and for listeners to discover, stream, and download tracks — inspired by platforms like Spotify and Gaana, built from scratch with a modern Android stack.

## ✨ Features

- 🔐 User authentication (Email/Password + Google Sign-In via Firebase Auth)
- 🎧 Full-featured audio player — play/pause, seek, queue, repeat, shuffle, crossfade
- 📱 Lock-screen and notification media controls (Media3/ExoPlayer)
- 📤 Artist upload flow — independent artists can upload tracks with cover art and metadata
- ✅ Content moderation system — uploads reviewed before going public
- 📋 Custom playlists — create, edit, and share
- ⬇️ Offline downloads — save tracks for offline listening
- 🕒 Recently played + liked songs tracking
- 📊 Artist dashboard with play-count analytics

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| Dependency Injection | Hilt |
| Backend | Firebase (Auth, Firestore, Storage, Analytics) |
| Media Playback | Media3 / ExoPlayer |
| Local Database | Room |
| Image Loading | Coil |
| Background Work | WorkManager |
| Navigation | Navigation Compose |

## 📸 Screenshots

| Home | Now Playing | Artist Upload |
|---|---|---|
<img width="590" height="1280" alt="start" src="https://github.com/user-attachments/assets/a6c76c7a-2112-4126-ad83-0579001b3b5b" />

---

## 🎬 Demo Video



## 📂 Project Structure

```
app/
 ├── data/           # Repositories, Firebase data sources
 ├── di/             # Hilt modules
 ├── domain/         # Models, business logic
 ├── ui/
 │   ├── auth/
 │   ├── home/
 │   ├── player/
 │   ├── upload/
 │   └── playlist/
 └── service/        # MusicService (Media3 foreground service)
```

---

## 📄 License & Content Policy

All music on MyMusic is uploaded by independent artists or sourced from royalty-free/Creative Commons libraries. See [LICENSE](LICENSE) and the in-app Artist Upload Agreement for details.

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

---

## 📬 Contact

Built by Chitrit Raaj Chauhan — chitrittech@gmail.com — www.linkedin.com/in/chitrit-raaj-chauhan-79a46924a
