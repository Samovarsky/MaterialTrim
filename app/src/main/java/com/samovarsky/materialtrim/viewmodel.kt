package com.samovarsky.materialtrim
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
sealed class EditorScreen {
    object Home : EditorScreen()
    object Trim : EditorScreen()
    object TrimLossless : EditorScreen()
    object VideoTrim : EditorScreen()
    object Merge : EditorScreen()
    object Converter : EditorScreen()
    object Split : EditorScreen()
    object ExtractAudio : EditorScreen()
    object Effects : EditorScreen()
    object Metadata : EditorScreen()
    object History : EditorScreen()
    object Settings : EditorScreen()
    object VideoTools : EditorScreen()
}
private data class Tags(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val year: Int = 0
)
data class EditorState(
    val currentScreen: EditorScreen = EditorScreen.Home,
    val selectedFiles: List<AudioFile> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
    val trimStart: Long = 0,
    val trimEnd: Long = 0,
    val fadeInSec: Int = 0,
    val fadeOutSec: Int = 0,
    val fadeInEnabled: Boolean = false,
    val fadeOutEnabled: Boolean = false,
    val volumeMultiplier: Float = 1.0f,
    val speedMultiplier: Float = 1.0f,
    val eqGains: List<Float> = listOf(0f, 0f, 0f, 0f, 0f),
    val selectedFormat: String = "mp3",
    val bitrate: Int = 192,
    val history: List<HistoryItem> = emptyList(),
    val videoUri: Uri? = null,
    val saveFileName: String = "",
    val trimOutputFormat: String = "mp3",
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3,
    val metadata: AudioMetadata = AudioMetadata(),
    val metadataCoverUri: Uri? = null,
    val titleDirty: Boolean = false,
    val artistDirty: Boolean = false,
    val albumDirty: Boolean = false,
    val yearDirty: Boolean = false,
    val coverTouched: Boolean = false,
    val videoTrimUri: Uri? = null,
    val videoToolsUri: Uri? = null,
    val videoLossless: Boolean = true,
    val lang: String = "en",
    val themeMode: String = "system",
    val dynamicColor: Boolean = false,
    val colorPreset: String = "pink"
)
class AudioEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state
    private val undoStack = mutableListOf<TrimSnapshot>()
    private val redoStack = mutableListOf<TrimSnapshot>()
    private val historyMax = 20
    init {
        loadHistory()
        loadLang()
        loadTheme()
    }
    private fun loadLang() {
        val code = getApplication<Application>()
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("lang", "en") ?: "en"
        _state.value = _state.value.copy(lang = code)
    }
    private fun loadTheme() {
        val p = getApplication<Application>().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val valid = listOf("pink", "green", "olive", "yellow", "brown", "red", "cyan")
        val raw = p.getString("colorPreset", null)
        val preset = if (raw == null || raw == "default" || raw !in valid) "pink" else raw
        val dynamic = if (p.contains("dynamicColor")) p.getBoolean("dynamicColor", false)
        else android.os.Build.VERSION.SDK_INT >= 31
        _state.value = _state.value.copy(
            themeMode = p.getString("themeMode", "system") ?: "system",
            dynamicColor = dynamic,
            colorPreset = preset
        )
    }
    private fun saveThemePrefs() {
        val s = _state.value
        getApplication<Application>().getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putString("themeMode", s.themeMode)
            .putBoolean("dynamicColor", s.dynamicColor)
            .putString("colorPreset", s.colorPreset)
            .apply()
    }
    fun setThemeMode(m: String) { _state.value = _state.value.copy(themeMode = m); saveThemePrefs() }
    fun setDynamicColor(b: Boolean) { _state.value = _state.value.copy(dynamicColor = b); saveThemePrefs() }
    fun setColorPreset(p: String) { _state.value = _state.value.copy(colorPreset = p); saveThemePrefs() }
    fun setLang(code: String) {
        _state.value = _state.value.copy(lang = code)
        getApplication<Application>()
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().putString("lang", code).apply()
    }
    fun navigate(screen: EditorScreen) =
        _state.value.let { _state.value = it.copy(currentScreen = screen, message = null) }
    fun addFile(file: AudioFile) =
        _state.value.let { _state.value = it.copy(selectedFiles = it.selectedFiles + file) }
    fun removeFile(id: String) =
        _state.value.let { _state.value = it.copy(selectedFiles = it.selectedFiles.filter { f -> f.id != id }) }
    fun setMessage(msg: String?, isError: Boolean = false) =
        _state.value.let { _state.value = it.copy(message = msg, messageIsError = isError) }
    private fun setLoading(loading: Boolean) = _state.value.let { _state.value = it.copy(isLoading = loading) }
    fun updateTrim(start: Long, end: Long) = _state.value.let { _state.value = it.copy(trimStart = start, trimEnd = end) }
    fun setFadeIn(sec: Int) = _state.value.let { _state.value = it.copy(fadeInSec = sec) }
    fun setFadeOut(sec: Int) = _state.value.let { _state.value = it.copy(fadeOutSec = sec) }
    fun setFadeInEnabled(b: Boolean) = _state.value.let {
        _state.value = it.copy(fadeInEnabled = b, fadeInSec = if (b && it.fadeInSec == 0) 2 else it.fadeInSec)
    }
    fun setFadeOutEnabled(b: Boolean) = _state.value.let {
        _state.value = it.copy(fadeOutEnabled = b, fadeOutSec = if (b && it.fadeOutSec == 0) 2 else it.fadeOutSec)
    }
    fun setVolume(v: Float) = _state.value.let { _state.value = it.copy(volumeMultiplier = v) }
    fun setSpeed(s: Float) = _state.value.let { _state.value = it.copy(speedMultiplier = s) }
    fun setEqGain(band: Int, gain: Float) = _state.value.let {
        val list = it.eqGains.toMutableList(); list[band] = gain
        _state.value = it.copy(eqGains = list)
    }
    fun setFormat(fmt: String) = _state.value.let { _state.value = it.copy(selectedFormat = fmt) }
    fun setBitrate(b: Int) = _state.value.let { _state.value = it.copy(bitrate = b) }
    fun setVideoUri(uri: Uri) = _state.value.let { _state.value = it.copy(videoUri = uri) }
    fun setSaveFileName(name: String) = _state.value.let { _state.value = it.copy(saveFileName = name) }
    fun setTrimOutputFormat(fmt: String) = _state.value.let { _state.value = it.copy(trimOutputFormat = fmt) }
    fun setCrossfadeEnabled(b: Boolean) = _state.value.let { _state.value = it.copy(crossfadeEnabled = b) }
    fun setCrossfadeDuration(sec: Int) = _state.value.let { _state.value = it.copy(crossfadeDuration = sec) }
    fun setVideoTrimUri(uri: Uri) = _state.value.let { _state.value = it.copy(videoTrimUri = uri) }
    fun setVideoToolsUri(uri: Uri) = _state.value.let { _state.value = it.copy(videoToolsUri = uri) }
    fun setVideoLossless(b: Boolean) = _state.value.let { _state.value = it.copy(videoLossless = b) }
    fun setMetadataTitle(t: String) = _state.value.let {
        _state.value = it.copy(metadata = it.metadata.copy(title = t), titleDirty = true)
    }
    fun setMetadataArtist(a: String) = _state.value.let {
        _state.value = it.copy(metadata = it.metadata.copy(artist = a), artistDirty = true)
    }
    fun setMetadataAlbum(a: String) = _state.value.let {
        _state.value = it.copy(metadata = it.metadata.copy(album = a), albumDirty = true)
    }
    fun setMetadataYear(y: Int) = _state.value.let {
        _state.value = it.copy(metadata = it.metadata.copy(year = y), yearDirty = true)
    }
    fun setMetadataCover(uri: Uri) = _state.value.let {
        _state.value = it.copy(metadataCoverUri = uri, coverTouched = true)
    }
    fun removeMetadataCover() = _state.value.let {
        _state.value = it.copy(metadataCoverUri = null, coverTouched = true)
    }
    fun prefillMetadata(title: String, artist: String, album: String, year: Int) = _state.value.let {
        _state.value = it.copy(
            metadata = it.metadata.copy(title = title, artist = artist, album = album, year = year),
            titleDirty = false, artistDirty = false, albumDirty = false, yearDirty = false,
            coverTouched = false, metadataCoverUri = null
        )
    }
    fun reorderFiles(from: Int, to: Int) = _state.value.let {
        val list = it.selectedFiles.toMutableList()
        if (from !in list.indices || to !in list.indices) return@let
        val item = list.removeAt(from)
        list.add(to, item)
        _state.value = it.copy(selectedFiles = list)
    }
    fun applyPreset(seconds: Int) {
        val file = _state.value.selectedFiles.firstOrNull() ?: return
        val end = (seconds * 1000L).coerceAtMost(file.duration)
        updateTrim(0, end)
    }
    fun saveSnapshot() {
        val s = _state.value
        undoStack.add(TrimSnapshot(s.trimStart, s.trimEnd, s.fadeInSec, s.fadeOutSec, s.volumeMultiplier, s.speedMultiplier, s.eqGains))
        if (undoStack.size > 30) undoStack.removeAt(0)
        redoStack.clear()
    }
    fun undo() {
        if (undoStack.isEmpty()) return
        val prev = undoStack.removeAt(undoStack.lastIndex)
        val cur = _state.value
        redoStack.add(TrimSnapshot(cur.trimStart, cur.trimEnd, cur.fadeInSec, cur.fadeOutSec, cur.volumeMultiplier, cur.speedMultiplier, cur.eqGains))
        _state.value = cur.copy(trimStart = prev.startMs, trimEnd = prev.endMs, fadeInSec = prev.fadeIn, fadeOutSec = prev.fadeOut, volumeMultiplier = prev.volume, speedMultiplier = prev.speed, eqGains = prev.eqGains)
    }
    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        val cur = _state.value
        undoStack.add(TrimSnapshot(cur.trimStart, cur.trimEnd, cur.fadeInSec, cur.fadeOutSec, cur.volumeMultiplier, cur.speedMultiplier, cur.eqGains))
        _state.value = cur.copy(trimStart = next.startMs, trimEnd = next.endMs, fadeInSec = next.fadeIn, fadeOutSec = next.fadeOut, volumeMultiplier = next.volume, speedMultiplier = next.speed, eqGains = next.eqGains)
    }
    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val file = File(getApplication<Application>().cacheDir, "history.json")
                if (file.exists()) {
                    val arr = JSONArray(file.readText())
                    val list = mutableListOf<HistoryItem>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        list.add(HistoryItem(o.getString("id"), o.getString("name"), Uri.parse(o.getString("uri")), o.getString("action"), o.getLong("ts")))
                    }
                    _state.value = _state.value.copy(history = list)
                }
            } catch (e: Exception) {  }
        }
    }
    private fun saveHistory() {
        viewModelScope.launch {
            try {
                val arr = JSONArray()
                _state.value.history.forEach { h ->
                    arr.put(JSONObject().put("id", h.id).put("name", h.name).put("uri", h.uri.toString()).put("action", h.action).put("ts", h.timestamp))
                }
                File(getApplication<Application>().cacheDir, "history.json").writeText(arr.toString())
            } catch (e: Exception) {  }
        }
    }
    private fun addHistory(name: String, uri: Uri, action: String) {
        val item = HistoryItem(UUID.randomUUID().toString(), name, uri, action, System.currentTimeMillis())
        val list = (listOf(item) + _state.value.history).take(historyMax)
        _state.value = _state.value.copy(history = list)
        saveHistory()
    }
    fun clearHistory() {
        _state.value = _state.value.copy(history = emptyList())
        saveHistory()
    }
    private fun extOf(ctx: Context, uri: Uri): String =
        DocumentFile.fromSingleUri(ctx, uri)?.name?.substringAfterLast('.', "mp4")?.lowercase() ?: "mp4"
    private fun buildAudioFilter(totalDurationMs: Long): String {
        val s = _state.value
        val durSec = totalDurationMs / 1000.0
        val parts = mutableListOf<String>()
        if (s.fadeInEnabled && s.fadeInSec > 0) {
            val d = minOf(s.fadeInSec.toDouble(), durSec / 2).coerceAtLeast(0.1)
            parts.add("afade=t=in:st=0:d=$d")
        }
        if (s.fadeOutEnabled && s.fadeOutSec > 0) {
            val d = minOf(s.fadeOutSec.toDouble(), durSec / 2).coerceAtLeast(0.1)
            val st = (durSec - d).coerceAtLeast(0.0)
            parts.add("afade=t=out:st=$st:d=$d")
        }
        if (s.volumeMultiplier != 1.0f) parts.add("volume=${s.volumeMultiplier}")
        if (s.speedMultiplier != 1.0f) {
            var sp = s.speedMultiplier
            while (sp > 2.0f) { parts.add("atempo=2.0"); sp /= 2.0f }
            while (sp < 0.5f) { parts.add("atempo=0.5"); sp /= 0.5f }
            parts.add("atempo=$sp")
        }
        val eqFreqs = listOf(60, 230, 910, 3600, 14000)
        s.eqGains.forEachIndexed { i, g ->
            if (g != 0f) parts.add("equalizer=f=${eqFreqs[i]}:t=q:w=1.5:g=$g")
        }
        return if (parts.isEmpty()) "" else "-af ${parts.joinToString(",")}"
    }
    private fun metadataArgs(): String {
        val s = _state.value
        val sb = StringBuilder()
        if (s.metadata.title.isNotBlank()) sb.append(" -metadata title=\"${s.metadata.title}\"")
        if (s.metadata.artist.isNotBlank()) sb.append(" -metadata artist=\"${s.metadata.artist}\"")
        if (s.metadata.album.isNotBlank()) sb.append(" -metadata album=\"${s.metadata.album}\"")
        if (s.metadata.year > 0) sb.append(" -metadata date=\"${s.metadata.year}\"")
        return sb.toString()
    }
    private fun bitrateFlag(): String = "-b:a ${_state.value.bitrate}k"
    private suspend fun runFfmpeg(cmd: String): FFmpegSession =
        withContext(Dispatchers.IO) { FFmpegKit.execute(cmd) }
    private fun readTags(ctx: Context, uri: Uri): Tags {
        val r = android.media.MediaMetadataRetriever()
        return try {
            r.setDataSource(ctx, uri)
            Tags(
                r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "",
                r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_AUTHOR) ?: "",
                r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "",
                r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DATE)?.toIntOrNull() ?: 0
            )
        } catch (e: Exception) { Tags() } finally { r.release() }
    }
    fun trimAudio(inputUri: Uri, startMs: Long, endMs: Long, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val s = _state.value
                val durMs = (endMs - startMs).coerceAtLeast(1L)
                val input = copyToCache(ctx, inputUri, "in_trim.mp3")
                val stage1 = File(ctx.cacheDir, "stage1_trim.mkv")
                val output = File(ctx.cacheDir, "out_trim.${s.trimOutputFormat}")
                val cut = runFfmpeg(
                    "-y -ss ${startMs / 1000.0} -i ${input.absolutePath} -t ${durMs / 1000.0} " +
                            "-map 0:a:0 -c copy ${stage1.absolutePath}"
                )
                if (!cut.returnCode.isValueSuccess || !stage1.exists() || stage1.length() == 0L) {
                    setMessage(cut.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                    input.delete(); stage1.delete()
                    setLoading(false)
                    return@launch
                }
                val filter = buildAudioFilter(durMs)
                val codec = when (s.trimOutputFormat) {
                    "mp3" -> "-acodec libmp3lame"
                    "m4a" -> "-acodec aac -movflags +faststart"
                    "ogg" -> "-acodec libvorbis"
                    "flac" -> "-acodec flac"
                    "wav" -> "-acodec pcm_s16le"
                    else -> "-acodec libmp3lame"
                }
                val brFlag = if (s.trimOutputFormat in listOf("mp3", "m4a", "ogg")) bitrateFlag() else ""
                val cmd = "-y -i ${stage1.absolutePath} $filter $codec $brFlag${metadataArgs()} ${output.absolutePath}"
                val session = runFfmpeg(cmd)
                if (session.returnCode.isValueSuccess && output.length() > 0) {
                    copyToUri(ctx, output, outputUri)
                    addHistory(state.value.saveFileName.ifBlank { "trimmed.${s.trimOutputFormat}" }, outputUri, "trimmed")
                    setMessage(getApplication<Application>().getString(R.string.msg_trim_ok, s.trimOutputFormat.uppercase()))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete(); stage1.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun trimAudioLossless(inputUri: Uri, startMs: Long, endMs: Long, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val file = state.value.selectedFiles.firstOrNull()
                    ?: throw IllegalStateException("Нет файла")
                val ext = file.name.substringAfterLast('.', "mp3").lowercase()
                val input = copyToCache(ctx, inputUri, "in_lossless.$ext")
                val output = File(ctx.cacheDir, "out_lossless.$ext")
                val cmd = "-y -i ${input.absolutePath} -ss ${startMs / 1000.0} -to ${endMs / 1000.0} -c copy -map 0 ${output.absolutePath}"
                val session = runFfmpeg(cmd)
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory(state.value.saveFileName.ifBlank { "trimmed.$ext" }, outputUri, "trimmed_lossless")
                    setMessage(getApplication<Application>().getString(R.string.msg_trim_lossless_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun trimVideoLossless(inputUri: Uri, startMs: Long, endMs: Long, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val ext = extOf(ctx, inputUri)
                val input = copyToCache(ctx, inputUri, "in_vtrim.$ext")
                val output = File(ctx.cacheDir, "out_vtrim.$ext")
                val seekSec = startMs / 1000.0
                val durationSec = ((endMs - startMs).coerceAtLeast(1L)) / 1000.0
                val isMp4Like = ext.lowercase() in listOf("mp4", "m4v", "mov", "3gp")
                val movFlag = if (isMp4Like) " -movflags +faststart" else ""
                val cmd = "-y -ss $seekSec -i ${input.absolutePath} -t $durationSec " +
                        "-c copy -map 0:v? -map 0:a? -copyinkf $movFlag ${output.absolutePath}"
                val session = runFfmpeg(cmd)
                if (session.returnCode.isValueSuccess && output.length() > 0) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("video_trimmed.$ext", outputUri, "video_trim")
                    setMessage(getApplication<Application>().getString(R.string.msg_vtrim_lossless_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun trimVideoReencode(inputUri: Uri, startMs: Long, endMs: Long, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, inputUri, "in_vtrim.mp4")
                val output = File(ctx.cacheDir, "out_vtrim.mp4")
                val cmd = "-y -i ${input.absolutePath} -ss ${startMs / 1000.0} -to ${endMs / 1000.0} " +
                        "-c:v libopenh264 -b:v 4000k -c:a aac -b:a 192k -movflags +faststart ${output.absolutePath}"
                val session = runFfmpeg(cmd)
                if (session.returnCode.isValueSuccess && output.length() > 0) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("video_trimmed.mp4", outputUri, "video_trim")
                    setMessage(getApplication<Application>().getString(R.string.msg_vtrim_reencode_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun applyMetadataBatch(coverUri: Uri?) {
        val files = _state.value.selectedFiles.toList()
        if (files.isEmpty()) {
            setMessage(getApplication<Application>().getString(R.string.msg_no_files), true)
            return
        }
        viewModelScope.launch {
            setLoading(true)
            val ctx = getApplication<Application>()
            val cover = if (coverUri != null) copyToCache(ctx, coverUri, "cover.jpg") else null
            var ok = 0
            var lastErr = ""
            val failed = mutableListOf<String>()
            for (file in files) {
                try {
                    val cur = readTags(ctx, file.uri)
                    val s = _state.value
                    val title = if (s.titleDirty) s.metadata.title else cur.title
                    val artist = if (s.artistDirty) s.metadata.artist else cur.artist
                    val album = if (s.albumDirty) s.metadata.album else cur.album
                    val year = if (s.yearDirty) s.metadata.year else cur.year
                    val ext = file.name.substringAfterLast('.', "").lowercase()
                    val outExt = when (ext) {
                        "mp3" -> "mp3"; "m4a", "aac" -> "m4a"; "wav" -> "wav"
                        "ogg", "oga" -> "ogg"; "flac" -> "flac"; else -> "mp3"
                    }
                    val canCover = outExt in listOf("mp3", "m4a")
                    val input = copyToCache(ctx, file.uri, "tag_in.$outExt")
                    val tmp = File(ctx.cacheDir, "tag_out.$outExt")
                    val sb = StringBuilder("-y -i ${input.absolutePath}")
                    when {
                        cover != null && canCover ->
                            sb.append(" -i ${cover.absolutePath} -map 0:a -map 1:v -c:a copy -c:v mjpeg -disposition:v attached_pic")
                        s.coverTouched ->
                            sb.append(" -map 0:a -c copy")
                        else ->
                            sb.append(" -map 0 -c copy")
                    }
                    if (outExt == "mp3") sb.append(" -id3v2_version 3")
                    if (title.isNotBlank()) sb.append(" -metadata title=\"$title\"")
                    if (artist.isNotBlank()) sb.append(" -metadata artist=\"$artist\"")
                    if (album.isNotBlank()) sb.append(" -metadata album=\"$album\"")
                    if (year > 0) sb.append(" -metadata date=\"$year\"")
                    sb.append(" ${tmp.absolutePath}")
                    val session = runFfmpeg(sb.toString())
                    if (session.returnCode.isValueSuccess && tmp.exists() && tmp.length() > 0) {
                        ctx.contentResolver.openOutputStream(file.uri, "wt")?.use { o ->
                            tmp.inputStream().use { it.copyTo(o) }
                        }
                        ok++
                    } else {
                        lastErr = session.output?.take(240)
                            ?: session.failStackTrace?.take(240) ?: "FFmpeg returned empty file"
                        failed.add(file.name)
                    }
                    input.delete(); tmp.delete()
                } catch (e: Throwable) {
                    lastErr = e.message ?: e.toString()
                    failed.add(file.name)
                }
            }
            cover?.delete()
            addHistory("tagged_x$ok", files.first().uri, "tagged")
            val finalMsg = when {
                ok == files.size -> ctx.getString(R.string.msg_meta_ok, ok)
                ok > 0 -> ctx.getString(R.string.msg_meta_partial, ok, files.size, failed.joinToString(", "))
                else -> if (lastErr.isBlank()) ctx.getString(R.string.msg_meta_failed)
                else ctx.getString(R.string.msg_meta_err, lastErr)
            }
            setMessage(finalMsg, ok == 0)
            setLoading(false)
        }
    }
    fun mergeAudios(uris: List<Uri>, volumes: List<Float>, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val inputs = uris.mapIndexed { i, u -> copyToCache(ctx, u, "m_$i.mp3") }
                val prepared = inputs.mapIndexed { i, f ->
                    val v = volumes.getOrElse(i) { 1.0f }
                    val pre = File(ctx.cacheDir, "m_pre_$i.mp3")
                    val cmd = if (v == 1.0f) "-y -i ${f.absolutePath} -c copy ${pre.absolutePath}"
                    else "-y -i ${f.absolutePath} -af volume=$v ${pre.absolutePath}"
                    runFfmpeg(cmd)
                    pre
                }
                val list = File(ctx.cacheDir, "concat.txt")
                list.writeText(prepared.joinToString("\n") { "file '${it.absolutePath}'" })
                    val output = File(ctx.cacheDir, "out_merge.mp3")
                val session = runFfmpeg("-y -f concat -safe 0 -i ${list.absolutePath} -c copy ${output.absolutePath}")
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("merged.mp3", outputUri, "merged")
                    setMessage(getApplication<Application>().getString(R.string.msg_merge_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                inputs.forEach { it.delete() }; prepared.forEach { it.delete() }; list.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun mergeAudiosWithCrossfade(uris: List<Uri>, volumes: List<Float>, crossfadeSec: Int, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val inputs = uris.mapIndexed { i, u -> copyToCache(ctx, u, "m_$i.mp3") }
                val output = File(ctx.cacheDir, "out_merge.mp3")
                val sb = StringBuilder()
                var prev = "[0:a]"
                for (i in 1 until inputs.size) {
                    val out = if (i == inputs.size - 1) "[aout]" else "[a$i]"
                    sb.append(prev).append("[").append(i).append(":a]acrossfade=d=")
                        .append(crossfadeSec).append(":c1=tri:c2=tri").append(out).append(";")
                    prev = out
                }
                val filter = sb.toString().removeSuffix(";")
                val inputsStr = inputs.joinToString(" ") { "-i ${it.absolutePath}" }
                val session = runFfmpeg("-y $inputsStr -filter_complex $filter -map [aout] ${output.absolutePath}")
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("merged_crossfade.mp3", outputUri, "merged")
                    setMessage(getApplication<Application>().getString(R.string.msg_merge_cross_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                inputs.forEach { it.delete() }
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun convertAudio(inputUri: Uri, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val s = _state.value
                val input = copyToCache(ctx, inputUri, "in_conv.mp3")
                val output = File(ctx.cacheDir, "out_conv.${s.selectedFormat}")
                val brFlag = if (s.selectedFormat in listOf("mp3", "m4a", "ogg")) bitrateFlag() else ""
                val codec = when (s.selectedFormat) {
                    "mp3" -> "-acodec libmp3lame"
                    "m4a" -> "-acodec aac"
                    "ogg" -> "-acodec libvorbis"
                    "flac" -> "-acodec flac"
                    "wav" -> "-acodec pcm_s16le"
                    else -> ""
                }
                val session = runFfmpeg("-y -i ${input.absolutePath} -map 0:a:0 $codec $brFlag ${output.absolutePath}")
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("converted.${s.selectedFormat}", outputUri, "converted")
                    setMessage(getApplication<Application>().getString(R.string.msg_conv_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun splitAudio(inputUri: Uri, splitAtMs: Long, outputUri1: Uri, outputUri2: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, inputUri, "in_split.mp3")
                val out1 = File(ctx.cacheDir, "out_split1.mp3")
                val out2 = File(ctx.cacheDir, "out_split2.mp3")
                val s1 = runFfmpeg("-y -i ${input.absolutePath} -t ${splitAtMs / 1000.0} -c copy ${out1.absolutePath}")
                val s2 = runFfmpeg("-y -ss ${splitAtMs / 1000.0} -i ${input.absolutePath} -c copy ${out2.absolutePath}")
                if (s1.returnCode.isValueSuccess && s2.returnCode.isValueSuccess) {
                    copyToUri(ctx, out1, outputUri1)
                    copyToUri(ctx, out2, outputUri2)
                    addHistory("part1.mp3", outputUri1, "split")
                    addHistory("part2.mp3", outputUri2, "split")
                    setMessage(getApplication<Application>().getString(R.string.msg_split_ok))
                } else setMessage(getApplication<Application>().getString(R.string.msg_split_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun extractAudio(videoUri: Uri, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, videoUri, "in_video.mp4")
                val output = File(ctx.cacheDir, "out_extract.${_state.value.selectedFormat}")
                val codec = when (_state.value.selectedFormat) {
                    "mp3" -> "-acodec libmp3lame ${bitrateFlag()}"
                    "m4a" -> "-acodec aac ${bitrateFlag()}"
                    "wav" -> "-acodec pcm_s16le"
                    else -> "-acodec libmp3lame ${bitrateFlag()}"
                }
                val session = runFfmpeg("-y -i ${input.absolutePath} -vn $codec ${output.absolutePath}")
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("extracted_audio.${_state.value.selectedFormat}", outputUri, "extracted")
                    setMessage(getApplication<Application>().getString(R.string.msg_extract_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun reverseAudio(inputUri: Uri, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, inputUri, "in_reverse.mp3")
                val output = File(ctx.cacheDir, "out_reverse.mp3")
                val session = runFfmpeg("-y -i ${input.absolutePath} -af areverse ${output.absolutePath}")
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("reversed.mp3", outputUri, "reversed")
                    setMessage(getApplication<Application>().getString(R.string.msg_reverse_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun loopAudio(inputUri: Uri, loopCount: Int, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, inputUri, "in_loop.mp3")
                val output = File(ctx.cacheDir, "out_loop.mp3")
                val session = runFfmpeg("-y -stream_loop $loopCount -i ${input.absolutePath} -c copy ${output.absolutePath}")
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("looped_x$loopCount.mp3", outputUri, "looped")
                    setMessage(getApplication<Application>().getString(R.string.msg_loop_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun denoiseAudio(inputUri: Uri, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, inputUri, "in_denoise.mp3")
                val output = File(ctx.cacheDir, "out_denoise.mp3")
                val session = runFfmpeg("-y -i ${input.absolutePath} -af afftdn=nf=-25 ${output.absolutePath}")
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("denoised.mp3", outputUri, "denoised")
                    setMessage(getApplication<Application>().getString(R.string.msg_denoise_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun normalizeAudio(inputUri: Uri, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, inputUri, "in_normalize.mp3")
                val output = File(ctx.cacheDir, "out_normalize.mp3")
                val session = runFfmpeg("-y -i ${input.absolutePath} -af loudnorm=I=-16:TP=-1.5:LRA=11 ${output.absolutePath}")
                if (session.returnCode.isValueSuccess) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("normalized.mp3", outputUri, "normalized")
                    setMessage(getApplication<Application>().getString(R.string.msg_normalize_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun batchConvertAudio(outputUri: Uri) {
        val files = _state.value.selectedFiles.toList()
        if (files.isEmpty()) {
            setMessage(getApplication<Application>().getString(R.string.msg_no_files), true)
            return
        }
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val fmt = _state.value.selectedFormat
                val brFlag = if (fmt in listOf("mp3", "m4a", "ogg")) bitrateFlag() else ""
                val codec = when (fmt) {
                    "mp3" -> "-acodec libmp3lame"
                    "m4a" -> "-acodec aac"
                    "ogg" -> "-acodec libvorbis"
                    "flac" -> "-acodec flac"
                    "wav" -> "-acodec pcm_s16le"
                    else -> ""
                }
                var ok = 0
                for ((idx, file) in files.withIndex()) {
                    val input = copyToCache(ctx, file.uri, "batch_$idx.mp3")
                    val outName = "${file.name.substringBeforeLast('.', "audio")}_conv.$fmt"
                    val outUri = Uri.withAppendedPath(outputUri, outName)
                    val output = File(ctx.cacheDir, "batch_out_$idx.$fmt")
                    val session = runFfmpeg("-y -i ${input.absolutePath} $codec $brFlag ${output.absolutePath}")
                    if (session.returnCode.isValueSuccess) {
                        copyToUri(ctx, output, outUri)
                        ok++
                    }
                    input.delete()
                }
                addHistory("batch_converted_x$ok", outputUri, "batch_converted")
                setMessage(getApplication<Application>().getString(R.string.msg_batch_ok, ok, files.size))
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    private fun copyToCache(ctx: Context, uri: Uri, name: String): File {
        val f = File(ctx.cacheDir, "${UUID.randomUUID()}_$name")
        ctx.contentResolver.openInputStream(uri)?.use { i -> FileOutputStream(f).use { o -> i.copyTo(o) } }
        return f
    }
    private fun copyToUri(ctx: Context, src: File, dest: Uri) {
        ctx.contentResolver.openOutputStream(dest)?.use { o -> src.inputStream().use { it.copyTo(o) } }
        src.delete()
    }
    fun videoToGif(inputUri: Uri, startSec: Double, endSec: Double, fps: Int, width: Int, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, inputUri, "in_gif.mp4")
                val output = File(ctx.cacheDir, "out.gif")
                val dur = (endSec - startSec).coerceAtLeast(0.1)
                val fc = "[0:v]fps=$fps,scale=$width:-1:flags=lanczos,split[a][b];" +
                        "[a]palettegen[p];[b][p]paletteuse"
                val session = runFfmpeg(
                    "-y -ss $startSec -t $dur -i ${input.absolutePath} " +
                            "-filter_complex \"$fc\" -loop 0 ${output.absolutePath}"
                )
                if (session.returnCode.isValueSuccess && output.length() > 0) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("clip.gif", outputUri, "gif")
                    setMessage(getApplication<Application>().getString(R.string.msg_gif_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun transformVideo(inputUri: Uri, width: Int, height: Int, transpose: Int, mirror: Boolean, speed: Double, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val input = copyToCache(ctx, inputUri, "in_tf.mp4")
                val output = File(ctx.cacheDir, "out_tf.mp4")
                val parts = mutableListOf<String>()
                parts.add("scale=$width:$height")
                if (transpose in 0..7) parts.add("transpose=$transpose")
                if (mirror) parts.add("hflip")
                val sp = speed.coerceIn(0.5, 2.0)
                if (sp != 1.0) parts.add("setpts=PTS/$sp")
                val vf = parts.joinToString(",")
                val af = if (sp != 1.0) " -af atempo=$sp" else ""
                val session = runFfmpeg(
                    "-y -i ${input.absolutePath} -vf \"$vf\"$af " +
                            "-c:v libx264 -crf 23 -preset medium -pix_fmt yuv420p -c:a aac -movflags +faststart ${output.absolutePath}"
                )
                if (session.returnCode.isValueSuccess && output.length() > 0) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("transformed.mp4", outputUri, "video_transform")
                    setMessage(getApplication<Application>().getString(R.string.msg_tf_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                input.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun replaceVideoAudio(videoUri: Uri, audioUri: Uri, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val v = copyToCache(ctx, videoUri, "in_va.mp4")
                val a = copyToCache(ctx, audioUri, "in_va_audio.m4a")
                val output = File(ctx.cacheDir, "out_va.mp4")
                val session = runFfmpeg(
                    "-y -i ${v.absolutePath} -i ${a.absolutePath} " +
                            "-map 0:v:0 -map 1:a:0 -c:v copy -c:a aac -b:a 192k -shortest -movflags +faststart ${output.absolutePath}"
                )
                if (session.returnCode.isValueSuccess && output.length() > 0) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("with_new_audio.mp4", outputUri, "video_audio_replace")
                    setMessage(getApplication<Application>().getString(R.string.msg_va_replace_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                v.delete(); a.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
    fun removeVideoAudio(videoUri: Uri, outputUri: Uri) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val ctx = getApplication<Application>()
                val v = copyToCache(ctx, videoUri, "in_na.mp4")
                val output = File(ctx.cacheDir, "out_na.mp4")
                val session = runFfmpeg(
                    "-y -i ${v.absolutePath} -map 0:v:0 -an -c:v copy -movflags +faststart ${output.absolutePath}"
                )
                if (session.returnCode.isValueSuccess && output.length() > 0) {
                    copyToUri(ctx, output, outputUri)
                    addHistory("no_audio.mp4", outputUri, "video_audio_remove")
                    setMessage(getApplication<Application>().getString(R.string.msg_va_remove_ok))
                } else setMessage(session.failStackTrace?.take(120) ?: getApplication<Application>().getString(R.string.msg_generic_err), true)
                v.delete()
            } catch (e: Throwable) { setMessage(e.message ?: getApplication<Application>().getString(R.string.msg_generic_err), true) }
            finally { setLoading(false) }
        }
    }
}
object AudioFileUtils {
    fun getAudioDuration(context: Context, uri: Uri): Long {
        val r = android.media.MediaMetadataRetriever()
        return try {
            r.setDataSource(context, uri)
            r.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        } catch (e: Exception) { 0L } finally { r.release() }
    }
}