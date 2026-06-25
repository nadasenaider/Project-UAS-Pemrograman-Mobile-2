package com.example.cinelog.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.data.MovieRepository
import com.example.cinelog.data.remote.model.TmdbMovieDto
import com.example.cinelog.data.remote.model.fixOverview
import com.example.cinelog.data.remote.model.tvToMovieDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _trendingMovies = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val trendingMovies: StateFlow<List<TmdbMovieDto>> = _trendingMovies.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val popularMovies: StateFlow<List<TmdbMovieDto>> = _popularMovies.asStateFlow()

    private val _indonesianMovies = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val indonesianMovies: StateFlow<List<TmdbMovieDto>> = _indonesianMovies.asStateFlow()

    private val _westSeries = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val westSeries: StateFlow<List<TmdbMovieDto>> = _westSeries.asStateFlow()

    private val _kDrama = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val kDrama: StateFlow<List<TmdbMovieDto>> = _kDrama.asStateFlow()

    private val _indoSeries = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val indoSeries: StateFlow<List<TmdbMovieDto>> = _indoSeries.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val topRatedMovies: StateFlow<List<TmdbMovieDto>> = _topRatedMovies.asStateFlow()

    private val _nowPlayingMovies = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val nowPlayingMovies: StateFlow<List<TmdbMovieDto>> = _nowPlayingMovies.asStateFlow()

    private val _animeMovies = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val animeMovies: StateFlow<List<TmdbMovieDto>> = _animeMovies.asStateFlow()

    private var basePopularList: List<TmdbMovieDto> = emptyList()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                kotlinx.coroutines.coroutineScope {
                    launch { _trendingMovies.value = repository.getTrendingMovies().results?.map { it.fixOverview() } ?: emptyList() }
                    launch { 
                        val popular = repository.getPopularMovies().results?.map { it.fixOverview() } ?: emptyList()
                        basePopularList = popular
                        _popularMovies.value = popular 
                    }
                    launch { _indonesianMovies.value = repository.getIndonesianMovies().results?.map { it.fixOverview() } ?: emptyList() }
                    launch { _westSeries.value = repository.getPopularTvSeries().results?.map { it.tvToMovieDto().fixOverview() } ?: emptyList() }
                    launch { _kDrama.value = repository.getKoreanDrama().results?.map { it.tvToMovieDto().fixOverview() } ?: emptyList() }
                    launch { _indoSeries.value = repository.getIndonesianSeries().results?.map { it.tvToMovieDto().fixOverview() } ?: emptyList() }
                    launch { _topRatedMovies.value = repository.getTopRatedMovies().results?.map { it.fixOverview() } ?: emptyList() }
                    launch { 
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val calendar = Calendar.getInstance()
                        val endDate = sdf.format(calendar.time) // Hari ini
                        calendar.add(Calendar.DAY_OF_YEAR, -30) // 1 bulan yang lalu
                        val startDate = sdf.format(calendar.time)
                        
                        _nowPlayingMovies.value = repository.getNowPlayingMovies(startDate, endDate).results?.map { it.fixOverview() } ?: emptyList() 
                    }
                    launch { _animeMovies.value = repository.getAnimeSeries().results?.map { it.tvToMovieDto().fixOverview() } ?: emptyList() }
                    Unit
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Gagal memuat data. Periksa koneksi internet Anda."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByGenre(genreId: Int?) {
        if (genreId == null) {
            _popularMovies.value = basePopularList
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = repository.getMoviesByGenre(genreId).results?.map { it.fixOverview() } ?: emptyList()
                _popularMovies.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memfilter genre."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
