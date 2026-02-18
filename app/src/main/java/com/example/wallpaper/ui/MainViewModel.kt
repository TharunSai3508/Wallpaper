package com.example.wallpaper.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.wallpaper.data.WallpaperMediaEntity
import com.example.wallpaper.domain.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WallpaperRepository.get(application)

    val mediaPaging = repository.mediaPager().cachedIn(viewModelScope)

    private val _selected = MutableStateFlow<WallpaperMediaEntity?>(null)
    val selected: StateFlow<WallpaperMediaEntity?> = _selected.asStateFlow()

    fun import(uris: List<Uri>) {
        viewModelScope.launch {
            repository.importFromUris(uris)
        }
    }

    fun openItem(item: WallpaperMediaEntity) {
        _selected.value = item
    }

    fun closePreview() {
        _selected.value = null
    }

    fun delete(item: WallpaperMediaEntity) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
}
