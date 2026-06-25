package com.example.cinelog.ui.collection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinelog.R
import com.example.cinelog.data.local.entity.MovieEntity
import com.example.cinelog.databinding.ItemMovieGridBinding
import com.example.cinelog.utils.Constants
import java.util.Locale

class CollectionAdapter(
    private val onMovieClick: (MovieEntity) -> Unit
) : ListAdapter<MovieEntity, CollectionAdapter.MovieViewHolder>(DIFF_CALLBACK) {

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

        fun bind(movie: MovieEntity) {
            binding.tvGridTitle.text = movie.title
            
            // Tampilkan rating personal jika statusnya WATCHED, jika tidak tampilkan rating umum TMDB
            val ratingToShow = if (movie.status == Constants.STATUS_WATCHED && movie.personalRating > 0f) {
                movie.personalRating * 2.0 // Skala 10
            } else {
                movie.voteAverage ?: 0.0
            }
            binding.tvGridRating.text = String.format(Locale.US, "%.1f", ratingToShow)

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

            // Season & Episode Badge for Series/Anime/KDrama
            if (movie.mediaType == "tv" || movie.numberOfSeasons > 0) {
                binding.tvGridSeasonEntry.visibility = android.view.View.VISIBLE
                binding.tvGridSeasonEntry.text = "S${movie.numberOfSeasons} • E${movie.numberOfEpisodes}"
            } else {
                binding.tvGridSeasonEntry.visibility = android.view.View.GONE
            }

            itemView.setOnClickListener { onMovieClick(movie) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MovieEntity>() {
            override fun areItemsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MovieEntity, newItem: MovieEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
