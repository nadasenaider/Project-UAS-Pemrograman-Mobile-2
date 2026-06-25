package com.example.cinelog.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinelog.R
import com.example.cinelog.data.remote.model.TmdbMovieDto
import com.example.cinelog.databinding.ItemMovieHorizontalBinding
import com.example.cinelog.utils.Constants
import java.util.Locale

class MovieHorizontalAdapter(
    private val onMovieClick: (TmdbMovieDto) -> Unit
) : ListAdapter<TmdbMovieDto, MovieHorizontalAdapter.MovieViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieHorizontalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: TmdbMovieDto) {
            binding.tvTitle.text = movie.displayTitle ?: "Unknown"
            binding.tvRating.text = String.format(Locale.US, "%.1f", movie.voteAverage ?: 0.0)
            
            // Format Release Year
            val year = if (!movie.releaseDate.isNullOrBlank() && movie.releaseDate.length >= 4) {
                movie.releaseDate.substring(0, 4)
            } else {
                "-"
            }
            binding.tvYear.text = year

            // Glide Poster Loader
            val imageUrl = if (!movie.posterPath.isNullOrBlank()) {
                Constants.TMDB_IMAGE_BASE_URL + movie.posterPath
            } else {
                null
            }

            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_onboarding) // default placeholder
                .error(R.drawable.bg_onboarding)
                .into(binding.ivPoster)

            // Show TV Badge if series
            binding.tvTvBadge.visibility = if (movie.mediaType == "tv") android.view.View.VISIBLE else android.view.View.GONE

            itemView.setOnClickListener { onMovieClick(movie) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TmdbMovieDto>() {
            override fun areItemsTheSame(oldItem: TmdbMovieDto, newItem: TmdbMovieDto): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TmdbMovieDto, newItem: TmdbMovieDto): Boolean {
                return oldItem == newItem
            }
        }
    }
}
