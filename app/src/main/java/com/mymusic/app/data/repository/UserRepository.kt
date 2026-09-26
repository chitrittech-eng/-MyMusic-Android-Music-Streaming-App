package com.mymusic.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.Result
import com.mymusic.app.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val usersRef = firestore.collection(Constants.COLLECTION_USERS)

    val currentUserId: String? get() = auth.currentUser?.uid

    fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { trySend(null); close(); return@callbackFlow }
        val listener = usersRef.document(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.toObject(User::class.java)?.copy(id = snap.id))
        }
        awaitClose { listener.remove() }
    }

    suspend fun getUserById(userId: String): Result<User> = try {
        val snap = usersRef.document(userId).get().await()
        val user = snap.toObject(User::class.java)?.copy(id = snap.id)
        if (user != null) Result.Success(user) else Result.Error("User not found")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get user", e)
    }

    suspend fun createUser(user: User): Result<Unit> = try {
        usersRef.document(user.id).set(user).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to create user", e)
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> = try {
        usersRef.document(userId).update(updates).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update user", e)
    }

    suspend fun followUser(currentUserId: String, targetUserId: String): Result<Unit> = try {
        firestore.runBatch { batch ->
            batch.update(usersRef.document(currentUserId), "following",
                com.google.firebase.firestore.FieldValue.arrayUnion(targetUserId))
            batch.update(usersRef.document(targetUserId), "followers",
                com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId))
        }.await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to follow user", e)
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<Unit> = try {
        firestore.runBatch { batch ->
            batch.update(usersRef.document(currentUserId), "following",
                com.google.firebase.firestore.FieldValue.arrayRemove(targetUserId))
            batch.update(usersRef.document(targetUserId), "followers",
                com.google.firebase.firestore.FieldValue.arrayRemove(currentUserId))
        }.await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to unfollow user", e)
    }

    suspend fun getAllUsers(): Result<List<User>> = try {
        val snap = usersRef.get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(User::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get users", e)
    }

    suspend fun updateUserRole(userId: String, role: String): Result<Unit> =
        updateUser(userId, mapOf("role" to role))

    suspend fun banUser(userId: String): Result<Unit> =
        updateUser(userId, mapOf("banned" to true))
}
