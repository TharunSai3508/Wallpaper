package com.example.wallpaper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters



@Database(entities = [WallpaperMediaEntity::class], version = 1, exportSchema = false)
@TypeConverters(MediaTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wallpaperMediaDao(): WallpaperMediaDao
}

class MediaTypeConverters {
    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)
}
