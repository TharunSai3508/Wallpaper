package com.example.wallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.wallpaper.data.MediaType
import com.example.wallpaper.data.WallpaperMediaEntity
import com.example.wallpaper.ui.MainViewModel
import com.example.wallpaper.ui.VideoPreview
import com.example.wallpaper.wallpaper.FitMode
import com.example.wallpaper.wallpaper.ResolutionPreset
import com.example.wallpaper.wallpaper.ScreenTarget
import com.example.wallpaper.wallpaper.WallpaperEditOptions
import com.example.wallpaper.wallpaper.WallpaperSetter

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { WallpaperHome(viewModel) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WallpaperHome(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val wallpaperSetter = remember { WallpaperSetter(context) }
    var pendingSetItem by remember { mutableStateOf<WallpaperMediaEntity?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) viewModel.import(uris)
    }

    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val mediaItems = viewModel.mediaPaging.collectAsLazyPagingItems()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { importLauncher.launch(arrayOf("image/*", "video/*", "image/gif")) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 14.dp)) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Text("Import")
                }
            }
        }
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(160.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            items(mediaItems.itemSnapshotList.items, key = { it.id }) { item ->
                MediaGridItem(
                    item = item,
                    onTap = { viewModel.openItem(item) },
                    onSetWallpaper = { pendingSetItem = item },
                    onDelete = { viewModel.delete(item) }
                )
            }
        }

        selected?.let { item ->
            FullScreenPreview(
                item = item,
                onDismiss = viewModel::closePreview,
                onSetWallpaper = { pendingSetItem = item }
            )
        }

        pendingSetItem?.let { item ->
            WallpaperSetDialog(
                item = item,
                onDismiss = { pendingSetItem = null },
                onApply = { options ->
                    wallpaperSetter.setWallpaper(activity, item, options)
                    pendingSetItem = null
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaGridItem(
    item: WallpaperMediaEntity,
    onTap: () -> Unit,
    onSetWallpaper: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val dynamicHeight by rememberSaveable(item.id) { mutableIntStateOf((170..310).random()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(dynamicHeight.dp)
            .combinedClickable(onClick = onTap, onLongClick = { showMenu = true })
    ) {
        Box(Modifier.fillMaxSize()) {
            when (item.mediaType) {
                MediaType.VIDEO -> VideoPreview(uri = item.filePath.toUri(), muted = true, playWhenReady = true, modifier = Modifier.fillMaxSize())
                MediaType.IMAGE, MediaType.GIF -> AsyncImage(model = item.filePath, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }

            Text(
                text = item.mediaType.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(8.dp).clip(MaterialTheme.shapes.small).background(Color.Black.copy(alpha = 0.45f)).padding(horizontal = 8.dp, vertical = 4.dp)
            )

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Set as wallpaper") }, leadingIcon = { Icon(Icons.Default.Image, null) }, onClick = { showMenu = false; onSetWallpaper() })
                DropdownMenuItem(text = { Text("Delete") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { showMenu = false; onDelete() })
                DropdownMenuItem(text = { Text("Info") }, leadingIcon = { Icon(Icons.Default.Info, null) }, onClick = { showMenu = false; showInfo = true })
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Close") } },
            title = { Text("Media info") },
            text = { Column { Text("Type: ${item.mediaType}"); Text("File: ${item.filePath}"); Text("Added: ${item.dateAddedEpochMs}") } }
        )
    }
}

@Composable
private fun FullScreenPreview(item: WallpaperMediaEntity, onDismiss: () -> Unit, onSetWallpaper: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    when (item.mediaType) {
                        MediaType.VIDEO -> VideoPreview(uri = item.filePath.toUri(), muted = true, playWhenReady = true, modifier = Modifier.fillMaxSize())
                        MediaType.IMAGE, MediaType.GIF -> AsyncImage(model = item.filePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                }
                Text("Path: ${item.filePath}")
                Text("Added: ${item.dateAddedEpochMs}")
            }
        },
        confirmButton = { TextButton(onClick = { onSetWallpaper(); onDismiss() }) { Text("Set wallpaper") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun WallpaperSetDialog(
    item: WallpaperMediaEntity,
    onDismiss: () -> Unit,
    onApply: (WallpaperEditOptions) -> Unit
) {
    var screenTarget by rememberSaveable { mutableStateOf(ScreenTarget.BOTH) }
    var zoom by rememberSaveable { mutableStateOf(1f) }
    var panX by rememberSaveable { mutableStateOf(0f) }
    var panY by rememberSaveable { mutableStateOf(0f) }
    var fitMode by rememberSaveable { mutableStateOf(FitMode.CENTER_CROP) }
    var resolution by rememberSaveable { mutableStateOf(ResolutionPreset.SCREEN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Wallpaper") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Target screen")
                SingleChoiceSegmentedButtonRow {
                    ScreenTarget.entries.forEachIndexed { index, target ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ScreenTarget.entries.size),
                            selected = target == screenTarget,
                            onClick = { screenTarget = target },
                            label = { Text(target.name) }
                        )
                    }
                }

                if (item.mediaType == MediaType.IMAGE) {
                    Text("Fit mode")
                    SingleChoiceSegmentedButtonRow {
                        FitMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = FitMode.entries.size),
                                selected = mode == fitMode,
                                onClick = { fitMode = mode },
                                label = { Text(mode.name.replace('_', ' ')) }
                            )
                        }
                    }

                    Text("Resolution")
                    SingleChoiceSegmentedButtonRow {
                        ResolutionPreset.entries.forEachIndexed { index, preset ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = ResolutionPreset.entries.size),
                                selected = preset == resolution,
                                onClick = { resolution = preset },
                                label = { Text(preset.name) }
                            )
                        }
                    }

                    Text("Zoom: ${"%.2f".format(zoom)}x")
                    Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 1f..3f)

                    Text("Pan Horizontal: ${"%.2f".format(panX)}")
                    Slider(value = panX, onValueChange = { panX = it }, valueRange = -1f..1f)

                    Text("Pan Vertical: ${"%.2f".format(panY)}")
                    Slider(value = panY, onValueChange = { panY = it }, valueRange = -1f..1f)
                } else {
                    Text(
                        "For GIF/VIDEO: BOTH uses live wallpaper. HOME/LOCK applies a still frame, so you can set them independently.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApply(
                    WallpaperEditOptions(
                        fitMode = fitMode,
                        zoom = zoom,
                        panX = panX,
                        panY = panY,
                        resolutionPreset = resolution,
                        screenTarget = screenTarget
                    )
                )
            }) { Text("Apply") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
