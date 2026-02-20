package com.example.wallpaper.wallpaper

enum class FitMode {
    CENTER_CROP,
    FIT_INSIDE,
    STRETCH
}

enum class ResolutionPreset {
    SCREEN,
    ORIGINAL,
    FHD,
    QHD
}

enum class ScreenTarget {
    BOTH,
    HOME,
    LOCK
}

data class WallpaperEditOptions(
    val fitMode: FitMode = FitMode.CENTER_CROP,
    val zoom: Float = 1f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val resolutionPreset: ResolutionPreset = ResolutionPreset.SCREEN,
    val screenTarget: ScreenTarget = ScreenTarget.BOTH
)
