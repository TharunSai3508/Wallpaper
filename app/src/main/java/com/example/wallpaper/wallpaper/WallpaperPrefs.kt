package com.example.wallpaper.wallpaper

import android.content.Context

class WallpaperPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)

    fun setVideoPath(path: String) = prefs.edit().putString(KEY_VIDEO_PATH, path).apply()
    fun getVideoPath(): String? = prefs.getString(KEY_VIDEO_PATH, null)

    fun setGifPath(path: String) = prefs.edit().putString(KEY_GIF_PATH, path).apply()
    fun getGifPath(): String? = prefs.getString(KEY_GIF_PATH, null)

    private companion object {
        const val KEY_VIDEO_PATH = "video_path"
        const val KEY_GIF_PATH = "gif_path"
    }
}
