package com.mymusic.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.Artist
import com.mymusic.app.domain.model.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val artistsRef = firestore.collection(Constants.COLLECTION_ARTISTS)

    suspend fun getArtistById(artistId: String): Result<Artist> = try {
        val snap = artistsRef.document(artistId).get().await()
        val artist = snap.toObject(Artist::class.java)?.copy(id = snap.id)
        if (artist != null) Result.Success(artist) else Result.Error("Artist not found")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get artist", e)
    }

    suspend fun createOrUpdateArtist(artist: Artist): Result<Unit> = try {
        artistsRef.document(artist.id).set(artist).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update artist", e)
    }

    suspend fun updateArtist(artistId: String, updates: Map<String, Any>): Result<Unit> = try {
        artistsRef.document(artistId).update(updates).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update artist", e)
    }

    suspend fun followArtist(userId: String, artistId: String): Result<Unit> = try {
        artistsRef.document(artistId).update("followers", FieldValue.arrayUnion(userId)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to follow artist", e)
    }

    suspend fun unfollowArtist(userId: String, artistId: String): Result<Unit> = try {
        artistsRef.document(artistId).update("followers", FieldValue.arrayRemove(userId)).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to unfollow artist", e)
    }

    suspend fun searchArtists(query: String): Result<List<Artist>> = try {
        val snap = artistsRef
            .orderBy("name")
            .startAt(query)
            .endAt(query + "")
            .limit(20)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Artist::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to search artists", e)
    }

    suspend fun verifyArtist(artistId: String): Result<Unit> =
        updateArtist(artistId, mapOf("verified" to true))

    suspend fun incrementStreams(artistId: String): Result<Unit> = try {
        artistsRef.document(artistId).update(
            "totalStreams", FieldValue.increment(1),
            "monthlyListeners", FieldValue.increment(1)
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to increment streams", e)
    }

    suspend fun getAllArtists(): Result<List<Artist>> = try {
        val snap = artistsRef.orderBy("totalStreams").get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Artist::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get artists", e)
    }
}
