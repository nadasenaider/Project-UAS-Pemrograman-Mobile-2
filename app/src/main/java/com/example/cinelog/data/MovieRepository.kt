package com.example.cinelog.data

import com.example.cinelog.data.local.dao.MovieDao
import com.example.cinelog.data.local.entity.MovieEntity
import com.example.cinelog.data.remote.api.TmdbApiService
import com.example.cinelog.data.remote.model.TmdbCreditsResponse
import com.example.cinelog.data.remote.model.TmdbMovieDetailDto
import com.example.cinelog.data.remote.model.TmdbMovieResponse
import com.example.cinelog.data.remote.model.TmdbMultiSearchResponse
import com.example.cinelog.data.remote.model.TmdbTvResponse
import com.example.cinelog.data.remote.model.TmdbVideoResponse
import com.example.cinelog.data.remote.model.tvDetailToMovieDetailDto
import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val movieDao: MovieDao,
    private val apiService: TmdbApiService
) {
    // --- Local DB (Room) Operations ---
    suspend fun insertMovie(movie: MovieEntity) {
        movieDao.insertMovie(movie)
    }

    suspend fun updateMovie(movie: MovieEntity) {
        movieDao.updateMovie(movie)
    }

    suspend fun updateMovieStatus(id: Int, status: String) {
        movieDao.updateMovieStatus(id, status)
    }

    suspend fun updatePersonalReview(id: Int, rating: Float, notes: String?) {
        movieDao.updatePersonalReview(id, rating, notes)
    }

    suspend fun deleteMovieById(id: Int) {
        movieDao.deleteMovieById(id)
    }

    suspend fun getMovieById(id: Int): MovieEntity? {
        return movieDao.getMovieById(id)
    }

    fun getMovieByIdFlow(id: Int): Flow<MovieEntity?> {
        return movieDao.getMovieByIdFlow(id)
    }

    fun getMoviesByStatus(status: String): Flow<List<MovieEntity>> {
        return movieDao.getMoviesByStatus(status)
    }

    fun getAllMovies(): Flow<List<MovieEntity>> {
        return movieDao.getAllMovies()
    }

    fun getAverageRating(): Flow<Float?> {
        return movieDao.getAverageRating()
    }

    fun getMovieCountByStatus(status: String): Flow<Int> {
        return movieDao.getMovieCountByStatus(status)
    }

    suspend fun getMovieCountByStatusDirect(status: String): Int {
        return movieDao.getMovieCountByStatusDirect(status)
    }

    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {
        movieDao.updateFavoriteStatus(id, isFavorite)
    }

    fun getFavoriteMovies(): Flow<List<MovieEntity>> {
        return movieDao.getFavoriteMovies()
    }

    fun getTotalRuntime(): Flow<Int?> {
        return movieDao.getTotalRuntime()
    }

    // --- Remote API (TMDB) Operations ---
    suspend fun getTrendingMovies(): TmdbMovieResponse {
        return apiService.getTrendingMovies()
    }

    suspend fun getPopularMovies(page: Int = 1): TmdbMovieResponse {
        return apiService.getPopularMovies(page = page)
    }

    suspend fun getTopRatedMovies(page: Int = 1): TmdbMovieResponse {
        return apiService.getTopRatedMovies(page = page)
    }

    suspend fun getNowPlayingMovies(startDate: String, endDate: String, page: Int = 1): TmdbMovieResponse {
        return apiService.getNowPlayingMovies(releaseDateGte = startDate, releaseDateLte = endDate, page = page)
    }

    suspend fun searchMovies(query: String, page: Int = 1): TmdbMovieResponse {
        return apiService.searchMovies(query = query, page = page)
    }

    suspend fun globalSearch(query: String, page: Int = 1): TmdbMultiSearchResponse {
        return apiService.globalSearch(query = query, page = page)
    }



    suspend fun getMovieDetails(movieId: Int, language: String = "id-ID"): TmdbMovieDetailDto {
        return apiService.getMovieDetails(movieId, language)
    }

    suspend fun getMovieCredits(movieId: Int): TmdbCreditsResponse {
        return apiService.getMovieCredits(movieId)
    }

    suspend fun getIndonesianMovies(): TmdbMovieResponse {
        return apiService.getIndonesianMovies()
    }

    suspend fun getMoviesByGenre(genreId: Int): TmdbMovieResponse {
        return apiService.getMoviesByGenre(genreId.toString())
    }

    suspend fun getPopularTvSeries(): TmdbTvResponse {
        return apiService.getPopularTvSeries()
    }

    suspend fun getKoreanDrama(): TmdbTvResponse {
        return apiService.getKoreanDrama()
    }

    suspend fun getIndonesianSeries(): TmdbTvResponse {
        return apiService.getIndonesianSeries()
    }

    suspend fun getTvDetails(tvId: Int, language: String = "id-ID"): TmdbMovieDetailDto {
        return apiService.getTvDetails(tvId, language).tvDetailToMovieDetailDto()
    }

    suspend fun getTvCredits(tvId: Int): TmdbCreditsResponse {
        return apiService.getTvCredits(tvId)
    }

    suspend fun getAnimeSeries(): TmdbTvResponse {
        return apiService.getAnimeSeries()
    }

    suspend fun getMovieVideos(movieId: Int): TmdbVideoResponse {
        return apiService.getMovieVideos(movieId)
    }

    suspend fun getTvVideos(tvId: Int): TmdbVideoResponse {
        return apiService.getTvVideos(tvId)
    }


}
