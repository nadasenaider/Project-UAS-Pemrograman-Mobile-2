package com.example.cinelog.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.data.MovieRepository
import com.example.cinelog.data.local.entity.MovieEntity
import com.example.cinelog.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(private val repository: MovieRepository) : ViewModel() {

    // Mengamati seluruh daftar film di database lokal secara reaktif menggunakan StateFlow
    val allMovies: StateFlow<List<MovieEntity>> = repository.getAllMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlistMovies: StateFlow<List<MovieEntity>> = repository.getMoviesByStatus(Constants.STATUS_WATCHLIST)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchingMovies: StateFlow<List<MovieEntity>> = repository.getMoviesByStatus(Constants.STATUS_WATCHING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchedMovies: StateFlow<List<MovieEntity>> = repository.getMoviesByStatus(Constants.STATUS_WATCHED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val averageRating: StateFlow<Float?> = repository.getAverageRating()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val watchlistCount: StateFlow<Int> = repository.getMovieCountByStatus(Constants.STATUS_WATCHLIST)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val watchingCount: StateFlow<Int> = repository.getMovieCountByStatus(Constants.STATUS_WATCHING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val watchedCount: StateFlow<Int> = repository.getMovieCountByStatus(Constants.STATUS_WATCHED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val favoriteMovies: StateFlow<List<MovieEntity>> = repository.getFavoriteMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRuntime: StateFlow<Int?> = repository.getTotalRuntime()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
