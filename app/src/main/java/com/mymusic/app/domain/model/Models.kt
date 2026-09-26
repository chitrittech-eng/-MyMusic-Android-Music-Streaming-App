package com.mymusic.app.domain.model

data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "listener",
    val photoUrl: String? = null,
    val bio: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val createdAt: Long = 0L
)

// ─── License Types ──────────────────────────────────────────────────────────

enum class LicenseType(val value: String) {
    ARTIST_OWNED("artist_owned"),
    CREATIVE_COMMONS("creative_commons"),
    ROYALTY_FREE_LICENSED("royalty_free_licensed"),
    PUBLIC_DOMAIN("public_domain");

    companion object {
        fun fromValue(value: String): LicenseType =
            entries.firstOrNull { it.value == value } ?: ARTIST_OWNED
    }
}

data class Song(
    val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val albumId: String? = null,
    val albumTitle: String? = null,
    val audioUrl: String = "",
    val coverUrl: String? = null,
    val duration: Long = 0L,
    val genre: String = "",
    val lyrics: String = "",
    val plays: Long = 0L,
    val likes: Long = 0L,
    val uploadedAt: Long = 0L,
    // Legacy boolean — kept for backward compat; prefer approvalStatus
    val isApproved: Boolean = false,
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    // ── Rights & Licensing ──────────────────────────────────────────────────
    val licenseType: String = LicenseType.ARTIST_OWNED.value,
    val rightsConfirmed: Boolean = false,
    val rightsConfirmedAt: Long? = null,   // epoch millis (Timestamp serialised as Long by Firestore)
    val agreementVersion: String = "",
    val licenseSource: String? = null,     // e.g. "Jamendo" — null for artist-owned
    val approvalStatus: String = "pending" // "pending" | "approved" | "rejected"
)

data class Album(
    val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val coverUrl: String? = null,
    val songs: List<String> = emptyList(),
    val genre: String = "",
    val releaseDate: Long = 0L,
    val totalPlays: Long = 0L
)

data class Playlist(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val coverUrl: String? = null,
    val songs: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val followers: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val description: String = ""
)

data class Artist(
    val id: String = "",
    val name: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val verified: Boolean = false,
    val totalStreams: Long = 0L,
    val monthlyListeners: Long = 0L,
    val genres: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    // "independent" | "licensed_partner" — future-proofs aggregator catalog support
    val sourceType: String = "independent"
)

data class Category(
    val id: String = "",
    val name: String = "",
    val color: String = "#1DB954",
    val imageUrl: String? = null,
    val songIds: List<String> = emptyList()
)

data class Report(
    val id: String = "",
    val reportedBy: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val reason: String = "",
    val status: String = "pending",
    val createdAt: Long = 0L
)

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val targetId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)

enum class RepeatMode { OFF, ONE, ALL }

data class PlayerState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffled: Boolean = false,
    val isBuffering: Boolean = false
)

// ─── Agreement Acceptance (immutable audit log) ──────────────────────────────

data class AgreementAcceptance(
    val id: String = "",
    val userId: String = "",
    val songId: String = "",
    val agreementVersion: String = "",
    val acceptedAt: Long = 0L,
    val agreementTextHash: String = "" // SHA-256 of the agreement text shown to the user
)

// ─── Result ──────────────────────────────────────────────────────────────────

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
