package com.example.wallpaper.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.wallpaper.data.MediaType
import com.example.wallpaper.data.WallpaperMediaEntity
import java.io.File

class WallpaperSetter(private val context: Context) {

    fun setWallpaper(activity: Activity, item: WallpaperMediaEntity) {
        when (item.mediaType) {
            MediaType.IMAGE -> setStatic(item.filePath)
            MediaType.GIF -> setGifLive(activity, item.filePath)
            MediaType.VIDEO -> setVideoLive(activity, item.filePath)
        }
    }

    private fun setStatic(filePath: String) {
        File(filePath).inputStream().use { input ->
            WallpaperManager.getInstance(context).setStream(input)
        }
    }

    private fun setGifLive(activity: Activity, filePath: String) {
        WallpaperPrefs(context).setGifPath(filePath)
        launchLiveWallpaper(activity, GifWallpaperService::class.java)
    }

    private fun setVideoLive(activity: Activity, filePath: String) {
        WallpaperPrefs(context).setVideoPath(filePath)
        launchLiveWallpaper(activity, VideoWallpaperService::class.java)
    }

    private fun launchLiveWallpaper(activity: Activity, service: Class<*>) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(activity, service)
                )
            }
        } else {
            Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
        }
        activity.startActivity(intent)
    }
}
