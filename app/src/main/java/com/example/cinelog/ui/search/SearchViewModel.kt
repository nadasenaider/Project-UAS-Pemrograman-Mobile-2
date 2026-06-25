package com.example.cinelog.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.data.MovieRepository
import com.example.cinelog.data.remote.model.TmdbMovieDto
import com.example.cinelog.data.remote.model.multiToMovieDto
import com.example.cinelog.utils.DummyData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<TmdbMovieDto>>(emptyList())
    val searchResults: StateFlow<List<TmdbMovieDto>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun searchMovies(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.globalSearch(query)
                // Filter out results that are people, only keep movie and tv
                val filteredResults = (response.results ?: emptyList())
                    .filter { it.mediaType == "movie" || it.mediaType == "tv" }
                    .map { it.multiToMovieDto() }
                _searchResults.value = filteredResults
            } catch (e: Exception) {
                // Filter dummy movies by title if offline/error
                val filteredDummy = DummyData.dummyMovies.filter {
                    it.title?.contains(query, ignoreCase = true) == true
                }
                _searchResults.value = filteredDummy
                // Berikan pesan error yang lebih spesifik
                val isNetworkError = e is java.io.IOException
                if (isNetworkError) {
                    _errorMessage.value = "OFFLINE_ERROR"
                } else {
                    _errorMessage.value = "SERVER_ERROR"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
