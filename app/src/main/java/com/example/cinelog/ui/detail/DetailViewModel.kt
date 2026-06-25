package com.example.cinelog.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.data.MovieRepository
import com.example.cinelog.data.local.entity.MovieEntity
import com.example.cinelog.data.remote.model.TmdbCastDto
import com.example.cinelog.data.remote.model.TmdbMovieDetailDto
import com.example.cinelog.data.remote.model.TmdbVideoDto
import com.example.cinelog.data.remote.model.TmdbVideoResponse
import com.example.cinelog.data.remote.model.determineCategory
import com.example.cinelog.data.remote.model.fixOverview
import com.example.cinelog.utils.DummyData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: MovieRepository,
    private val aiRepository: com.example.cinelog.data.AiRepository
) : ViewModel() {

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _beautifiedReview = MutableStateFlow<String?>(null)
    val beautifiedReview: StateFlow<String?> = _beautifiedReview.asStateFlow()

    fun beautifyReview(rawText: String, movieTitle: String) {
        if (rawText.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            val prompt = """
                Tugas: Rapikan dan perbagus ulasan film berikut menjadi sebuah review yang profesional, menarik, dan informatif dalam Bahasa Indonesia.
                Film: $movieTitle
                Ulasan Kasar: $rawText
                
                Aturan:
                1. Pertahankan inti perasaan atau pendapat dari ulasan kasar tersebut.
                2. Gunakan tata bahasa yang baik.
                3. Jangan terlalu panjang, cukup 2-3 kalimat yang berbobot.
                4. Hasil akhir HANYA teks review saja, jangan ada kalimat pengantar dari AI.
            """.trimIndent()

            val result = aiRepository.sendMessage(prompt)
            _beautifiedReview.value = result
            _isAiLoading.value = false
        }
    }

    fun resetBeautifiedReview() {
        _beautifiedReview.value = null
    }

    private val _movieDetail = MutableStateFlow<TmdbMovieDetailDto?>(null)
    val movieDetail: StateFlow<TmdbMovieDetailDto?> = _movieDetail.asStateFlow()

    private val _movieCast = MutableStateFlow<List<TmdbCastDto>>(emptyList())
    val movieCast: StateFlow<List<TmdbCastDto>> = _movieCast.asStateFlow()

    private val _localMovieState = MutableStateFlow<MovieEntity?>(null)
    val localMovieState: StateFlow<MovieEntity?> = _localMovieState.asStateFlow()

    private val _trailerVideoId = MutableStateFlow<String?>(null)
    val trailerVideoId: StateFlow<String?> = _trailerVideoId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadMovieDetails(id: Int, mediaType: String? = "movie") {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _localMovieState.value = repository.getMovieById(id)

            try {
                if (mediaType == "tv") {
                    var tvDetail = repository.getTvDetails(id, "id-ID")
                    if (tvDetail.overview.isNullOrBlank()) {
                        val enTvDetail = repository.getTvDetails(id, "en-US")
                        if (!enTvDetail.overview.isNullOrBlank()) {
                            tvDetail = tvDetail.copy(overview = enTvDetail.overview)
                        }
                    }
                    _movieDetail.value = tvDetail.fixOverview()
                    _movieCast.value = repository.getTvCredits(id).cast ?: emptyList()
                    
                    // Fetch TV Trailers
                    val videoList = repository.getTvVideos(id).results ?: emptyList()
                    var foundKey: String? = null
                    for (v in videoList) {
                        if (v.site == "YouTube" && v.type == "Trailer") {
                            foundKey = v.videoKey
                            break
                        }
                    }
                    if (foundKey == null) {
                        for (v in videoList) {
                            if (v.site == "YouTube") {
                                foundKey = v.videoKey
                                break
                            }
                        }
                    }
                    _trailerVideoId.value = foundKey
                } else {
                    var movieDetail = repository.getMovieDetails(id, "id-ID")
                    if (movieDetail.overview.isNullOrBlank()) {
                        val enMovieDetail = repository.getMovieDetails(id, "en-US")
                        if (!enMovieDetail.overview.isNullOrBlank()) {
                            movieDetail = movieDetail.copy(overview = enMovieDetail.overview)
                        }
                    }
                    _movieDetail.value = movieDetail.fixOverview()
                    _movieCast.value = repository.getMovieCredits(id).cast ?: emptyList()

                    // Fetch Movie Trailers
                    val videoList = repository.getMovieVideos(id).results ?: emptyList()
                    var foundKey: String? = null
                    for (v in videoList) {
                        if (v.site == "YouTube" && v.type == "Trailer") {
                            foundKey = v.videoKey
                            break
                        }
                    }
                    if (foundKey == null) {
                        for (v in videoList) {
                            if (v.site == "YouTube") {
                                foundKey = v.videoKey
                                break
                            }
                        }
                    }
                    _trailerVideoId.value = foundKey
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat detail."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Tambah film ke Room (Watchlist / Watching / Watched)
    fun addMovieToCollection(movieDetail: TmdbMovieDetailDto, status: String, rating: Float = 0f, notes: String? = null) {
        viewModelScope.launch {
            val entity = MovieEntity(
                id = movieDetail.id,
                title = movieDetail.title ?: "Unknown Title",
                posterPath = movieDetail.posterPath,
                backdropPath = movieDetail.backdropPath,
                releaseDate = movieDetail.releaseDate,
                voteAverage = movieDetail.voteAverage ?: 0.0,
                status = status,
                personalRating = rating,
                personalNotes = notes,
                mediaType = movieDetail.mediaType ?: "movie",
                category = movieDetail.determineCategory(),
                numberOfSeasons = movieDetail.numberOfSeasons ?: 0,
                numberOfEpisodes = movieDetail.numberOfEpisodes ?: 0,
                runtime = movieDetail.runtime ?: 0
            )
            repository.insertMovie(entity)
            _localMovieState.value = entity
        }
    }

    fun toggleFavorite(movieId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(movieId, !currentStatus)
        }
    }

    // Update ulasan (Rating & Notes)
    fun updateReview(movieId: Int, rating: Float, notes: String?) {
        viewModelScope.launch {
            repository.updatePersonalReview(movieId, rating, notes)
            // Refresh data lokal
            val current = _localMovieState.value
            if (current != null) {
                _localMovieState.value = current.copy(personalRating = rating, personalNotes = notes)
            }
        }
    }

    // Update Status Film (Watchlist, Watching, Watched)
    fun updateMovieStatus(movieId: Int, status: String) {
        viewModelScope.launch {
            repository.updateMovieStatus(movieId, status)
            val current = _localMovieState.value
            if (current != null) {
                _localMovieState.value = current.copy(status = status)
            }
        }
    }

    // Hapus film dari database
    fun removeMovieFromCollection(movieId: Int) {
        viewModelScope.launch {
            repository.deleteMovieById(movieId)
            _localMovieState.value = null
        }
    }
}
