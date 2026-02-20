package com.example.wallpaper.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import com.example.wallpaper.data.MediaType
import com.example.wallpaper.data.WallpaperMediaEntity

class WallpaperSetter(private val context: Context) {

    private val imageProcessor by lazy { ImageWallpaperProcessor(context) }

    fun setWallpaper(activity: Activity, item: WallpaperMediaEntity) {
        setWallpaper(activity, item, WallpaperEditOptions())
    }

    fun setWallpaper(activity: Activity, item: WallpaperMediaEntity, options: WallpaperEditOptions) {
        when (item.mediaType) {
            MediaType.IMAGE -> {
                val bitmap = imageProcessor.process(item.filePath, options)
                applyBitmapToTarget(bitmap, options.screenTarget)
            }

            MediaType.GIF -> {
                if (options.screenTarget == ScreenTarget.BOTH) {
                    setGifLive(activity, item.filePath)
                } else {
                    val bitmap = BitmapFactory.decodeFile(item.filePath)
                        ?: throw IllegalArgumentException("Unable to decode GIF ${item.filePath}")
                    applyBitmapToTarget(bitmap, options.screenTarget)
                }
            }

            MediaType.VIDEO -> {
                if (options.screenTarget == ScreenTarget.BOTH) {
                    setVideoLive(activity, item.filePath)
                } else {
                    val bitmap = videoFrame(item.filePath)
                        ?: throw IllegalArgumentException("Unable to extract video frame ${item.filePath}")
                    applyBitmapToTarget(bitmap, options.screenTarget)
                }
            }
        }
    }

    private fun applyBitmapToTarget(bitmap: Bitmap, target: ScreenTarget) {
        val manager = WallpaperManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (target) {
                ScreenTarget.BOTH -> manager.setBitmap(bitmap)
                ScreenTarget.HOME -> manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                ScreenTarget.LOCK -> manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            }
        } else {
            manager.setBitmap(bitmap)
        }
    }

    private fun videoFrame(filePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } finally {
            retriever.release()
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
