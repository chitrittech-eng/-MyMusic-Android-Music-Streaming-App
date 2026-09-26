package com.mymusic.app.core.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mymusic.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideRecentlyPlayedDao(db: AppDatabase) = db.recentlyPlayedDao()

    @Provides
    fun provideDownloadedSongDao(db: AppDatabase) = db.downloadedSongDao()

    @Provides
    fun provideCachedSongDao(db: AppDatabase) = db.cachedSongDao()
}
