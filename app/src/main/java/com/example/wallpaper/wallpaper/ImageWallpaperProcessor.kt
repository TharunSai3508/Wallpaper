package com.example.wallpaper.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ImageWallpaperProcessor(private val context: Context) {

    fun process(filePath: String, options: WallpaperEditOptions): Bitmap {
        val source = BitmapFactory.decodeFile(filePath)
            ?: throw IllegalArgumentException("Unable to decode image: $filePath")

        val targetSize = resolveTargetSize(source.width, source.height, options.resolutionPreset)
        val targetW = max(1, targetSize.first)
        val targetH = max(1, targetSize.second)

        val output = createBitmap(targetW, targetH)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        if (options.fitMode == FitMode.STRETCH) {
            canvas.drawBitmap(
                source,
                null,
                android.graphics.Rect(0, 0, targetW, targetH),
                paint
            )
            return output
        }

        val scaleBase = when (options.fitMode) {
            FitMode.CENTER_CROP -> max(targetW / source.width.toFloat(), targetH / source.height.toFloat())
            FitMode.FIT_INSIDE -> min(targetW / source.width.toFloat(), targetH / source.height.toFloat())
            FitMode.STRETCH -> 1f
        }

        val scale = (scaleBase * options.zoom).coerceAtLeast(0.1f)
        val drawW = (source.width * scale).roundToInt()
        val drawH = (source.height * scale).roundToInt()

        val extraX = abs(targetW - drawW) / 2f
        val extraY = abs(targetH - drawH) / 2f
        val left = ((targetW - drawW) / 2f) + (extraX * options.panX.coerceIn(-1f, 1f))
        val top = ((targetH - drawH) / 2f) + (extraY * options.panY.coerceIn(-1f, 1f))

        canvas.drawBitmap(
            source,
            null,
            android.graphics.Rect(
                left.roundToInt(),
                top.roundToInt(),
                (left + drawW).roundToInt(),
                (top + drawH).roundToInt()
            ),
            paint
        )

        return output
    }

    private fun resolveTargetSize(srcW: Int, srcH: Int, preset: ResolutionPreset): Pair<Int, Int> {
        return when (preset) {
            ResolutionPreset.ORIGINAL -> srcW to srcH
            ResolutionPreset.FHD -> 1080 to 2400
            ResolutionPreset.QHD -> 1440 to 3120
            ResolutionPreset.SCREEN -> {
                val wm = WallpaperManager.getInstance(context)
                val w = wm.desiredMinimumWidth.takeIf { it > 0 } ?: context.resources.displayMetrics.widthPixels
                val h = wm.desiredMinimumHeight.takeIf { it > 0 } ?: context.resources.displayMetrics.heightPixels
                w to h
            }
        }
    }
}
