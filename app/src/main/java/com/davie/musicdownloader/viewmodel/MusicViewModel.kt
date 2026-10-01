package com.davie.musicdownloader.viewmodel

import android.content.Context
import android.os.Environment
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davie.musicdownloader.model.Song
import com.davie.musicdownloader.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class MusicUiState(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val downloadedSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MusicViewModel : ViewModel() {
    private val repository = MusicRepository()

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun searchSongs() {
        val currentQuery = _uiState.value.query.trim()
        if (currentQuery.isEmpty()) {
            _uiState.update { it.copy(songs = emptyList(), errorMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val songs = repository.searchSongs(currentQuery)
                _uiState.update { it.copy(songs = songs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Something went wrong while searching."
                    )
                }
            }
        }
    }

    fun downloadSong(context: Context, song: Song) {
        viewModelScope.launch {
            val musicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            val targetFile = File(musicDir, "${song.id}_${song.title.replace("\\s+".toRegex(), "_")}.m4a")

            try {
                val response = java.net.URL(song.previewUrl).openStream()
                response.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val downloadedSong = song.copy(
                    localFilePath = targetFile.absolutePath,
                    isDownloaded = true
                )

                val updatedDownloaded = _uiState.value.downloadedSongs.filterNot { it.id == song.id } + downloadedSong
                val updatedSongs = _uiState.value.songs.map {
                    if (it.id == song.id) downloadedSong else it
                }

                _uiState.update {
                    it.copy(
                        songs = updatedSongs,
                        downloadedSongs = updatedDownloaded,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Download failed.") }
            }
        }
    }
}
