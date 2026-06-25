package com.example.cinelog.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinelog.R
import com.example.cinelog.databinding.ItemFeedReviewBinding
import com.example.cinelog.utils.Constants
import com.example.cinelog.utils.DummyData

class FeedAdapter(
    private val reviews: List<DummyData.DummyReview>,
    private val onMovieClick: (String) -> Unit
) : RecyclerView.Adapter<FeedAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemFeedReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size

    inner class ReviewViewHolder(
        private val binding: ItemFeedReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(review: DummyData.DummyReview) {
            binding.tvUserName.text = review.userName
            binding.tvTimeAgo.text = review.timeAgo
            binding.tvReviewText.text = review.reviewText
            binding.rbReview.rating = review.rating

            // Initials avatar
            val initials = review.userName.split(" ")
                .mapNotNull { it.firstOrNull() }
                .take(2)
                .joinToString("")
                .uppercase()
            binding.tvAvatarInitials.text = initials

            // Image Poster
            val imageUrl = if (!review.moviePoster.isNullOrBlank()) {
                Constants.TMDB_IMAGE_BASE_URL + review.moviePoster
            } else {
                null
            }

            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_onboarding)
                .error(R.drawable.bg_onboarding)
                .into(binding.ivMoviePoster)

            itemView.setOnClickListener {
                onMovieClick(review.movieTitle)
            }
        }
    }
}
