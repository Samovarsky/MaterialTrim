package com.samovarsky.materialtrim

import android.net.Uri

data class AudioFile(
    val id: String,
    val name: String,
    val uri: Uri,
    val duration: Long,
    val size: Long
)


data class HistoryItem(
    val id: String,
    val name: String,
    val uri: Uri,
    val action: String,
    val timestamp: Long
)


data class TrimSnapshot(
    val startMs: Long,
    val endMs: Long,
    val fadeIn: Int,
    val fadeOut: Int,
    val volume: Float,
    val speed: Float,
    val eqGains: List<Float>
)

data class ProcessingTask(
    val id: String,
    val type: String,
    val fileName: String,
    val progress: Int = 0,
    val status: String = "processing" // processing, completed, error
)


data class AudioMetadata(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val year: Int = 0,
    val coverUri: Uri? = null
)