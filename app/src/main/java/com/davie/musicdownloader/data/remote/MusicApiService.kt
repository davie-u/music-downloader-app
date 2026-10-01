package com.davie.musicdownloader.data.remote

import com.davie.musicdownloader.model.Song
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 10
    ): SearchSongsResponse
}

data class SearchSongsResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<ItunesSong>
)

data class ItunesSong(
    @SerializedName("trackId") val trackId: Long,
    @SerializedName("trackName") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("artworkUrl100") val artworkUrl: String,
    @SerializedName("previewUrl") val previewUrl: String
) {
    fun toSong(): Song = Song(
        id = trackId,
        title = trackName,
        artist = artistName,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl
    )
}
