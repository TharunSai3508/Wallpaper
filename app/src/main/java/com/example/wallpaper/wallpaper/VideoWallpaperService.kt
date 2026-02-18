package com.example.wallpaper.wallpaper

import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.io.File

class VideoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = VideoEngine()

    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            val path = WallpaperPrefs(applicationContext).getVideoPath() ?: return
            if (!File(path).exists()) return

            mediaPlayer = MediaPlayer().apply {
                setSurface(holder.surface)
                isLooping = true
                setVolume(0f, 0f)
                setDataSource(path)
                setOnPreparedListener { it.start() }
                prepareAsync()
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) mediaPlayer?.start() else mediaPlayer?.pause()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            mediaPlayer?.release()
            mediaPlayer = null
            super.onSurfaceDestroyed(holder)
        }
    }
}
