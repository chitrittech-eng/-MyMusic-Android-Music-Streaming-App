package com.mymusic.app.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadAudio(songId: String, uri: Uri): Result<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_MUSIC}/$songId/audio.mp3")
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        Result.Success(url)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to upload audio", e)
    }

    suspend fun uploadSongCover(songId: String, uri: Uri): Result<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_COVERS_SONGS}/$songId/cover.jpg")
        ref.putFile(uri).await()
        Result.Success(ref.downloadUrl.await().toString())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to upload song cover", e)
    }

    suspend fun uploadAlbumCover(albumId: String, uri: Uri): Result<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_COVERS_ALBUMS}/$albumId/cover.jpg")
        ref.putFile(uri).await()
        Result.Success(ref.downloadUrl.await().toString())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to upload album cover", e)
    }

    suspend fun uploadPlaylistCover(playlistId: String, uri: Uri): Result<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_COVERS_PLAYLISTS}/$playlistId/cover.jpg")
        ref.putFile(uri).await()
        Result.Success(ref.downloadUrl.await().toString())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to upload playlist cover", e)
    }

    suspend fun uploadProfilePhoto(userId: String, uri: Uri): Result<String> = try {
        val ref = storage.reference.child("${Constants.STORAGE_PROFILES}/$userId/avatar.jpg")
        ref.putFile(uri).await()
        Result.Success(ref.downloadUrl.await().toString())
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to upload profile photo", e)
    }

    suspend fun deleteAudio(songId: String): Result<Unit> = try {
        storage.reference.child("${Constants.STORAGE_MUSIC}/$songId/audio.mp3").delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to delete audio", e)
    }
}
