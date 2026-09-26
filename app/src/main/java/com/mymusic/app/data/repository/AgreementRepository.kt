package com.mymusic.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.AgreementAcceptance
import com.mymusic.app.domain.model.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgreementRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val acceptancesRef = firestore.collection(Constants.COLLECTION_AGREEMENT_ACCEPTANCES)

    /**
     * Writes an immutable acceptance record.
     * Call this AFTER the [Song] doc has been created — the [songId] must exist.
     *
     * @param userId           UID of the authenticated user accepting the agreement
     * @param songId           Firestore document ID of the newly created song
     * @param agreementVersion Version tag of the agreement text (e.g. "v1.0")
     * @param textHash         SHA-256 hex of the agreement text shown on screen; defaults to the
     *                         pre-computed constant for [agreementVersion] == "v1.0"
     */
    suspend fun recordAcceptance(
        userId: String,
        songId: String,
        agreementVersion: String,
        textHash: String = Constants.AGREEMENT_V1_TEXT_HASH
    ): Result<String> = try {
        val ref = acceptancesRef.document()
        val acceptance = AgreementAcceptance(
            id = ref.id,
            userId = userId,
            songId = songId,
            agreementVersion = agreementVersion,
            acceptedAt = System.currentTimeMillis(),
            agreementTextHash = textHash
        )
        ref.set(acceptance).await()
        Result.Success(ref.id)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to record agreement acceptance", e)
    }

    /**
     * Returns all acceptance records for a given song.
     * Intended for moderator / admin review only — the Firestore rule enforces this.
     */
    suspend fun getAcceptancesForSong(songId: String): Result<List<AgreementAcceptance>> = try {
        val snap = acceptancesRef
            .whereEqualTo("songId", songId)
            .get().await()
        Result.Success(
            snap.documents.mapNotNull { it.toObject(AgreementAcceptance::class.java)?.copy(id = it.id) }
        )
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to fetch acceptance records", e)
    }
}
