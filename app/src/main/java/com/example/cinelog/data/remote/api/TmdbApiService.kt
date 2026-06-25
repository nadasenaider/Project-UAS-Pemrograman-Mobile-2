package com.example.cinelog.data.remote.api

import com.example.cinelog.data.remote.model.TmdbCreditsResponse
import com.example.cinelog.data.remote.model.TmdbMovieDetailDto
import com.example.cinelog.data.remote.model.TmdbMovieResponse
import com.example.cinelog.data.remote.model.TmdbMultiSearchResponse
import com.example.cinelog.data.remote.model.TmdbTvDetailDto
import com.example.cinelog.data.remote.model.TmdbTvDto
import com.example.cinelog.data.remote.model.TmdbTvResponse
import com.example.cinelog.data.remote.model.TmdbVideoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {
    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("language") language: String = "id-ID"
    ): TmdbMovieResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("language") language: String = "id-ID",
        @Query("page") page: Int = 1
    ): TmdbMovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("language") language: String = "id-ID",
        @Query("page") page: Int = 1
    ): TmdbMovieResponse

    @GET("discover/movie")
    suspend fun getNowPlayingMovies(
        @Query("language") language: String = "id-ID",
        @Query("region") region: String = "ID",
        @Query("with_release_type") releaseType: String = "3|2", // Bioskop (Theatrical & Limited) di Indonesia
        @Query("release_date.gte") releaseDateGte: String, // Jadwal tayang mulai di Indo
        @Query("release_date.lte") releaseDateLte: String, // Jadwal tayang akhir (hari ini) di Indo
        @Query("with_original_language") languages: String = "id|en", // Hanya Indonesia dan Hollywood
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("page") page: Int = 1
    ): TmdbMovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("language") language: String = "id-ID",
        @Query("page") page: Int = 1
    ): TmdbMovieResponse

    @GET("search/multi")
    suspend fun globalSearch(
        @Query("query") query: String,
        @Query("language") language: String = "id-ID",
        @Query("page") page: Int = 1
    ): TmdbMultiSearchResponse

    @GET("discover/movie")
    suspend fun getIndonesianMovies(
        @Query("language") language: String = "id-ID",
        @Query("with_original_language") originalLanguage: String = "id",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbMovieResponse

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("with_genres") genreId: String,
        @Query("language") language: String = "id-ID",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_count.gte") minVoteCount: Int = 500 // Memastikan film yang muncul adalah film populer berating valid
    ): TmdbMovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "id-ID"
    ): TmdbMovieDetailDto

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int
    ): TmdbCreditsResponse

    // --- TV SERIES ---

    @GET("discover/tv")
    suspend fun getPopularTvSeries(
        @Query("language") language: String = "id-ID",
        @Query("with_original_language") originLanguage: String = "en",
        @Query("sort_by") sortBy: String = "vote_average.desc",
        @Query("vote_count.gte") minVoteCount: Int = 1000,
        @Query("page") page: Int = 1
    ): TmdbTvResponse

    @GET("discover/tv")
    suspend fun getKoreanDrama(
        @Query("language") language: String = "en-US", // Pakai judul Bahasa Inggris
        @Query("with_original_language") originLanguage: String = "ko",
        @Query("sort_by") sortBy: String = "vote_average.desc", // Rating tertinggi
        @Query("vote_count.gte") minVoteCount: Int = 200 // Minimal di-review 200 orang agar valid
    ): TmdbTvResponse

    @GET("discover/tv")
    suspend fun getIndonesianSeries(
        @Query("language") language: String = "id-ID",
        @Query("with_original_language") originLanguage: String = "id",
        @Query("sort_by") sortBy: String = "vote_average.desc",
        @Query("vote_count.gte") minVoteCount: Int = 10     // Threshold lebih rendah karena data lokal di TMDB lebih sedikit
    ): TmdbTvResponse

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("language") language: String = "id-ID"
    ): TmdbTvDetailDto

    @GET("tv/{tv_id}/credits")
    suspend fun getTvCredits(
        @Path("tv_id") tvId: Int
    ): TmdbCreditsResponse

    @GET("discover/tv")
    suspend fun getAnimeSeries(
        @Query("language") language: String = "id-ID",
        @Query("with_genres") genres: String = "16",
        @Query("with_original_language") originLanguage: String = "ja",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("sort_by") sortBy: String = "vote_count.desc", // Mengutamakan anime "besar" yang banyak audiensnya
        @Query("vote_count.gte") minVoteCount: Int = 100     // Memastikan hanya anime populer yang muncul
    ): TmdbTvResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int
    ): TmdbVideoResponse

    @GET("tv/{tv_id}/videos")
    suspend fun getTvVideos(
        @Path("tv_id") tvId: Int
    ): TmdbVideoResponse

}
