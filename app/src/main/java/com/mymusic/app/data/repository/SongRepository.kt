package com.mymusic.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.Song
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val songsRef = firestore.collection(Constants.COLLECTION_SONGS)

    fun getApprovedSongsFlow(): Flow<List<Song>> = callbackFlow {
        val listener = songsRef
            .whereEqualTo("isApproved", true)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val songs = snap?.documents?.mapNotNull {
                    it.toObject(Song::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(songs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getSongById(songId: String): Result<Song> = try {
        val snap = songsRef.document(songId).get().await()
        val song = snap.toObject(Song::class.java)?.copy(id = snap.id)
        if (song != null) Result.Success(song) else Result.Error("Song not found")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get song", e)
    }

    suspend fun getSongsByArtist(artistId: String): Result<List<Song>> = try {
        val snap = songsRef
            .whereEqualTo("artistId", artistId)
            .whereEqualTo("isApproved", true)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Song::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get artist songs", e)
    }

    suspend fun getSongsByAlbum(albumId: String): Result<List<Song>> = try {
        val snap = songsRef.whereEqualTo("albumId", albumId).get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Song::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get album songs", e)
    }

    suspend fun getNewReleases(limit: Long = 20): Result<List<Song>> = try {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val snap = songsRef
            .whereEqualTo("isApproved", true)
            .whereGreaterThan("uploadedAt", thirtyDaysAgo)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Song::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get new releases", e)
    }

    suspend fun getSongsByGenre(genre: String, limit: Long = 30): Result<List<Song>> = try {
        val snap = songsRef
            .whereEqualTo("genre", genre)
            .whereEqualTo("isApproved", true)
            .orderBy("plays", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Song::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get songs by genre", e)
    }

    suspend fun searchSongs(query: String): Result<List<Song>> = try {
        val snap = songsRef
            .whereEqualTo("isApproved", true)
            .orderBy("title")
            .startAt(query)
            .endAt(query + "")
            .limit(20)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Song::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to search songs", e)
    }

    suspend fun uploadSong(song: Song): Result<String> = try {
        val ref = songsRef.document()
        ref.set(song.copy(id = ref.id, uploadedAt = System.currentTimeMillis())).await()
        Result.Success(ref.id)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to upload song", e)
    }

    suspend fun incrementPlayCount(songId: String): Result<Unit> = try {
        songsRef.document(songId).update(
            "plays", com.google.firebase.firestore.FieldValue.increment(1)
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to increment play count", e)
    }

    /**
     * Approves a song for public listening.
     * Writes both [approvalStatus] (new string field) and the legacy [isApproved] boolean
     * so that old Firestore documents are backward-compatible during migration.
     *
     * Pre-condition: moderators should verify [Song.rightsConfirmed] == true before calling.
     */
    suspend fun approveSong(songId: String): Result<Unit> = try {
        songsRef.document(songId).update(
            mapOf(
                "approvalStatus" to "approved",
                "isApproved" to true
            )
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to approve song", e)
    }

    /** Rejects a song — sets approvalStatus to "rejected" without deleting the doc. */
    suspend fun rejectSong(songId: String): Result<Unit> = try {
        songsRef.document(songId).update(
            mapOf(
                "approvalStatus" to "rejected",
                "isApproved" to false
            )
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to reject song", e)
    }

    suspend fun deleteSong(songId: String): Result<Unit> = try {
        songsRef.document(songId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to delete song", e)
    }

    /**
     * Returns songs with [approvalStatus] == "pending".
     * Falls back to querying [isApproved] == false for legacy documents that pre-date
     * the rights schema migration.
     */
    suspend fun getPendingApprovals(): Result<List<Song>> = try {
        val snap = songsRef
            .whereEqualTo("approvalStatus", "pending")
            .orderBy("uploadedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Song::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get pending songs", e)
    }

    fun getLikedSongsFlow(userId: String): Flow<List<Song>> = callbackFlow {
        val likedRef = firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_LIKED_SONGS)
        val listener = likedRef.addSnapshotListener { snap, _ ->
            val songIds = snap?.documents?.map { it.id } ?: emptyList()
            trySend(songIds.map { Song(id = it) })
        }
        awaitClose { listener.remove() }
    }

    suspend fun likeSong(userId: String, songId: String): Result<Unit> = try {
        val likedRef = firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_LIKED_SONGS)
            .document(songId)
        likedRef.set(mapOf("likedAt" to System.currentTimeMillis())).await()
        songsRef.document(songId).update("likes", com.google.firebase.firestore.FieldValue.increment(1)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to like song", e)
    }

    suspend fun unlikeSong(userId: String, songId: String): Result<Unit> = try {
        val likedRef = firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_LIKED_SONGS)
            .document(songId)
        likedRef.delete().await()
        songsRef.document(songId).update("likes", com.google.firebase.firestore.FieldValue.increment(-1)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to unlike song", e)
    }

    suspend fun isLiked(userId: String, songId: String): Boolean = try {
        val doc = firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .collection(Constants.COLLECTION_LIKED_SONGS)
            .document(songId)
            .get().await()
        doc.exists()
    } catch (e: Exception) { false }
}
