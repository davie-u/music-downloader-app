package com.davie.musicdownloader.repository

import com.davie.musicdownloader.data.remote.MusicApiService
import com.davie.musicdownloader.model.Song
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MusicRepository {
    private val api: MusicApiService = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    }
                )
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MusicApiService::class.java)

    suspend fun searchSongs(query: String): List<Song> {
        if (query.isBlank()) return emptyList()
        return api.searchSongs(query).results.map { it.toSong() }
    }
}
