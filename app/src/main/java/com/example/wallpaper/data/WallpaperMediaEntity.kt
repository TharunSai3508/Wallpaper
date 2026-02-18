package com.example.wallpaper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpaper_media")
data class WallpaperMediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val mediaType: MediaType,
    val thumbnailPath: String,
    val dateAddedEpochMs: Long
)
