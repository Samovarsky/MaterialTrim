package com.samovarsky.materialtrim
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samovarsky.materialtrim.ui.theme.MusicEditorTheme
import java.util.Locale
import java.util.UUID
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences("settings", MODE_PRIVATE)
            .getString("lang", "en") ?: "en"
        val conf = Configuration(newBase.resources.configuration)
        conf.setLocale(Locale.forLanguageTag(lang))
        super.attachBaseContext(newBase.createConfigurationContext(conf))
    }
    override fun getResources(): Resources {
        return baseContext.resources
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AudioEditorViewModel = viewModel()
            val st by vm.state.collectAsState()
            MusicEditorTheme(
                themeMode = st.themeMode,
                dynamicColor = st.dynamicColor,
                colorPreset = st.colorPreset
            ) {
                MusicEditorApp(vm)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicEditorApp(viewModel: AudioEditorViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarIsError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(state.message) {
        state.message?.let { msg ->
            snackbarIsError = state.messageIsError
            snackbarHostState.showSnackbar(msg)
            viewModel.setMessage(null)
        }
    }
    val openAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {  }
            viewModel.addFile(
                AudioFile(
                    id = UUID.randomUUID().toString(),
                    name = getFileName(context, uri),
                    uri = uri,
                    duration = AudioFileUtils.getAudioDuration(context, uri),
                    size = 0
                )
            )
        }
    }
    val openVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.setVideoUri(it) } }
    val openVideoTrimLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.setVideoTrimUri(it) } }
    val openCoverLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.setMetadataCover(it) } }
    val saveTrimLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { out ->
            state.selectedFiles.firstOrNull()?.let { f ->
                viewModel.trimAudio(f.uri, state.trimStart, state.trimEnd, out)
            }
        }
    }
    val saveTrimLosslessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { out ->
            state.selectedFiles.firstOrNull()?.let { f ->
                viewModel.trimAudioLossless(f.uri, state.trimStart, state.trimEnd, out)
            }
        }
    }
    val saveVideoTrimLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { out ->
            state.videoTrimUri?.let { v ->
                if (state.videoLossless) viewModel.trimVideoLossless(v, state.trimStart, state.trimEnd, out)
                else viewModel.trimVideoReencode(v, state.trimStart, state.trimEnd, out)
            }
        }
    }
    val saveMergeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { uri ->
        uri?.let { out ->
            val uris = state.selectedFiles.map { it.uri }
            val vols = List(state.selectedFiles.size) { 1.0f }
            if (state.crossfadeEnabled) {
                viewModel.mergeAudiosWithCrossfade(uris, vols, state.crossfadeDuration, out)
            } else {
                viewModel.mergeAudios(uris, vols, out)
            }
        }
    }
    val saveConvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { out ->
            state.selectedFiles.firstOrNull()?.let { f -> viewModel.convertAudio(f.uri, out) }
        }
    }
    var pendingSplitFirst by remember { mutableStateOf<Uri?>(null) }
    var pendingSplitMs by remember { mutableLongStateOf(0L) }
    val saveSplit2Launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { uri2 ->
        val uri1 = pendingSplitFirst
        if (uri1 != null && uri2 != null) {
            state.selectedFiles.firstOrNull()?.let { f ->
                viewModel.splitAudio(f.uri, pendingSplitMs, uri1, uri2)
            }
        }
        pendingSplitFirst = null
    }
    val saveSplit1Launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { uri1 ->
        if (uri1 != null) {
            pendingSplitFirst = uri1
            saveSplit2Launcher.launch("part2.mp3")
        }
    }
    val saveExtractLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let { out ->
            state.videoUri?.let { v -> viewModel.extractAudio(v, out) }
        }
    }
    val saveReverseLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { uri ->
        uri?.let { out ->
            state.selectedFiles.firstOrNull()?.let { f -> viewModel.reverseAudio(f.uri, out) }
        }
    }
    var pendingLoopCount by remember { mutableIntStateOf(2) }
    val saveLoopLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { uri ->
        uri?.let { out ->
            state.selectedFiles.firstOrNull()?.let { f -> viewModel.loopAudio(f.uri, pendingLoopCount, out) }
        }
    }
    val saveDenoiseLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { uri ->
        uri?.let { out ->
            state.selectedFiles.firstOrNull()?.let { f -> viewModel.denoiseAudio(f.uri, out) }
        }
    }
    val saveNormalizeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { uri ->
        uri?.let { out ->
            state.selectedFiles.firstOrNull()?.let { f -> viewModel.normalizeAudio(f.uri, out) }
        }
    }
    val saveBatchConvertLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { out -> viewModel.batchConvertAudio(out) }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 72.dp)
            ) { data ->
                Snackbar(
                    containerColor = if (snackbarIsError) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (snackbarIsError) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (snackbarIsError) Icons.Rounded.Error else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (snackbarIsError) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(data.visuals.message)
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            when (state.currentScreen) {
                                EditorScreen.Home -> R.string.app_name
                                EditorScreen.Trim -> R.string.screen_trim
                                EditorScreen.TrimLossless -> R.string.screen_trim_lossless
                                EditorScreen.VideoTrim -> R.string.screen_video_trim
                                EditorScreen.Merge -> R.string.screen_merge
                                EditorScreen.Converter -> R.string.screen_converter
                                EditorScreen.Split -> R.string.screen_split
                                EditorScreen.ExtractAudio -> R.string.screen_extract
                                EditorScreen.Effects -> R.string.screen_effects
                                EditorScreen.Metadata -> R.string.screen_metadata
                                EditorScreen.History -> R.string.screen_history
                                EditorScreen.VideoTools -> R.string.screen_video_tools
                                EditorScreen.Settings -> R.string.screen_settings
                            }
                        )
                    )
                },
                navigationIcon = {
                    if (state.currentScreen != EditorScreen.Home) {
                        IconButton(onClick = { viewModel.navigate(EditorScreen.Home) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigate(EditorScreen.Settings) }) {
                        Icon(Icons.Default.Settings, stringResource(R.string.screen_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(
                    targetState = state.currentScreen,
                    transitionSpec = {
                        val forward = targetState != EditorScreen.Home
                        (fadeIn(tween(280)) + slideInHorizontally(tween(280)) { if (forward) it / 5 else -it / 5 })
                            .togetherWith(
                                fadeOut(tween(280)) + slideOutHorizontally(tween(280)) { if (forward) -it / 5 else it / 5 }
                            )
                    },
                    label = "screenTransition"
                ) { screen ->
                    when (screen) {
                        EditorScreen.Home -> HomeScreen(
                            state = state,
                            onTrimClick = { viewModel.navigate(EditorScreen.Trim) },
                            onTrimLosslessClick = { viewModel.navigate(EditorScreen.TrimLossless) },
                            onVideoTrimClick = { viewModel.navigate(EditorScreen.VideoTrim) },
                            onVideoToolsClick = { viewModel.navigate(EditorScreen.VideoTools) },
                            onMergeClick = { viewModel.navigate(EditorScreen.Merge) },
                            onConvertClick = { viewModel.navigate(EditorScreen.Converter) },
                            onSplitClick = { viewModel.navigate(EditorScreen.Split) },
                            onExtractClick = { viewModel.navigate(EditorScreen.ExtractAudio) },
                            onEffectsClick = { viewModel.navigate(EditorScreen.Effects) },
                            onMetadataClick = { viewModel.navigate(EditorScreen.Metadata) },
                            onHistoryClick = { viewModel.navigate(EditorScreen.History) },
                            onAddClick = { openAudioLauncher.launch(arrayOf("audio/*", "video/*")) },
                            onRemoveFile = { viewModel.removeFile(it) }
                        )
                        EditorScreen.Trim -> TrimScreen(
                            state = state,
                            onUpdateTrim = viewModel::updateTrim,
                            onUndo = viewModel::undo,
                            onRedo = viewModel::redo,
                            onSaveSnapshot = viewModel::saveSnapshot,
                            onSetFadeIn = viewModel::setFadeIn,
                            onSetFadeOut = viewModel::setFadeOut,
                            onSetFadeInEnabled = viewModel::setFadeInEnabled,
                            onSetFadeOutEnabled = viewModel::setFadeOutEnabled,
                            onSetVolume = viewModel::setVolume,
                            onSetSpeed = viewModel::setSpeed,
                            onSetEqGain = viewModel::setEqGain,
                            onPreset = viewModel::applyPreset,
                            onSetSaveName = viewModel::setSaveFileName,
                            onSetTrimFormat = viewModel::setTrimOutputFormat,
                            onSetTitle = viewModel::setMetadataTitle,
                            onSetArtist = viewModel::setMetadataArtist,
                            onSetAlbum = viewModel::setMetadataAlbum,
                            onSetYear = viewModel::setMetadataYear,
                            onPrefill = viewModel::prefillMetadata,
                            onSave = { saveTrimLauncher.launch("${state.saveFileName.ifBlank { "trimmed" }}.${state.trimOutputFormat}") },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.TrimLossless -> TrimLosslessScreen(
                            state = state,
                            onUpdateTrim = viewModel::updateTrim,
                            onSetSaveName = viewModel::setSaveFileName,
                            onSave = {
                                val ext = state.selectedFiles.firstOrNull()?.name?.substringAfterLast('.', "mp3") ?: "mp3"
                                saveTrimLosslessLauncher.launch("${state.saveFileName.ifBlank { "trimmed" }}.$ext")
                            },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.VideoTrim -> VideoTrimScreen(
                            state = state,
                            onUpdateTrim = viewModel::updateTrim,
                            onSetLossless = viewModel::setVideoLossless,
                            onPickVideo = { openVideoTrimLauncher.launch(arrayOf("video/*")) },
                            onSetSaveName = viewModel::setSaveFileName,
                            onSave = {
                                val ext = if (state.videoLossless) {
                                    state.videoTrimUri?.let { getFileName(context, it).substringAfterLast('.', "mp4") } ?: "mp4"
                                } else "mp4"
                                saveVideoTrimLauncher.launch("${state.saveFileName.ifBlank { "trimmed_video" }}.$ext")
                            },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.Merge -> MergeScreen(
                            state = state,
                            onSetCrossfadeEnabled = viewModel::setCrossfadeEnabled,
                            onSetCrossfadeDuration = viewModel::setCrossfadeDuration,
                            onMove = viewModel::reorderFiles,
                            onSave = { saveMergeLauncher.launch("merged.mp3") },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.Converter -> ConverterScreen(
                            state = state,
                            onSetFormat = viewModel::setFormat,
                            onSetBitrate = viewModel::setBitrate,
                            onSave = { saveConvLauncher.launch("converted.${state.selectedFormat}") },
                            onBatchConvert = { saveBatchConvertLauncher.launch(null) },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.Split -> SplitScreen(
                            state = state,
                            onSave = { at ->
                                pendingSplitMs = at
                                saveSplit1Launcher.launch("part1.mp3")
                            },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.ExtractAudio -> ExtractAudioScreen(
                            state = state,
                            onPickVideo = { openVideoLauncher.launch(arrayOf("video/*")) },
                            onSetFormat = viewModel::setFormat,
                            onSave = { saveExtractLauncher.launch("extracted.${state.selectedFormat}") },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.Effects -> EffectsScreen(
                            state = state,
                            onReverse = { saveReverseLauncher.launch("reversed.mp3") },
                            onLoop = { count ->
                                pendingLoopCount = count
                                saveLoopLauncher.launch("looped_x$count.mp3")
                            },
                            onDenoise = { saveDenoiseLauncher.launch("denoised.mp3") },
                            onNormalize = { saveNormalizeLauncher.launch("normalized.mp3") },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.Metadata -> MetadataScreen(
                            state = state,
                            onPrefill = viewModel::prefillMetadata,
                            onSetTitle = viewModel::setMetadataTitle,
                            onSetArtist = viewModel::setMetadataArtist,
                            onSetAlbum = viewModel::setMetadataAlbum,
                            onSetYear = viewModel::setMetadataYear,
                            onPickCover = { openCoverLauncher.launch(arrayOf("image/*")) },
                            onRemoveCover = { viewModel.removeMetadataCover() },
                            onApply = { viewModel.applyMetadataBatch(state.metadataCoverUri) },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.History -> HistoryScreen(
                            state = state,
                            onClear = { viewModel.clearHistory() },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.VideoTools -> VideoToolsScreen(
                            state = state,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                        EditorScreen.Settings -> SettingsScreen(
                            state = state,
                            onSetLang = { code ->
                                viewModel.setLang(code)
                                (context as? Activity)?.recreate()
                            },
                            onSetThemeMode = { viewModel.setThemeMode(it) },
                            onSetDynamic = { viewModel.setDynamicColor(it) },
                            onSetColorPreset = { viewModel.setColorPreset(it) },
                            onBackClick = { viewModel.navigate(EditorScreen.Home) }
                        )
                    }
                }
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
fun getFileName(context: Context, uri: Uri): String =
    try { DocumentFile.fromSingleUri(context, uri)?.name ?: "Unknown" } catch (e: Exception) { "Unknown" }