package com.example.cinelog.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinelog.data.AiRepository
import com.example.cinelog.data.MovieRepository
import com.example.cinelog.data.remote.model.AiMessage
import com.example.cinelog.data.remote.model.TmdbMovieDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AiViewModel(
    private val movieRepository: MovieRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<AiMessage>>(listOf(
        AiMessage("Halo! Saya CineLog AI Assistant. Ada yang bisa saya bantu cari hari ini?", false)
    ))
    val chatMessages: StateFlow<List<AiMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = AiMessage(content = text, isUser = true)
        _chatMessages.value = _chatMessages.value + userMessage
        
        // Add a temporary loading message for AI
        val loadingMessage = AiMessage(content = "Sabar ya, lagi mikir...", isUser = false, isLoading = true)
        _chatMessages.value = _chatMessages.value + loadingMessage

        viewModelScope.launch {
            _isAiLoading.value = true
            
            // Get context from user collection
            val collection = movieRepository.getAllMovies().first().take(10)
            val context = if (collection.isNotEmpty()) {
                "Film di koleksi saya: " + collection.joinToString(", ") { it.title }
            } else {
                null
            }

            val aiResponseRaw = aiRepository.sendMessage(text, context)
            
            // Extract movies if any
            val (cleanResponse, suggestedMovies) = extractMoviesFromResponse(aiResponseRaw)
            
            // Fetch real metadata from TMDB for suggested movies
            val movieDtos = if (suggestedMovies.isNotEmpty()) {
                fetchMovieDetails(suggestedMovies)
            } else {
                emptyList()
            }

            // Remove loading message and add real AI message
            val currentMessages = _chatMessages.value.toMutableList()
            currentMessages.removeAt(currentMessages.size - 1)
            
            val aiMessage = AiMessage(
                content = cleanResponse,
                isUser = false,
                suggestedMovies = movieDtos
            )
            _chatMessages.value = currentMessages + aiMessage
            _isAiLoading.value = false
        }
    }

    private fun extractMoviesFromResponse(response: String): Pair<String, List<String>> {
        val pattern = Regex("\\[MOVIES:(.*?)\\]")
        val match = pattern.find(response)
        
        return if (match != null) {
            val moviesStr = match.groupValues[1]
            val moviesList = moviesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val cleanResponse = response.replace(match.value, "").trim()
            Pair(cleanResponse, moviesList)
        } else {
            Pair(response, emptyList())
        }
    }

    private suspend fun fetchMovieDetails(titles: List<String>): List<TmdbMovieDto> {
        val results = mutableListOf<TmdbMovieDto>()
        for (title in titles) {
            try {
                val searchResponse = movieRepository.searchMovies(title)
                searchResponse.results?.firstOrNull()?.let {
                    results.add(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return results
    }
}
