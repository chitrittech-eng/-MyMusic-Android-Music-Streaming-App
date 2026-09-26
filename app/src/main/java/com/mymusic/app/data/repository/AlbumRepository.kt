package com.mymusic.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.Album
import com.mymusic.app.domain.model.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val albumsRef = firestore.collection(Constants.COLLECTION_ALBUMS)

    suspend fun getAlbumById(albumId: String): Result<Album> = try {
        val snap = albumsRef.document(albumId).get().await()
        val album = snap.toObject(Album::class.java)?.copy(id = snap.id)
        if (album != null) Result.Success(album) else Result.Error("Album not found")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get album", e)
    }

    suspend fun getAlbumsByArtist(artistId: String): Result<List<Album>> = try {
        val snap = albumsRef
            .whereEqualTo("artistId", artistId)
            .orderBy("releaseDate", Query.Direction.DESCENDING)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Album::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get albums", e)
    }

    suspend fun createAlbum(album: Album): Result<String> = try {
        val ref = albumsRef.document()
        ref.set(album.copy(id = ref.id)).await()
        Result.Success(ref.id)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to create album", e)
    }

    suspend fun updateAlbum(albumId: String, updates: Map<String, Any>): Result<Unit> = try {
        albumsRef.document(albumId).update(updates).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update album", e)
    }

    suspend fun deleteAlbum(albumId: String): Result<Unit> = try {
        albumsRef.document(albumId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to delete album", e)
    }

    suspend fun searchAlbums(query: String): Result<List<Album>> = try {
        val snap = albumsRef
            .orderBy("title")
            .startAt(query)
            .endAt(query + "")
            .limit(20)
            .get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Album::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to search albums", e)
    }
}
