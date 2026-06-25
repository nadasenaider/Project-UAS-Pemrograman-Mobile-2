package com.example.cinelog.data.remote.model

data class AiMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedMovies: List<TmdbMovieDto> = emptyList(),
    val isLoading: Boolean = false
)
