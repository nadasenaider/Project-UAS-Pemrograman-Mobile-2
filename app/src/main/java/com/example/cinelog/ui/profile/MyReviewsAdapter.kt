package com.example.cinelog.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinelog.R
import com.example.cinelog.data.local.entity.MovieEntity
import com.example.cinelog.databinding.ItemMyReviewCardBinding
import com.example.cinelog.utils.Constants

class MyReviewsAdapter(
    private val onReviewClick: (MovieEntity) -> Unit
) : ListAdapter<MovieEntity, MyReviewsAdapter.ReviewViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemMyReviewCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(
        private val binding: ItemMyReviewCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: MovieEntity) {
            binding.tvReviewMovieTitle.text = movie.title
            binding.rbMyReview.rating = movie.personalRating
            binding.tvReviewSnippet.text = movie.personalNotes ?: "No notes written."

            // Memuat poster film menggunakan Glide
            Glide.with(itemView.context)
                .load(Constants.TMDB_IMAGE_BASE_URL + movie.posterPath)
                .placeholder(R.drawable.bg_onboarding)
                .error(R.drawable.bg_onboarding)
                .centerCrop()
                .into(binding.ivReviewPoster)

            itemView.setOnClickListener { onReviewClick(movie) }
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
