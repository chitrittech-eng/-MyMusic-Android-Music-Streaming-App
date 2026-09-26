package com.mymusic.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mymusic.app.core.common.Constants
import com.mymusic.app.domain.model.Category
import com.mymusic.app.domain.model.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val categoriesRef = firestore.collection(Constants.COLLECTION_CATEGORIES)

    suspend fun getCategories(): Result<List<Category>> = try {
        val snap = categoriesRef.get().await()
        Result.Success(snap.documents.mapNotNull { it.toObject(Category::class.java)?.copy(id = it.id) })
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to get categories", e)
    }

    suspend fun createCategory(category: Category): Result<String> = try {
        val ref = categoriesRef.document()
        ref.set(category.copy(id = ref.id)).await()
        Result.Success(ref.id)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to create category", e)
    }

    suspend fun updateCategory(categoryId: String, updates: Map<String, Any>): Result<Unit> = try {
        categoriesRef.document(categoryId).update(updates).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to update category", e)
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = try {
        categoriesRef.document(categoryId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Failed to delete category", e)
    }
}
