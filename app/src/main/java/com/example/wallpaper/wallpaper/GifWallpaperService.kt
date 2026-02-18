package com.example.wallpaper.wallpaper

import android.graphics.Canvas
import android.graphics.Movie
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.io.File

class GifWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = GifEngine()

    inner class GifEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var movie: Movie? = null
        private var startMs: Long = 0

        private val drawFrame = object : Runnable {
            override fun run() {
                draw()
                handler.postDelayed(this, 16)
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            val path = WallpaperPrefs(applicationContext).getGifPath() ?: return
            if (!File(path).exists()) return
            movie = Movie.decodeFile(path)
            startMs = System.currentTimeMillis()
            handler.post(drawFrame)
        }

        private fun draw() {
            val holder = surfaceHolder
            val canvas: Canvas = holder.lockCanvas() ?: return
            try {
                val gif = movie ?: return
                val duration = if (gif.duration() == 0) 1000 else gif.duration()
                val time = ((System.currentTimeMillis() - startMs) % duration).toInt()
                gif.setTime(time)
                val scaleX = canvas.width / gif.width().toFloat()
                val scaleY = canvas.height / gif.height().toFloat()
                canvas.scale(scaleX, scaleY)
                gif.draw(canvas, 0f, 0f)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            handler.removeCallbacks(drawFrame)
            if (visible) handler.post(drawFrame)
        }

        override fun onDestroy() {
            handler.removeCallbacks(drawFrame)
            super.onDestroy()
        }
    }
}
