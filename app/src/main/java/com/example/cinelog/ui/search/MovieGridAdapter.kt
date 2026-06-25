package com.example.cinelog.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinelog.R
import com.example.cinelog.data.remote.model.TmdbMovieDto
import com.example.cinelog.databinding.ItemMovieGridBinding
import com.example.cinelog.utils.Constants
import java.util.Locale

class MovieGridAdapter(
    private val onMovieClick: (TmdbMovieDto) -> Unit
) : ListAdapter<TmdbMovieDto, MovieGridAdapter.MovieViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieGridBinding.inflate(
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
        private val binding: ItemMovieGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: TmdbMovieDto) {
            binding.tvGridTitle.text = movie.displayTitle ?: "Unknown"
            binding.tvGridRating.text = String.format(Locale.US, "%.1f", movie.voteAverage ?: 0.0)

            val imageUrl = if (!movie.posterPath.isNullOrBlank()) {
                Constants.TMDB_IMAGE_BASE_URL + movie.posterPath
            } else {
                null
            }

            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_onboarding)
                .error(R.drawable.bg_onboarding)
                .into(binding.ivGridPoster)

            // Show "TV" badge if it's a series
            if (movie.mediaType == "tv") {
                binding.tvGridSeasonEntry.visibility = android.view.View.VISIBLE
                binding.tvGridSeasonEntry.text = "TV SHOW"
                binding.tvGridSeasonEntry.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xCCFFD700.toInt()))
            } else {
                binding.tvGridSeasonEntry.visibility = android.view.View.GONE
            }

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
