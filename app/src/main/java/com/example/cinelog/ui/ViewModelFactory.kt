package com.example.cinelog.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cinelog.data.MovieRepository
import com.example.cinelog.data.local.CineLogDatabase
import com.example.cinelog.data.remote.api.RetrofitClient
import com.example.cinelog.ui.collection.CollectionViewModel
import com.example.cinelog.ui.detail.DetailViewModel
import com.example.cinelog.ui.home.HomeViewModel
import com.example.cinelog.ui.search.SearchViewModel
import com.example.cinelog.ui.ai.AiViewModel
import com.example.cinelog.data.AiRepository


class ViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(repository, AiRepository()) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CollectionViewModel::class.java) -> {
                CollectionViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AiViewModel::class.java) -> {
                AiViewModel(repository, AiRepository()) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")

        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val database = CineLogDatabase.getDatabase(context)
                val repository = MovieRepository(database.movieDao(), RetrofitClient.apiService)
                val instance = ViewModelFactory(repository)
                INSTANCE = instance
                instance
            }
        }
    }
}
