package com.ax3lmt.listropik.model

import android.net.Uri

data class MusicFile(
    val id: String,
    val title: String,
    val artist: String = "Unknown Artist",
    val path: String,
    val uri: Uri,
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L
)
