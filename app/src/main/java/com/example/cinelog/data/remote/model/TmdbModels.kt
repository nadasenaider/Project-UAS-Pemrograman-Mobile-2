package com.example.cinelog.data.remote.model

import com.google.gson.annotations.SerializedName

// 1. Respon List Film (Trending, Popular, Search)
data class TmdbMovieResponse(
    @SerializedName("results")
    val results: List<TmdbMovieDto>
)

data class TmdbMovieDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("genre_ids")
    val genreIds: List<Int>? = emptyList(),
    val mediaType: String? = "movie"
) {
    val displayTitle: String
        get() = title ?: "Untitled"
}


// 2. Respon Detail Film
data class TmdbMovieDetailDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("runtime")
    val runtime: Int? = 0,
    @SerializedName("genres")
    val genres: List<TmdbGenreDto>? = emptyList(),
    @SerializedName("budget")
    val budget: Long? = 0,
    @SerializedName("production_companies")
    val productionCompanies: List<TmdbCompanyDto>? = emptyList(),
    @SerializedName("original_language")
    val originalLanguage: String?,
    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int? = 0,
    @SerializedName("number_of_episodes")
    val numberOfEpisodes: Int? = 0,
    val mediaType: String? = "movie"
)


data class TmdbGenreDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?
)

data class TmdbCompanyDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?
)

// 3. Respon Pemeran / Cast
data class TmdbCreditsResponse(
    @SerializedName("cast")
    val cast: List<TmdbCastDto>?
)

data class TmdbCastDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("character")
    val character: String?,
    @SerializedName("profile_path")
    val profilePath: String?
)

// 4. Respon TV Series
data class TmdbTvResponse(
    @SerializedName("results")
    val results: List<TmdbTvDto>?
)

data class TmdbTvDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("origin_country")
    val originCountry: List<String>?
)

// Respon Detail TV
data class TmdbTvDetailDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,
    @SerializedName("overview")
    val overview: String?,
    @SerializedName("genres")
    val genres: List<TmdbGenreDto>?,
    @SerializedName("episode_run_time")
    val episodeRunTime: List<Int>?,
    @SerializedName("original_language")
    val originalLanguage: String?,
    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int?,
    @SerializedName("number_of_episodes")
    val numberOfEpisodes: Int?
)


// 5. Multi Search (Movies + TV)
data class TmdbMultiSearchResponse(
    @SerializedName("results")
    val results: List<TmdbMultiSearchDto>?
)

data class TmdbMultiSearchDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("media_type")
    val mediaType: String?,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("poster_path")
    val posterPath: String? = null,
    @SerializedName("overview")
    val overview: String? = null,
    @SerializedName("vote_average")
    val voteAverage: Double? = 0.0,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    @SerializedName("first_air_date")
    val firstAirDate: String? = null
)

// 6. Respon Video (Trailer)
data class TmdbVideoResponse(
    @SerializedName("results")
    val results: List<TmdbVideoDto>?
)

data class TmdbVideoDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("key")
    val videoKey: String,         // YouTube Video ID
    @SerializedName("name")
    val name: String,
    @SerializedName("site")
    val site: String,         // "YouTube"
    @SerializedName("type")
    val type: String,         // "Trailer", "Teaser", etc.
    @SerializedName("official")
    val official: Boolean
)

// --- Extension Functions for Conversion ---

fun TmdbMultiSearchDto.multiToMovieDto(): TmdbMovieDto = TmdbMovieDto(
    id = this.id,
    title = this.title ?: this.name ?: "Unknown Title",
    posterPath = this.posterPath,
    backdropPath = null,
    overview = this.overview,
    voteAverage = this.voteAverage ?: 0.0,
    releaseDate = this.releaseDate ?: this.firstAirDate,
    mediaType = this.mediaType ?: "movie"
)

fun TmdbTvDto.tvToMovieDto(): TmdbMovieDto = TmdbMovieDto(
    id = this.id,
    title = this.name,
    posterPath = this.posterPath,
    backdropPath = this.backdropPath,
    releaseDate = this.firstAirDate,
    voteAverage = this.voteAverage ?: 0.0,
    overview = this.overview,
    mediaType = "tv"
)

fun TmdbTvDetailDto.tvDetailToMovieDetailDto(): TmdbMovieDetailDto = TmdbMovieDetailDto(
    id = this.id,
    title = this.name,
    posterPath = this.posterPath,
    backdropPath = this.backdropPath,
    releaseDate = this.firstAirDate,
    voteAverage = this.voteAverage ?: 0.0,
    overview = this.overview,
    runtime = this.episodeRunTime?.firstOrNull() ?: 30,
    genres = this.genres ?: emptyList(),
    budget = 0L,
    productionCompanies = emptyList(),
    originalLanguage = this.originalLanguage,
    numberOfSeasons = this.numberOfSeasons,
    numberOfEpisodes = this.numberOfEpisodes,
    mediaType = "tv"
)


fun TmdbMovieDetailDto.determineCategory(): String {
    val type = this.mediaType ?: "movie"
    if (type == "movie") return "movie"
    
    val isKDrama = (this.originalLanguage == "ko")
    if (isKDrama) return "kdrama"
    
    return "series"
}

// Memperbaiki overview jika kosong atau null
fun TmdbMovieDetailDto.fixOverview(): TmdbMovieDetailDto = this.copy(
    overview = if (this.overview.isNullOrBlank()) "Sinopsis tidak tersedia untuk judul ini dalam bahasa Indonesia." else this.overview
)

fun TmdbMovieDto.fixOverview(): TmdbMovieDto = this.copy(
    overview = if (this.overview.isNullOrBlank()) "Sinopsis tidak tersedia untuk judul ini dalam bahasa Indonesia." else this.overview
)
