package com.example.cinelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: Int, // ID film dari TMDB
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val status: String, // WATCHLIST, WATCHING, WATCHED
    val personalRating: Float = 0f, // Rating pribadi (1-5 bintang)
    val personalNotes: String? = null, // Catatan pribadi
    val mediaType: String = "movie", // "movie" atau "tv"
    val category: String = "movie", // "movie", "series", "kdrama", "anime"
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val runtime: Int = 0, // Durasi film dalam menit
    val isFavorite: Boolean = false, // Menandai film favorit
    val addedAt: Long = System.currentTimeMillis() // Tanggal ditambahkan untuk pengurutan
)
