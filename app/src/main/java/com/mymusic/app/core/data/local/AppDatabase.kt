package com.mymusic.app.core.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entities ───────────────────────────────────────────────────────────────

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artistName: String,
    val coverUrl: String?,
    val audioUrl: String,
    val duration: Long,
    val genre: String,
    val playedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String?,
    val localAudioPath: String,
    val coverUrl: String?,
    val duration: Long,
    val genre: String,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_songs")
data class CachedSongEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val albumId: String?,
    val audioUrl: String,
    val coverUrl: String?,
    val duration: Long,
    val genre: String,
    val plays: Long,
    val likes: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

// ─── DAOs ────────────────────────────────────────────────────────────────────

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 50")
    fun getAll(): Flow<List<RecentlyPlayedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played WHERE songId = :songId")
    suspend fun delete(songId: String)

    @Query("DELETE FROM recently_played WHERE songId NOT IN (SELECT songId FROM recently_played ORDER BY playedAt DESC LIMIT 50)")
    suspend fun trimToLimit()
}

@Dao
interface DownloadedSongDao {
    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAll(): Flow<List<DownloadedSongEntity>>

    @Query("SELECT COUNT(*) FROM downloaded_songs WHERE songId = :songId")
    suspend fun exists(songId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadedSongEntity)

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun delete(songId: String)
}

@Dao
interface CachedSongDao {
    @Query("SELECT * FROM cached_songs WHERE songId = :songId")
    suspend fun getById(songId: String): CachedSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedSongEntity)

    @Query("DELETE FROM cached_songs WHERE cachedAt < :threshold")
    suspend fun clearOldCache(threshold: Long)
}

// ─── Database ────────────────────────────────────────────────────────────────

@Database(
    entities = [
        RecentlyPlayedEntity::class,
        DownloadedSongEntity::class,
        CachedSongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun downloadedSongDao(): DownloadedSongDao
    abstract fun cachedSongDao(): CachedSongDao
}
