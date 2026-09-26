package com.mymusic.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.Playlist
import com.mymusic.app.domain.model.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val playlistsRef = firestore.collection(Constants.COLLECTION_PLAYLISTS)

    fun getUserPlaylistsFlow(userId: String): Flow<List<Playlist>> = callbackFlow {
        val listener = playlistsRef
            .whereEqualTo("ownerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val playlists = snap?.documents?.mapNotNull {
                    it.toObject(Playlist::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(playlists)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getPlaylistById(playlistId: String): Result<Playlist> = try {
        val snap = playlistsRef.document(playlistId).get().await()
        val playlist = snap.toObject(Playlist::class.java)?.copy(id = snap.id)
        if (playlist != null) Result.Success(playlist) else Result.Error("Playlist not found")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get playlist", e)
    }

    suspend fun createPlaylist(playlist: Playlist): Result<String> = try {
        val ref = playlistsRef.document()
        ref.set(playlist.copy(id = ref.id, createdAt = System.currentTimeMillis())).await()
        Result.Success(ref.id)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to create playlist", e)
    }

    suspend fun updatePlaylist(playlistId: String, updates: Map<String, Any>): Result<Unit> = try {
        playlistsRef.document(playlistId).update(updates).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update playlist", e)
    }

    suspend fun deletePlaylist(playlistId: String): Result<Unit> = try {
        playlistsRef.document(playlistId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to delete playlist", e)
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String): Result<Unit> = try {
        playlistsRef.document(playlistId).update("songs", FieldValue.arrayUnion(songId)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to add song", e)
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> = try {
        playlistsRef.document(playlistId).update("songs", FieldValue.arrayRemove(songId)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to remove song", e)
    }

    suspend fun followPlaylist(userId: String, playlistId: String): Result<Unit> = try {
        playlistsRef.document(playlistId).update("followers", FieldValue.arrayUnion(userId)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to follow playlist", e)
    }

    suspend fun unfollowPlaylist(userId: String, playlistId: String): Result<Unit> = try {
        playlistsRef.document(playlistId).update("followers", FieldValue.arrayRemove(userId)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to unfollow playlist", e)
    }

    suspend fun searchPlaylists(query: String): Result<List<Playlist>> = try {
        val snap = playlistsRef
            .whereEqualTo("isPublic", true)
            .orderBy("name")
            .startAt(query)
            .endAt(query + "")
            .limit(20)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Playlist::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to search playlists", e)
    }
}
