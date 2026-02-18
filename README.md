# Wallpaper

Compose-first Android wallpaper app that supports static images, GIF live wallpapers, and video live wallpapers.

## Highlights
- Kotlin + Jetpack Compose UI only (no XML layouts)
- Pinterest-style staggered media grid with lazy paging
- Import from any gallery/media provider via system chooser (`OpenMultipleDocuments`)
- Internal gallery storage (files copied into app private storage)
- Room metadata persistence
- Long-press actions: set wallpaper, delete, info
- Fullscreen preview for image/GIF/video content
- Live wallpaper services for GIF and video playback

## Tech stack
- Jetpack Compose + Material 3
- Room + Paging 3
- Coil (image/gif/video decoders)
- Media3 ExoPlayer (video previews)
