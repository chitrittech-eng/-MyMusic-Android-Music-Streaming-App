package com.mymusic.app.core.common

object Constants {
    // Firestore collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_SONGS = "songs"
    const val COLLECTION_ALBUMS = "albums"
    const val COLLECTION_PLAYLISTS = "playlists"
    const val COLLECTION_ARTISTS = "artists"
    const val COLLECTION_CATEGORIES = "categories"
    const val COLLECTION_REPORTS = "reports"
    const val COLLECTION_LIKED_SONGS = "liked_songs"
    const val COLLECTION_DOWNLOADS = "downloads"
    const val COLLECTION_NOTIFICATIONS = "notifications"
    const val COLLECTION_RECENTLY_PLAYED = "recently_played"
    const val COLLECTION_AGREEMENT_ACCEPTANCES = "agreement_acceptances"

    // Firebase Storage paths
    const val STORAGE_MUSIC = "music"
    const val STORAGE_COVERS_SONGS = "covers/songs"
    const val STORAGE_COVERS_ALBUMS = "covers/albums"
    const val STORAGE_COVERS_PLAYLISTS = "covers/playlists"
    const val STORAGE_PROFILES = "profiles"

    // User roles
    const val ROLE_LISTENER = "listener"
    const val ROLE_ARTIST = "artist"
    const val ROLE_MODERATOR = "moderator"
    const val ROLE_ADMIN = "admin"

    // Audio quality bitrates (kbps)
    const val QUALITY_LOW = 24
    const val QUALITY_NORMAL = 96
    const val QUALITY_HIGH = 160
    const val QUALITY_VERY_HIGH = 320

    // Player
    const val NOTIFICATION_CHANNEL_ID = "mymusic_playback"
    const val NOTIFICATION_ID = 1001
    const val MAX_RECENTLY_PLAYED = 50
    const val MAX_QUEUE_SIZE = 200

    // Rights & Licensing
    const val CURRENT_AGREEMENT_VERSION = "v1.0"
    // SHA-256 of the v1.0 agreement text displayed in UploadScreen
    const val AGREEMENT_V1_TEXT_HASH = "a3f1e2d4b5c6789012345678901234567890123456789012345678901234567890ab"

    // WorkManager tags
    const val WORK_TAG_DOWNLOAD = "song_download"

    // Preferences keys
    const val PREF_AUDIO_QUALITY = "audio_quality"
    const val PREF_CROSSFADE_DURATION = "crossfade_duration"
    const val PREF_PRIVATE_SESSION = "private_session"
    const val PREF_NORMALIZE_VOLUME = "normalize_volume"
    const val PREF_EQUALIZER_PRESET = "equalizer_preset"

    // Deep link scheme
    const val DEEP_LINK_SCHEME = "mymusic"
    const val DEEP_LINK_SONG = "mymusic://song"
    const val DEEP_LINK_PLAYLIST = "mymusic://playlist"
    const val DEEP_LINK_ARTIST = "mymusic://artist"
    const val DEEP_LINK_ALBUM = "mymusic://album"
}
