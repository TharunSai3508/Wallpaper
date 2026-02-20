package com.example.wallpaper.domain

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.Room
import com.example.wallpaper.data.AppDatabase
import com.example.wallpaper.data.WallpaperMediaEntity
import com.example.wallpaper.util.MediaImportManager
import kotlinx.coroutines.flow.Flow

class WallpaperRepository private constructor(
    private val database: AppDatabase,
    private val importManager: MediaImportManager
) {

    fun mediaPager(): Flow<PagingData<WallpaperMediaEntity>> {
        return Pager(PagingConfig(pageSize = 30, prefetchDistance = 10, enablePlaceholders = false)) {
            database.wallpaperMediaDao().pagingSource()
        }.flow
    }

    suspend fun importFromUris(uris: List<Uri>) {
        val entities = importManager.import(uris)
        if (entities.isNotEmpty()) {
            database.wallpaperMediaDao().insertAll(entities)
        }
    }

    suspend fun getById(id: Long): WallpaperMediaEntity? = database.wallpaperMediaDao().getById(id)

    suspend fun delete(item: WallpaperMediaEntity) {
        database.wallpaperMediaDao().delete(item)
        importManager.deleteFiles(item)
    }

    companion object {
        @Volatile
        private var instance: WallpaperRepository? = null

        fun get(context: Context): WallpaperRepository {
            return instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }
        }

        private fun build(context: Context): WallpaperRepository {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "wallpaper-db").build()
            return WallpaperRepository(db, MediaImportManager(context))
        }
    }
}
