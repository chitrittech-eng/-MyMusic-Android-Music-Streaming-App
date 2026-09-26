package com.mymusic.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.Report
import com.mymusic.app.domain.model.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val reportsRef = firestore.collection(Constants.COLLECTION_REPORTS)

    suspend fun submitReport(report: Report): Result<Unit> = try {
        val ref = reportsRef.document()
        ref.set(report.copy(id = ref.id, createdAt = System.currentTimeMillis())).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to submit report", e)
    }

    suspend fun getPendingReports(): Result<List<Report>> = try {
        val snap = reportsRef.whereEqualTo("status", "pending").get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Report::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get reports", e)
    }

    suspend fun resolveReport(reportId: String, resolution: String): Result<Unit> = try {
        reportsRef.document(reportId).update(
            mapOf("status" to resolution, "resolvedAt" to System.currentTimeMillis())
        ).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to resolve report", e)
    }
}
