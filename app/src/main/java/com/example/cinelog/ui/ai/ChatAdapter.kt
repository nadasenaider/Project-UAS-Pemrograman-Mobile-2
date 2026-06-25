package com.example.cinelog.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinelog.databinding.ItemChatAiBinding
import com.example.cinelog.databinding.ItemChatUserBinding
import com.example.cinelog.data.remote.model.AiMessage
import com.example.cinelog.data.remote.model.TmdbMovieDto
import com.example.cinelog.databinding.ItemAiMovieSuggestionBinding
import com.example.cinelog.utils.Constants

class ChatAdapter(private val onMovieClick: (TmdbMovieDto) -> Unit) :
    ListAdapter<AiMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_AI = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_USER) {
            val binding = ItemChatUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            UserViewHolder(binding)
        } else {
            val binding = ItemChatAiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AiViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is UserViewHolder) {
            holder.bind(message)
        } else if (holder is AiViewHolder) {
            holder.bind(message)
        }
    }

    inner class UserViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: AiMessage) {
            binding.tvContent.text = message.content
        }
    }

    inner class AiViewHolder(private val binding: ItemChatAiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: AiMessage) {
            binding.tvContent.text = message.content
            
            if (message.suggestedMovies.isNotEmpty()) {
                binding.rvSuggestions.visibility = View.VISIBLE
                val suggestionAdapter = SuggestionAdapter(message.suggestedMovies, onMovieClick)
                binding.rvSuggestions.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = suggestionAdapter
                }
            } else {
                binding.rvSuggestions.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AiMessage>() {
        override fun areItemsTheSame(oldItem: AiMessage, newItem: AiMessage) =
            oldItem.timestamp == newItem.timestamp
        override fun areContentsTheSame(oldItem: AiMessage, newItem: AiMessage) =
            oldItem == newItem
    }

    // Inner Adapter for Movie Suggestions
    class SuggestionAdapter(
        private val movies: List<TmdbMovieDto>,
        private val onClick: (TmdbMovieDto) -> Unit
    ) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAiMovieSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(movies[position])
        }

        override fun getItemCount() = movies.size

        inner class ViewHolder(private val binding: ItemAiMovieSuggestionBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(movie: TmdbMovieDto) {
                binding.tvTitle.text = movie.title
                binding.tvRating.text = "★ ${movie.voteAverage}"
                
                Glide.with(binding.ivPoster.context)
                    .load(Constants.TMDB_IMAGE_BASE_URL + movie.posterPath)
                    .into(binding.ivPoster)
                
                binding.root.setOnClickListener { onClick(movie) }
            }
        }
    }
}
