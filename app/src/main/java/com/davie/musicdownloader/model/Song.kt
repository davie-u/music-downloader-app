package com.davie.musicdownloader.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUrl: String,
    val previewUrl: String,
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false
)
