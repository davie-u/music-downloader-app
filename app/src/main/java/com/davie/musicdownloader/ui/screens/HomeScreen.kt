package com.davie.musicdownloader.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davie.musicdownloader.model.Song
import com.davie.musicdownloader.ui.components.SongCard
import com.davie.musicdownloader.viewmodel.MusicViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import java.io.File

@Composable
fun HomeScreen(viewModel: MusicViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedSong by remember { mutableStateOf<Song?>(null) }

    val exoPlayer = remember(context) { ExoPlayer.Builder(context).build() }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Music Downloader",
                style = MaterialTheme.typography.headlineMedium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Search artist or song") }
                )

                Button(
                    onClick = viewModel::searchSongs,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            uiState.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.songs) { song ->
                    SongCard(
                        title = song.title,
                        artist = song.artist,
                        artworkUrl = song.artworkUrl,
                        isDownloaded = song.isDownloaded,
                        onClick = {
                            selectedSong = song
                            val localPath = song.localFilePath
                            if (localPath != null) {
                                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(localPath))))
                                exoPlayer.prepare()
                                exoPlayer.play()
                            }
                        }
                    )

                    Button(
                        onClick = { viewModel.downloadSong(context, song) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Text(text = if (song.isDownloaded) "Downloaded" else "Download")
                    }
                }
            }

            selectedSong?.let { song ->
                Text(
                    text = "Now playing: ${song.title} by ${song.artist}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
