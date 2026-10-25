package com.samovarsky.materialtrim
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Build
import android.widget.VideoView
import androidx.compose.runtime.key
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler

fun formatDuration(millis: Long): String {
    val s = (millis / 1000) % 60
    val m = (millis / 1000) / 60
    return "%d:%02d".format(m, s)
}
fun formatTimeMs(ms: Long): String {
    val minutes = ms / 60000
    val seconds = (ms % 60000) / 1000
    val millis = ms % 1000
    return "%02d:%02d.%03d".format(minutes, seconds, millis)
}
fun parseTimeMs(text: String): Long? = try {
    val parts = text.split(":")
    if (parts.size != 2) null else {
        val minutes = parts[0].toLong()
        val secMsParts = parts[1].split(".")
        val seconds = secMsParts[0].toLong()
        val millis = if (secMsParts.size > 1) secMsParts[1].toLong() else 0L
        minutes * 60000 + seconds * 1000 + millis
    }
} catch (e: Exception) {
    null
}
@Composable
fun FadeInItem(delayMs: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300, delayMillis = delayMs)) +
                slideInVertically(tween(300, delayMillis = delayMs)) { it / 8 }
    ) { content() }
}
@Composable
fun HomeScreen(
    state: EditorState,
    onTrimClick: () -> Unit,
    onTrimLosslessClick: () -> Unit,
    onVideoTrimClick: () -> Unit,
    onVideoToolsClick: () -> Unit,
    onMergeClick: () -> Unit,
    onConvertClick: () -> Unit,
    onSplitClick: () -> Unit,
    onExtractClick: () -> Unit,
    onEffectsClick: () -> Unit,
    onMetadataClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onAddClick: () -> Unit,
    onRemoveFile: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text(stringResource(R.string.home_loaded_files), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary) }
            if (state.selectedFiles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.home_no_files))
                            Text(stringResource(R.string.home_no_files_hint))
                        }
                    }
                }
            }
            items(state.selectedFiles) { file ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AudioFile, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(file.name, style = MaterialTheme.typography.titleMedium)
                            Text(formatDuration(file.duration), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onRemoveFile(file.id) }) {
                            Icon(Icons.Default.Close, stringResource(R.string.home_remove))
                        }
                    }
                }
            }
            item { Text(stringResource(R.string.home_tools), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToolCard(Icons.Default.ContentCut, stringResource(R.string.tool_trim_title), stringResource(R.string.tool_trim_desc), onTrimClick, role = 0)
                    ToolCard(Icons.Default.ContentCut, stringResource(R.string.tool_lossless_title), stringResource(R.string.tool_lossless_desc), onTrimLosslessClick, role = 1)
                    ToolCard(Icons.Default.VideoFile, stringResource(R.string.tool_vtrim_title), stringResource(R.string.tool_vtrim_desc), onVideoTrimClick, role = 2)
                    ToolCard(Icons.Default.VideoFile, stringResource(R.string.tool_vtools_title), stringResource(R.string.tool_vtools_desc), onVideoToolsClick, role = 2)
                    ToolCard(Icons.Default.Merge, stringResource(R.string.tool_merge_title), stringResource(R.string.tool_merge_desc), onMergeClick, role = 0)
                    ToolCard(Icons.Default.SwapHoriz, stringResource(R.string.tool_conv_title), stringResource(R.string.tool_conv_desc), onConvertClick, role = 1)
                    ToolCard(Icons.AutoMirrored.Filled.CallSplit, stringResource(R.string.tool_split_title), stringResource(R.string.tool_split_desc), onSplitClick, role = 2)
                    ToolCard(Icons.Default.VideoFile, stringResource(R.string.tool_extract_title), stringResource(R.string.tool_extract_desc), onExtractClick, role = 0)
                    ToolCard(Icons.Default.AutoAwesome, stringResource(R.string.tool_fx_title), stringResource(R.string.tool_fx_desc), onEffectsClick, role = 1)
                    ToolCard(Icons.Default.EditNote, stringResource(R.string.tool_meta_title), stringResource(R.string.tool_meta_desc), onMetadataClick, role = 2)
                    ToolCard(Icons.Default.History, stringResource(R.string.tool_hist_title), "${state.history.size} ${stringResource(R.string.hist_entries)}", onHistoryClick, role = 0)
                }
            }
        }
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) { Icon(Icons.Default.Add, stringResource(R.string.home_add)) }
    }
}
@Composable
fun ToolCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    role: Int = 0
) {
    val scheme = MaterialTheme.colorScheme
    val (container, onContainer) = when (role % 3) {
        0 -> scheme.primaryContainer to scheme.onPrimaryContainer
        1 -> scheme.secondaryContainer to scheme.onSecondaryContainer
        else -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    }
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = container,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = onContainer)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
@Composable
fun TrimScreen(
    state: EditorState,
    onUpdateTrim: (Long, Long) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSaveSnapshot: () -> Unit,
    onSetFadeIn: (Int) -> Unit,
    onSetFadeOut: (Int) -> Unit,
    onSetFadeInEnabled: (Boolean) -> Unit,
    onSetFadeOutEnabled: (Boolean) -> Unit,
    onSetVolume: (Float) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetEqGain: (Int, Float) -> Unit,
    onPreset: (Int) -> Unit,
    onSetSaveName: (String) -> Unit,
    onSetTrimFormat: (String) -> Unit,
    onSetTitle: (String) -> Unit,
    onSetArtist: (String) -> Unit,
    onSetAlbum: (String) -> Unit,
    onSetYear: (Int) -> Unit,
    onPrefill: (String, String, String, Int) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val file = state.selectedFiles.firstOrNull()
    val context = LocalContext.current
    val player = remember(file?.id) {
        if (file != null) {
            try {
                MediaPlayer().apply {
                    setDataSource(context, file.uri)
                    prepare()
                }
            } catch (e: Exception) { null }
        } else null
    }
    DisposableEffect(player) { onDispose { player?.release() } }
    val totalDuration = player?.duration?.toLong() ?: file?.duration ?: 0L
    var startMs by remember(file?.id) { mutableLongStateOf(0L) }
    var endMs by remember(file?.id) { mutableLongStateOf(totalDuration) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var startTimeText by remember(file?.id) { mutableStateOf(formatTimeMs(0L)) }
    var endTimeText by remember(file?.id) { mutableStateOf(formatTimeMs(totalDuration)) }
    LaunchedEffect(player, isPlaying) {
        if (player != null && isPlaying) {
            while (isPlaying) {
                val pos = player.currentPosition.toLong()
                positionMs = pos
                if (pos >= endMs) {
                    player.pause()
                    player.seekTo(startMs.toInt())
                    isPlaying = false
                    positionMs = startMs
                    break
                }
                if (!player.isPlaying) {
                    isPlaying = false
                    break
                }
                delay(30)
            }
        }
    }
    LaunchedEffect(file?.id) {
        onUpdateTrim(0L, totalDuration)
        if (file != null) {
            val r = android.media.MediaMetadataRetriever()
            try {
                r.setDataSource(context, file.uri)
                val title = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                val artist = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_AUTHOR) ?: ""
                val album = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
                val year = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DATE)?.toIntOrNull() ?: 0
                onPrefill(title, artist, album, year)
            } catch (e: Exception) {  } finally { r.release() }
        }
    }
    fun applyStart(ms: Long) {
        val v = ms.coerceIn(0L, (endMs - 1).coerceAtLeast(0L))
        startMs = v
        startTimeText = formatTimeMs(v)
        onUpdateTrim(v, endMs)
        if (!isPlaying) player?.seekTo(v.toInt())
    }
    fun applyEnd(ms: Long) {
        val v = ms.coerceIn(startMs + 1, totalDuration)
        endMs = v
        endTimeText = formatTimeMs(v)
        onUpdateTrim(startMs, v)
        val cur = player?.currentPosition?.toLong() ?: 0L
        if (!isPlaying && (cur < startMs || cur >= endMs)) {
            player?.seekTo(startMs.toInt())
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (file == null || player == null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(stringResource(R.string.need_file), modifier = Modifier.padding(16.dp))
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onSaveSnapshot(); onUndo() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Undo, null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.trim_undo))
                }
                OutlinedButton(onClick = onRedo, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Filled.Redo, null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.trim_redo))
                }
            }
            FadeInItem(0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(file.name, style = MaterialTheme.typography.titleMedium)
                        Text("${stringResource(R.string.trim_duration)} ${formatTimeMs(totalDuration)}")
                    }
                }
            }
            FadeInItem(60) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_presets), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(15, 30, 45).forEach { sec ->
                                FilledTonalButton(
                                    onClick = {
                                        applyEnd((startMs + sec * 1000L).coerceAtMost(totalDuration))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text(stringResource(R.string.trim_sec, sec), maxLines = 1, softWrap = false) }
                            }
                        }
                    }
                }
            }
            FadeInItem(120) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_selection), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        TrimTimeline(
                            totalDuration = totalDuration,
                            startMs = startMs,
                            endMs = endMs,
                            onStartChange = { applyStart(it) },
                            onEndChange = { applyEnd(it) }
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.trim_start), style = MaterialTheme.typography.labelSmall)
                                OutlinedTextField(
                                    value = startTimeText,
                                    onValueChange = { txt ->
                                        startTimeText = txt
                                        parseTimeMs(txt)?.let { v -> applyStart(v) }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.trim_end), style = MaterialTheme.typography.labelSmall)
                                OutlinedTextField(
                                    value = endTimeText,
                                    onValueChange = { txt ->
                                        endTimeText = txt
                                        parseTimeMs(txt)?.let { v -> applyEnd(v) }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${stringResource(R.string.trim_fragment)} ${formatTimeMs((endMs - startMs).coerceAtLeast(0L))}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            FadeInItem(180) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_playback), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = positionMs.toFloat(),
                            onValueChange = { positionMs = it.toLong(); player.seekTo(it.toInt()) },
                            valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatTimeMs(positionMs), style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.weight(1f))
                            Text(formatTimeMs(totalDuration), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = {
                                if (player == null) return@IconButton
                                if (isPlaying) {
                                    player.pause()
                                    isPlaying = false
                                } else {
                                    val cur = player.currentPosition.toLong()
                                    if (cur < startMs || cur >= endMs) {
                                        player.seekTo(startMs.toInt())
                                    }
                                    player.start()
                                    isPlaying = true
                                }
                            }) {
                                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                            }
                            IconButton(onClick = {
                                player.pause()
                                player.seekTo(startMs.toInt())
                                isPlaying = false
                                positionMs = startMs
                            }) {
                                Icon(Icons.Default.Stop, stringResource(R.string.trim_stop))
                            }
                        }
                    }
                }
            }
            FadeInItem(240) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_effects), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.trim_fadein))
                                Text("fade in", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = state.fadeInEnabled, onCheckedChange = onSetFadeInEnabled)
                        }
                        if (state.fadeInEnabled) {
                            Text(stringResource(R.string.trim_fade_dur, state.fadeInSec))
                            Slider(value = state.fadeInSec.toFloat(), onValueChange = { onSetFadeIn(it.toInt()) }, valueRange = 1f..10f)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.trim_fadeout))
                                Text("fade out", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(checked = state.fadeOutEnabled, onCheckedChange = onSetFadeOutEnabled)
                        }
                        if (state.fadeOutEnabled) {
                            Text(stringResource(R.string.trim_fade_dur, state.fadeOutSec))
                            Slider(value = state.fadeOutSec.toFloat(), onValueChange = { onSetFadeOut(it.toInt()) }, valueRange = 1f..10f)
                        }
                        Text("${stringResource(R.string.trim_volume)} ${"%.2f".format(state.volumeMultiplier)}x")
                        Slider(value = state.volumeMultiplier, onValueChange = onSetVolume, valueRange = 0.1f..3.0f)
                        Text("${stringResource(R.string.trim_speed)} ${"%.2f".format(state.speedMultiplier)}x")
                        Slider(value = state.speedMultiplier, onValueChange = onSetSpeed, valueRange = 0.5f..2.0f)
                    }
                }
            }
            FadeInItem(300) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Equalizer, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.trim_eq), style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        val labels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
                        labels.forEachIndexed { i, label ->
                            Text("$label: ${state.eqGains[i].toInt()} dB", style = MaterialTheme.typography.labelSmall)
                            Slider(value = state.eqGains[i], onValueChange = { onSetEqGain(i, it) }, valueRange = -12f..12f)
                        }
                    }
                }
            }
            FadeInItem(360) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_meta_copy), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.trim_meta_hint), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.metadata.title,
                            onValueChange = onSetTitle,
                            label = { Text(stringResource(R.string.meta_title)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.metadata.artist,
                            onValueChange = onSetArtist,
                            label = { Text(stringResource(R.string.meta_artist)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.metadata.album,
                            onValueChange = onSetAlbum,
                            label = { Text(stringResource(R.string.meta_album)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = if (state.metadata.year == 0) "" else state.metadata.year.toString(),
                            onValueChange = { txt -> onSetYear(txt.toIntOrNull() ?: 0) },
                            label = { Text(stringResource(R.string.meta_year)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
            FadeInItem(420) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_out_format), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        val formats = listOf("mp3", "wav", "m4a", "ogg", "flac")
                        formats.forEach { f ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = state.trimOutputFormat == f, onClick = { onSetTrimFormat(f) })
                                Text(f.uppercase() + stringResource(
                                    when (f) {
                                        "mp3" -> R.string.fmt_desc_mp3
                                        "wav" -> R.string.fmt_desc_wav
                                        "m4a" -> R.string.fmt_desc_m4a
                                        "ogg" -> R.string.fmt_desc_ogg
                                        else -> R.string.fmt_desc_flac
                                    }
                                ), modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
            FadeInItem(480) {
                Column {
                    OutlinedTextField(
                        value = state.saveFileName,
                        onValueChange = onSetSaveName,
                        label = { Text(stringResource(R.string.save_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onSaveSnapshot(); onSave() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = endMs > startMs
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("${stringResource(R.string.save_as)} ${state.trimOutputFormat.uppercase()}")
                    }
                }
            }
        }
    }
}
@Composable
fun TrimLosslessScreen(
    state: EditorState,
    onUpdateTrim: (Long, Long) -> Unit,
    onSetSaveName: (String) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val file = state.selectedFiles.firstOrNull()
    val context = LocalContext.current
    val player = remember(file?.id) {
        if (file != null) {
            try {
                MediaPlayer().apply {
                    setDataSource(context, file.uri)
                    prepare()
                }
            } catch (e: Exception) { null }
        } else null
    }
    DisposableEffect(player) { onDispose { player?.release() } }
    val totalDuration = player?.duration?.toLong() ?: file?.duration ?: 0L
    var startMs by remember(file?.id) { mutableLongStateOf(0L) }
    var endMs by remember(file?.id) { mutableLongStateOf(totalDuration) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var startTimeText by remember(file?.id) { mutableStateOf(formatTimeMs(0L)) }
    var endTimeText by remember(file?.id) { mutableStateOf(formatTimeMs(totalDuration)) }
    val fileExt = remember(file?.name) { file?.name?.substringAfterLast('.', "mp3")?.uppercase() ?: "MP3" }
    LaunchedEffect(player, isPlaying) {
        if (player != null && isPlaying) {
            while (isPlaying) {
                val pos = player.currentPosition.toLong()
                positionMs = pos
                if (pos >= endMs) {
                    player.pause()
                    player.seekTo(startMs.toInt())
                    isPlaying = false
                    positionMs = startMs
                    break
                }
                if (!player.isPlaying) {
                    isPlaying = false
                    break
                }
                delay(30)
            }
        }
    }
    LaunchedEffect(file?.id) { onUpdateTrim(0L, totalDuration) }
    fun applyStart(ms: Long) {
        val v = ms.coerceIn(0L, (endMs - 1).coerceAtLeast(0L))
        startMs = v
        startTimeText = formatTimeMs(v)
        onUpdateTrim(v, endMs)
        if (!isPlaying) player?.seekTo(v.toInt())
    }
    fun applyEnd(ms: Long) {
        val v = ms.coerceIn(startMs + 1, totalDuration)
        endMs = v
        endTimeText = formatTimeMs(v)
        onUpdateTrim(startMs, v)
        val cur = player?.currentPosition?.toLong() ?: 0L
        if (!isPlaying && (cur < startMs || cur >= endMs)) {
            player?.seekTo(startMs.toInt())
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (file == null || player == null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(stringResource(R.string.need_file), modifier = Modifier.padding(16.dp))
            }
        } else {
            FadeInItem(0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCut, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.lossless_mode), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.lossless_desc, fileExt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            FadeInItem(60) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(file.name, style = MaterialTheme.typography.titleMedium)
                        Text("${stringResource(R.string.trim_duration)} ${formatTimeMs(totalDuration)}")
                        Text(stringResource(R.string.lossless_src, fileExt), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            FadeInItem(120) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_selection), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        TrimTimeline(
                            totalDuration = totalDuration,
                            startMs = startMs,
                            endMs = endMs,
                            onStartChange = { applyStart(it) },
                            onEndChange = { applyEnd(it) }
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.trim_start), style = MaterialTheme.typography.labelSmall)
                                OutlinedTextField(
                                    value = startTimeText,
                                    onValueChange = { txt ->
                                        startTimeText = txt
                                        parseTimeMs(txt)?.let { v -> applyStart(v) }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.trim_end), style = MaterialTheme.typography.labelSmall)
                                OutlinedTextField(
                                    value = endTimeText,
                                    onValueChange = { txt ->
                                        endTimeText = txt
                                        parseTimeMs(txt)?.let { v -> applyEnd(v) }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${stringResource(R.string.trim_fragment)} ${formatTimeMs((endMs - startMs).coerceAtLeast(0L))}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            FadeInItem(180) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_playback), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = positionMs.toFloat(),
                            onValueChange = { positionMs = it.toLong(); player.seekTo(it.toInt()) },
                            valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatTimeMs(positionMs), style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.weight(1f))
                            Text(formatTimeMs(totalDuration), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = {
                                if (player == null) return@IconButton
                                if (isPlaying) {
                                    player.pause()
                                    isPlaying = false
                                } else {
                                    val cur = player.currentPosition.toLong()
                                    if (cur < startMs || cur >= endMs) {
                                        player.seekTo(startMs.toInt())
                                    }
                                    player.start()
                                    isPlaying = true
                                }
                            }) {
                                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                            }
                            IconButton(onClick = {
                                player.pause()
                                player.seekTo(startMs.toInt())
                                isPlaying = false
                                positionMs = startMs
                            }) {
                                Icon(Icons.Default.Stop, stringResource(R.string.trim_stop))
                            }
                        }
                    }
                }
            }
            FadeInItem(240) {
                Column {
                    OutlinedTextField(
                        value = state.saveFileName,
                        onValueChange = onSetSaveName,
                        label = { Text(stringResource(R.string.save_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = endMs > startMs
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("${stringResource(R.string.save_lossless)} ($fileExt)")
                    }
                }
            }
        }
    }
}
@Composable
fun VideoTrimScreen(
    state: EditorState,
    onUpdateTrim: (Long, Long) -> Unit,
    onSetLossless: (Boolean) -> Unit,
    onPickVideo: () -> Unit,
    onSetSaveName: (String) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current
    val videoUri = state.videoTrimUri
    val videoName = videoUri?.let { getFileName(context, it) } ?: ""
    val videoExt = videoName.substringAfterLast('.', "mp4").lowercase()
    val totalDuration = remember(videoUri) {
        if (videoUri != null) AudioFileUtils.getAudioDuration(context, videoUri) else 0L
    }
    var startMs by remember(videoUri) { mutableLongStateOf(0L) }
    var endMs by remember(videoUri) { mutableLongStateOf(totalDuration) }
    var startTimeText by remember(videoUri) { mutableStateOf(formatTimeMs(0L)) }
    var endTimeText by remember(videoUri) { mutableStateOf(formatTimeMs(totalDuration)) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var aspect by remember(videoUri) { mutableStateOf(0f) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var isVideoPlaying by remember { mutableStateOf(false) }
    var pendingSeekStart by remember { mutableStateOf(false) }
    fun seekToExact(ms: Int) {
        val mp = mediaPlayer
        val clamped = ms.coerceIn(0, (totalDuration - 1).toInt().coerceAtLeast(0))
        if (mp != null && android.os.Build.VERSION.SDK_INT >= 26) {
            mp.seekTo(clamped.toLong(), MediaPlayer.SEEK_CLOSEST)
        } else {
            videoView?.seekTo(clamped)
        }
        positionMs = clamped.toLong()
    }
    fun seekToStartMs() {
        seekToExact(startMs.toInt())
    }
    LaunchedEffect(videoUri) { onUpdateTrim(0L, totalDuration) }
    LaunchedEffect(isVideoPlaying, videoUri) {
        if (isVideoPlaying) {
            while (isVideoPlaying) {
                val vv = videoView
                if (vv == null) { isVideoPlaying = false; break }
                delay(50)
                val pos = vv.currentPosition.toLong()
                positionMs = pos
                if (pos >= endMs) {
                    vv.pause(); seekToStartMs(); isVideoPlaying = false; break
                }
                if (!vv.isPlaying) { isVideoPlaying = false; break }
            }
        }
    }
    LaunchedEffect(pendingSeekStart, videoUri) {
        if (!pendingSeekStart) return@LaunchedEffect
        val vv = videoView
        if (vv == null) {
            pendingSeekStart = false
            return@LaunchedEffect
        }
        delay(300)
        if (!pendingSeekStart) return@LaunchedEffect
        vv.start()
        isVideoPlaying = true
        positionMs = vv.currentPosition.toLong()
        pendingSeekStart = false
    }
    DisposableEffect(Unit) {
        onDispose { videoView?.stopPlayback(); videoView = null; mediaPlayer = null }
    }
    fun applyStart(ms: Long) {
        val v = ms.coerceIn(0L, (endMs - 1).coerceAtLeast(0L))
        startMs = v
        startTimeText = formatTimeMs(v)
        onUpdateTrim(v, endMs)
        if (!isVideoPlaying) seekToExact(v.toInt())
    }
    fun applyEnd(ms: Long) {
        val v = ms.coerceIn(startMs + 1, totalDuration.coerceAtLeast(1L))
        endMs = v
        endTimeText = formatTimeMs(v)
        onUpdateTrim(startMs, v)
        val cur = videoView?.currentPosition?.toLong() ?: 0L
        if (!isVideoPlaying && (cur < startMs || cur >= endMs)) seekToStartMs()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (videoUri == null) {
            FadeInItem(0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Icon(Icons.Default.VideoFile, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.screen_video_trim), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.vt_pick_hint), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onPickVideo, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.vt_pick))
                        }
                    }
                }
            }
        } else {
            FadeInItem(0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(videoName, style = MaterialTheme.typography.titleMedium)
                        Text("${stringResource(R.string.trim_duration)} ${formatTimeMs(totalDuration)}")
                        Text(stringResource(R.string.vt_format, videoExt.uppercase()), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            FadeInItem(40) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.vt_preview), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        key(videoUri) {
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val previewHeight = if (aspect > 0f) (maxWidth / aspect).coerceIn(140.dp, 420.dp) else 220.dp
                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(previewHeight)
                                        .clip(RoundedCornerShape(12.dp)),
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            videoView = this
                                            setVideoURI(videoUri)
                                            setOnPreparedListener { mp ->
                                                mediaPlayer = mp
                                                val vw = mp.videoWidth.coerceAtLeast(1)
                                                val vh = mp.videoHeight.coerceAtLeast(1)
                                                aspect = vw.toFloat() / vh
                                                seekToExact(startMs.toInt())
                                                post { seekToExact(startMs.toInt()) }
                                            }
                                            setOnCompletionListener { isVideoPlaying = false }
                                            setOnErrorListener { _, _, _ -> isVideoPlaying = false; true }
                                        }
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = positionMs.toFloat(),
                            onValueChange = { positionMs = it.toLong(); videoView?.seekTo(it.toInt()) },
                            valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatTimeMs(positionMs), style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.weight(1f))
                            Text(formatTimeMs(totalDuration), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = {
                                val vv = videoView ?: return@IconButton
                                if (isVideoPlaying || pendingSeekStart) {
                                    vv.pause(); isVideoPlaying = false; pendingSeekStart = false
                                } else {
                                    val cur = vv.currentPosition.toLong()
                                    if (cur < startMs || cur >= endMs) {
                                        seekToExact(startMs.toInt())
                                        pendingSeekStart = true
                                    } else {
                                        vv.start(); isVideoPlaying = true
                                    }
                                }
                            }) {
                                Icon(if (isVideoPlaying || pendingSeekStart) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                            }
                            IconButton(onClick = {
                                videoView?.pause()
                                isVideoPlaying = false
                                pendingSeekStart = false
                                seekToStartMs()
                            }) {
                                Icon(Icons.Default.Stop, stringResource(R.string.vt_to_start))
                            }
                        }
                    }
                }
            }
            FadeInItem(80) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.trim_selection), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        TrimTimeline(
                            totalDuration = totalDuration,
                            startMs = startMs,
                            endMs = endMs,
                            onStartChange = { applyStart(it) },
                            onEndChange = { applyEnd(it) }
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.trim_start), style = MaterialTheme.typography.labelSmall)
                                OutlinedTextField(
                                    value = startTimeText,
                                    onValueChange = { txt ->
                                        startTimeText = txt
                                        parseTimeMs(txt)?.let { v -> applyStart(v) }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.trim_end), style = MaterialTheme.typography.labelSmall)
                                OutlinedTextField(
                                    value = endTimeText,
                                    onValueChange = { txt ->
                                        endTimeText = txt
                                        parseTimeMs(txt)?.let { v -> applyEnd(v) }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${stringResource(R.string.trim_fragment)} ${formatTimeMs((endMs - startMs).coerceAtLeast(0L))}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            FadeInItem(120) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.vt_lossless), style = MaterialTheme.typography.titleSmall)
                                Text(
                                    if (state.videoLossless) stringResource(R.string.vt_lossless_on, videoExt)
                                    else stringResource(R.string.vt_lossless_off),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(checked = state.videoLossless, onCheckedChange = onSetLossless)
                        }
                        if (!state.videoLossless) {
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.vt_reencode_note), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            FadeInItem(160) {
                Column {
                    OutlinedTextField(
                        value = state.saveFileName,
                        onValueChange = onSetSaveName,
                        label = { Text(stringResource(R.string.save_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = endMs > startMs
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (state.videoLossless) "${stringResource(R.string.save_lossless)} (${videoExt.uppercase()})"
                            else stringResource(R.string.save_mp4)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun TrimTimeline(
    totalDuration: Long,
    startMs: Long,
    endMs: Long,
    onStartChange: (Long) -> Unit,
    onEndChange: (Long) -> Unit
) {
    val curStart by rememberUpdatedState(startMs)
    val curEnd by rememberUpdatedState(endMs)
    var active by remember { mutableStateOf(-1) }
    val total = totalDuration.coerceAtLeast(1L)
    val startF = (curStart.toFloat() / total).coerceIn(0f, 1f)
    val endF = (curEnd.toFloat() / total).coerceIn(0f, 1f)
    val selF = (endF - startF).coerceAtLeast(0f)
    val eps = 0.001f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(total) {
                val w = size.width.toFloat().coerceAtLeast(1f)
                detectHorizontalDragGestures(
                    onDragStart = { off ->
                        val sx = curStart.toFloat() / total * w
                        val ex = curEnd.toFloat() / total * w
                        active = if (kotlin.math.abs(off.x - sx) <= kotlin.math.abs(off.x - ex)) 0 else 1
                    },
                    onDragEnd = { active = -1 },
                    onDragCancel = { active = -1 },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val ms = (change.position.x / w * total).toLong()
                        if (active == 0) onStartChange(ms)
                        else if (active == 1) onEndChange(ms)
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(startF.coerceAtLeast(eps)))
        Box(modifier = Modifier.width(8.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
        Box(
            modifier = Modifier
                .weight(selF.coerceAtLeast(eps))
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        )
        Box(modifier = Modifier.width(8.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
        Spacer(modifier = Modifier.weight((1f - endF).coerceAtLeast(eps)))
    }
}
@Composable
fun MergeScreen(
    state: EditorState,
    onSetCrossfadeEnabled: (Boolean) -> Unit,
    onSetCrossfadeDuration: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FadeInItem(0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.merge_files), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.merge_selected, state.selectedFiles.size), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        state.selectedFiles.forEachIndexed { index, file ->
            FadeInItem(40 * index) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${index + 1}. ${file.name}")
                            Text(formatDuration(file.duration), style = MaterialTheme.typography.bodySmall)
                        }
                        Column {
                            IconButton(onClick = { onMove(index, index - 1) }, enabled = index > 0) {
                                Icon(Icons.Default.KeyboardArrowUp, stringResource(R.string.merge_up))
                            }
                            IconButton(
                                onClick = { onMove(index, index + 1) },
                                enabled = index < state.selectedFiles.lastIndex
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.merge_down))
                            }
                        }
                    }
                }
            }
        }
        FadeInItem(60) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.merge_crossfade), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.merge_crossfade_sub), style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = state.crossfadeEnabled, onCheckedChange = onSetCrossfadeEnabled)
                    }
                    if (state.crossfadeEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.merge_crossfade_dur, state.crossfadeDuration))
                        Slider(
                            value = state.crossfadeDuration.toFloat(),
                            onValueChange = { onSetCrossfadeDuration(it.toInt()) },
                            valueRange = 1f..10f
                        )
                    }
                }
            }
        }
        if (state.selectedFiles.size < 2) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(stringResource(R.string.merge_need2), modifier = Modifier.padding(16.dp))
            }
        }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), enabled = state.selectedFiles.size >= 2) {
            Text(stringResource(R.string.merge_save))
        }
    }
}
@Composable
fun ConverterScreen(
    state: EditorState,
    onSetFormat: (String) -> Unit,
    onSetBitrate: (Int) -> Unit,
    onSave: () -> Unit,
    onBatchConvert: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val formats = listOf("mp3", "wav", "m4a", "ogg", "flac")
    val bitrates = listOf(128, 192, 256, 320)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FadeInItem(0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.conv_format), style = MaterialTheme.typography.titleSmall)
                    formats.forEach { f ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = state.selectedFormat == f, onClick = { onSetFormat(f) })
                            Text(f.uppercase(), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
        if (state.selectedFormat in listOf("mp3", "m4a", "ogg")) {
            FadeInItem(80) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.conv_bitrate, state.bitrate), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        bitrates.forEach { b ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = state.bitrate == b, onClick = { onSetBitrate(b) })
                                Text(stringResource(R.string.conv_kbps, b), modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }
        FadeInItem(120) {
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), enabled = state.selectedFiles.isNotEmpty()) {
                Text(stringResource(R.string.conv_save, state.selectedFormat.uppercase()))
            }
        }
        FadeInItem(160) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.conv_batch), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.conv_batch_sub, state.selectedFiles.size), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onBatchConvert,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.selectedFiles.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.conv_batch_btn))
                    }
                }
            }
        }
    }
}
@Composable
fun SplitScreen(state: EditorState, onSave: (Long) -> Unit, onBackClick: () -> Unit) {
    BackHandler { onBackClick() }
    var splitAtMs by remember { mutableLongStateOf(0L) }
    val file = state.selectedFiles.firstOrNull()
    val total = file?.duration ?: 0L
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (file == null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(stringResource(R.string.need_file), modifier = Modifier.padding(16.dp))
            }
        } else {
            FadeInItem(0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Icon(Icons.AutoMirrored.Filled.CallSplit, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.split_title), style = MaterialTheme.typography.titleMedium)
                        Text(file.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            FadeInItem(90) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.split_point, formatTimeMs(splitAtMs)), style = MaterialTheme.typography.titleSmall)
                        Slider(
                            value = splitAtMs.toFloat(),
                            onValueChange = { splitAtMs = it.toLong() },
                            valueRange = 1000f..(total - 1000).toFloat().coerceAtLeast(2000f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.split_part1, formatTimeMs(splitAtMs)))
                        Text(stringResource(R.string.split_part2, formatTimeMs(splitAtMs), formatTimeMs(total)))
                    }
                }
            }
            Button(onClick = { onSave(splitAtMs) }, modifier = Modifier.fillMaxWidth(), enabled = splitAtMs > 1000L) {
                Text(stringResource(R.string.split_save))
            }
        }
    }
}
@Composable
fun ExtractAudioScreen(
    state: EditorState,
    onPickVideo: () -> Unit,
    onSetFormat: (String) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current
    val videoName = state.videoUri?.let { getFileName(context, it) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FadeInItem(0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Icon(Icons.Default.VideoFile, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.ex_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.ex_sub), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        FadeInItem(80) {
            Button(onClick = onPickVideo, modifier = Modifier.fillMaxWidth()) {
                Text(if (videoName == null) stringResource(R.string.vt_pick) else stringResource(R.string.ex_video, videoName))
            }
        }
        if (state.videoUri != null) {
            FadeInItem(160) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.ex_out), style = MaterialTheme.typography.titleSmall)
                        listOf("mp3", "wav", "m4a").forEach { f ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = state.selectedFormat == f, onClick = { onSetFormat(f) })
                                Text(f.uppercase(), modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
            FadeInItem(240) {
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.ex_save, state.selectedFormat.uppercase()))
                }
            }
        }
    }
}
@Composable
fun EffectsScreen(
    state: EditorState,
    onReverse: () -> Unit,
    onLoop: (Int) -> Unit,
    onDenoise: () -> Unit,
    onNormalize: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val file = state.selectedFiles.firstOrNull()
    var loopCount by remember { mutableIntStateOf(2) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (file == null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(stringResource(R.string.need_file), modifier = Modifier.padding(16.dp))
            }
        } else {
            FadeInItem(0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(file.name, style = MaterialTheme.typography.titleMedium)
                        Text("${stringResource(R.string.trim_duration)} ${formatDuration(file.duration)}")
                    }
                }
            }
            FadeInItem(80) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.fx_reverse), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.fx_reverse_sub), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onReverse, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.fx_reverse_btn))
                        }
                    }
                }
            }
            FadeInItem(160) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.fx_loop), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.fx_loop_sub), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.fx_loop_count, loopCount))
                        Slider(
                            value = loopCount.toFloat(),
                            onValueChange = { loopCount = it.toInt() },
                            valueRange = 1f..10f
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { onLoop(loopCount) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.fx_loop_btn))
                        }
                    }
                }
            }
            FadeInItem(240) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.fx_denoise), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.fx_denoise_sub), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onDenoise, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.fx_denoise_btn))
                        }
                    }
                }
            }
            FadeInItem(300) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.fx_normalize), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.fx_normalize_sub), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onNormalize, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.fx_normalize_btn))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MetadataScreen(
    state: EditorState,
    onPrefill: (String, String, String, Int) -> Unit,
    onSetTitle: (String) -> Unit,
    onSetArtist: (String) -> Unit,
    onSetAlbum: (String) -> Unit,
    onSetYear: (Int) -> Unit,
    onPickCover: () -> Unit,
    onRemoveCover: () -> Unit,
    onApply: () -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current
    val firstFile = state.selectedFiles.firstOrNull()
    var curTitle by remember { mutableStateOf("") }
    var curArtist by remember { mutableStateOf("") }
    var curAlbum by remember { mutableStateOf("") }
    var curYear by remember { mutableStateOf("") }
    var coverBytes by remember { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(firstFile?.id) {
        coverBytes = null
        if (firstFile != null) {
            val r = android.media.MediaMetadataRetriever()
            try {
                r.setDataSource(context, firstFile.uri)
                val title = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                val artist = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_AUTHOR) ?: ""
                val album = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
                val year = r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DATE)?.toIntOrNull() ?: 0
                coverBytes = r.embeddedPicture
                onPrefill(title, artist, album, year)
                curTitle = title
                curArtist = artist
                curAlbum = album
                curYear = if (year > 0) year.toString() else ""
            } catch (e: Exception) {  } finally { r.release() }
        }
    }
    val coverBitmap = remember(coverBytes) {
        val bytes = coverBytes
        if (bytes != null) BitmapFactory.decodeByteArray(bytes, 0, bytes.size) else null
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FadeInItem(0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.md_files, state.selectedFiles.size), style = MaterialTheme.typography.titleMedium)
                    state.selectedFiles.take(4).forEach { f ->
                        Text("• ${f.name}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (state.selectedFiles.size > 4) {
                        Text(stringResource(R.string.md_more, state.selectedFiles.size - 4), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        FadeInItem(60) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.md_current), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("${stringResource(R.string.meta_title)}: ${curTitle.ifBlank { "—" }}")
                    Text("${stringResource(R.string.meta_artist)}: ${curArtist.ifBlank { "—" }}")
                    Text("${stringResource(R.string.meta_album)}: ${curAlbum.ifBlank { "—" }}")
                    Text("${stringResource(R.string.meta_year)}: ${curYear.ifBlank { "—" }}")
                    Spacer(Modifier.height(8.dp))
                    if (coverBitmap != null) {
                        Image(
                            bitmap = coverBitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.md_cover_current),
                            modifier = Modifier.size(96.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(stringResource(R.string.md_no_cover), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        FadeInItem(120) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.md_new), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.md_new_hint), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.metadata.title,
                        onValueChange = onSetTitle,
                        label = { Text(stringResource(R.string.meta_title)) },
                        placeholder = { Text(stringResource(R.string.md_ph_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.metadata.artist,
                        onValueChange = onSetArtist,
                        label = { Text(stringResource(R.string.meta_artist)) },
                        placeholder = { Text(stringResource(R.string.md_ph_artist)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.metadata.album,
                        onValueChange = onSetAlbum,
                        label = { Text(stringResource(R.string.meta_album)) },
                        placeholder = { Text(stringResource(R.string.md_ph_album)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = if (state.metadata.year == 0) "" else state.metadata.year.toString(),
                        onValueChange = { txt -> onSetYear(txt.toIntOrNull() ?: 0) },
                        label = { Text(stringResource(R.string.meta_year)) },
                        placeholder = { Text(stringResource(R.string.md_ph_year)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
        FadeInItem(180) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.md_cover), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    if (state.metadataCoverUri != null) {
                        Text(stringResource(R.string.md_cover_new))
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onRemoveCover) { Text(stringResource(R.string.md_cover_remove)) }
                    } else {
                        Button(onClick = onPickCover, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.md_cover_pick))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.md_cover_note), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        FadeInItem(240) {
            Column {
                Text(stringResource(R.string.md_inplace), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedFiles.isNotEmpty()
                ) {
                    Text(stringResource(R.string.md_apply, state.selectedFiles.size))
                }
            }
        }
    }
}
@Composable
fun HistoryScreen(state: EditorState, onClear: () -> Unit, onBackClick: () -> Unit) {
    BackHandler { onBackClick() }
    val df = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.hist_title, state.history.size), style = MaterialTheme.typography.titleLarge)
            if (state.history.isNotEmpty()) {
                OutlinedButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.hist_clear))
                }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.history.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.hist_empty))
                            Text(stringResource(R.string.hist_empty_hint))
                        }
                    }
                }
            }
            items(state.history) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AudioFile, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleSmall)
                            Text("${item.action} • ${df.format(Date(item.timestamp))}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SettingsScreen(
    state: EditorState,
    onSetLang: (String) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onSetDynamic: (Boolean) -> Unit,
    onSetColorPreset: (String) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FadeInItem(0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.set_interface), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.set_language), style = MaterialTheme.typography.titleSmall)
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = state.lang == "ru", onClick = { onSetLang("ru") })
                        Text(stringResource(R.string.set_lang_ru), modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = state.lang == "en", onClick = { onSetLang("en") })
                        Text(stringResource(R.string.set_lang_en), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
        FadeInItem(60) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.set_theme), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.set_theme_mode), style = MaterialTheme.typography.titleSmall)
                    listOf("system" to R.string.set_theme_system, "light" to R.string.set_theme_light, "dark" to R.string.set_theme_dark).forEach { (code, res) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = state.themeMode == code, onClick = { onSetThemeMode(code) })
                            Text(stringResource(res), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    FadeInItem(120) {
                        val uriHandler = LocalUriHandler.current
                        val context = LocalContext.current
                        val versionName = remember {
                            try {
                                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
                            } catch (e: Exception) { "1.0" }
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(stringResource(R.string.set_about_title), style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    stringResource(R.string.set_about_version, versionName),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(stringResource(R.string.set_about_text), style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { uriHandler.openUri("https://github.com/Samovarsky/MaterialTrim") },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.set_about_source)) }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { uriHandler.openUri("https://github.com/Samovarsky/MaterialTrim/blob/main/LICENSE") },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.set_about_license)) }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.set_dynamic))
                            Text(stringResource(R.string.set_dynamic_sub), style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = state.dynamicColor,
                            onCheckedChange = onSetDynamic,
                            enabled = Build.VERSION.SDK_INT >= 31
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.set_preset), style = MaterialTheme.typography.titleSmall)
                    listOf(
                        "pink" to R.string.set_preset_pink,
                        "green" to R.string.set_preset_green,
                        "olive" to R.string.set_preset_olive,
                        "yellow" to R.string.set_preset_yellow,
                        "brown" to R.string.set_preset_brown,
                        "red" to R.string.set_preset_red,
                        "cyan" to R.string.set_preset_cyan
                    ).forEach { (code, res) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.colorPreset == code,
                                onClick = { onSetColorPreset(code) },
                                enabled = !state.dynamicColor
                            )
                            Text(stringResource(res), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun VideoToolsScreen(
    state: EditorState,
    viewModel: AudioEditorViewModel,
    onBackClick: () -> Unit
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current
    val videoUri = state.videoToolsUri
    val videoName = videoUri?.let { getFileName(context, it) } ?: ""
    var gifStart by remember { mutableStateOf("0") }
    var gifEnd by remember { mutableStateOf("5") }
    var gifFps by remember { mutableIntStateOf(12) }
    var gifWidth by remember { mutableIntStateOf(480) }
    var tfWidth by remember { mutableStateOf("640") }
    var tfHeight by remember { mutableStateOf("-1") }
    var tfTranspose by remember { mutableIntStateOf(-1) }
    var tfMirror by remember { mutableStateOf(false) }
    var tfSpeed by remember { mutableStateOf("1.0") }
    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.setVideoToolsUri(it) }
    }
    val saveGif = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/gif")) { uri ->
        uri?.let { out ->
            val v = videoUri ?: return@let
            val s = gifStart.toDoubleOrNull() ?: 0.0
            val e = gifEnd.toDoubleOrNull() ?: (s + 5.0)
            viewModel.videoToGif(v, s, e, gifFps, gifWidth, out)
        }
    }
    val saveTf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { uri ->
        uri?.let { out ->
            val v = videoUri ?: return@let
            val w = tfWidth.toIntOrNull() ?: 640
            val h = tfHeight.toIntOrNull() ?: -1
            val sp = tfSpeed.toDoubleOrNull() ?: 1.0
            viewModel.transformVideo(v, w, h, tfTranspose, tfMirror, sp, out)
        }
    }
    val saveRemoveAudio = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { uri ->
        uri?.let { out ->
            val v = videoUri ?: return@let
            viewModel.removeVideoAudio(v, out)
        }
    }
    var pendingReplaceAudio by remember { mutableStateOf<Uri?>(null) }
    val saveReplaceAudio = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { uri ->
        uri?.let { out ->
            val v = videoUri
            val a = pendingReplaceAudio
            if (v != null && a != null) viewModel.replaceVideoAudio(v, a, out)
            pendingReplaceAudio = null
        }
    }
    val pickReplaceAudio = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { aUri ->
        val v = videoUri
        if (v != null && aUri != null) {
            pendingReplaceAudio = aUri
            saveReplaceAudio.launch("with_new_audio.mp4")
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (videoUri == null) {
            FadeInItem(0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Icon(Icons.Default.VideoFile, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.vtools_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.vtools_hint), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { pickVideo.launch(arrayOf("video/*")) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.vt_pick))
                        }
                    }
                }
            }
        } else {
            FadeInItem(0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(videoName, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.vtools_change), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { pickVideo.launch(arrayOf("video/*")) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.vt_pick))
                        }
                    }
                }
            }
            FadeInItem(60) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.vtools_gif), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = gifStart, onValueChange = { gifStart = it }, label = { Text(stringResource(R.string.vtools_gif_start)) }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = gifEnd, onValueChange = { gifEnd = it }, label = { Text(stringResource(R.string.vtools_gif_end)) }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = gifFps.toString(), onValueChange = { gifFps = it.toIntOrNull() ?: 12 }, label = { Text(stringResource(R.string.vtools_gif_fps)) }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = gifWidth.toString(), onValueChange = { gifWidth = it.toIntOrNull() ?: 480 }, label = { Text(stringResource(R.string.vtools_gif_width)) }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { saveGif.launch("clip.gif") }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.vtools_gif_btn))
                        }
                    }
                }
            }
            FadeInItem(120) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.vtools_tf), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = tfWidth, onValueChange = { tfWidth = it }, label = { Text(stringResource(R.string.vtools_tf_w)) }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            OutlinedTextField(value = tfHeight, onValueChange = { tfHeight = it }, label = { Text(stringResource(R.string.vtools_tf_h)) }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.vtools_tf_rot), style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(-1 to R.string.vtools_tf_rot_none, 0 to R.string.vtools_tf_rot_cw, 2 to R.string.vtools_tf_rot_ccw, 1 to R.string.vtools_tf_flip_h, 3 to R.string.vtools_tf_flip_v).forEach { (code, res) ->
                                FilledTonalButton(onClick = { tfTranspose = code }, modifier = Modifier.weight(1f)) { Text(stringResource(res), maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(stringResource(R.string.vtools_tf_mirror)) }
                            Switch(checked = tfMirror, onCheckedChange = { tfMirror = it })
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = tfSpeed, onValueChange = { tfSpeed = it }, label = { Text(stringResource(R.string.vtools_tf_speed)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { saveTf.launch("transformed.mp4") }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.vtools_tf_btn))
                        }
                    }
                }
            }
            FadeInItem(180) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.vtools_audio), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { pickReplaceAudio.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.vtools_audio_replace))
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { saveRemoveAudio.launch("no_audio.mp4") }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.vtools_audio_remove))
                        }
                    }
                }
            }
        }
    }
}