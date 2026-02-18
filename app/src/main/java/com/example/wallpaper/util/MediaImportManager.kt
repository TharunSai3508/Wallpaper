package com.example.wallpaper.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.wallpaper.data.MediaType
import com.example.wallpaper.data.WallpaperMediaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MediaImportManager(private val context: Context) {

    private val wallpaperDir by lazy { File(context.filesDir, "wallpapers").apply { mkdirs() } }
    private val thumbsDir by lazy { File(context.filesDir, "thumbnails").apply { mkdirs() } }

    suspend fun import(uriList: List<Uri>): List<WallpaperMediaEntity> = withContext(Dispatchers.IO) {
        uriList.mapNotNull { uri ->
            val mime = context.contentResolver.getType(uri) ?: return@mapNotNull null
            val mediaType = resolveMediaType(mime) ?: return@mapNotNull null

            val extension = extensionFromMime(mime)
            val fileName = "${UUID.randomUUID()}.$extension"
            val mediaFile = File(wallpaperDir, fileName)
            copyUri(context.contentResolver, uri, mediaFile)

            val thumbFile = File(thumbsDir, "${UUID.randomUUID()}.jpg")
            generateThumbnail(mediaFile, mediaType, thumbFile)

            WallpaperMediaEntity(
                filePath = mediaFile.absolutePath,
                mediaType = mediaType,
                thumbnailPath = thumbFile.absolutePath,
                dateAddedEpochMs = System.currentTimeMillis()
            )
        }
    }

    fun deleteFiles(entity: WallpaperMediaEntity) {
        File(entity.filePath).delete()
        File(entity.thumbnailPath).delete()
    }

    private fun resolveMediaType(mime: String): MediaType? = when {
        mime.startsWith("video/") -> MediaType.VIDEO
        mime == "image/gif" -> MediaType.GIF
        mime.startsWith("image/") -> MediaType.IMAGE
        else -> null
    }

    private fun extensionFromMime(mime: String): String = when (mime) {
        "image/gif" -> "gif"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "video/mp4" -> "mp4"
        "video/3gpp" -> "3gp"
        else -> mime.substringAfter('/').ifBlank { "bin" }
    }

    private fun copyUri(contentResolver: ContentResolver, uri: Uri, outFile: File) {
        contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(outFile).use { output ->
                requireNotNull(input) { "Unable to open selected file" }
                input.copyTo(output)
            }
        }
    }

    private fun generateThumbnail(mediaFile: File, mediaType: MediaType, thumbFile: File) {
        val bitmap = when (mediaType) {
            MediaType.VIDEO -> videoFrame(mediaFile)
            MediaType.IMAGE, MediaType.GIF -> BitmapFactory.decodeFile(mediaFile.absolutePath)
        } ?: Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)

        FileOutputStream(thumbFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
        }
    }

    private fun videoFrame(file: File): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } finally {
            retriever.release()
        }
    }
}
