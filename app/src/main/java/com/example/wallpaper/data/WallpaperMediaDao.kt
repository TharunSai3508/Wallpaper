package com.example.wallpaper.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WallpaperMediaDao {
    @Query("SELECT * FROM wallpaper_media ORDER BY dateAddedEpochMs DESC")
    fun pagingSource(): PagingSource<Int, WallpaperMediaEntity>

    @Query("SELECT * FROM wallpaper_media WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WallpaperMediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WallpaperMediaEntity>)

    @Delete
    suspend fun delete(item: WallpaperMediaEntity)
}
