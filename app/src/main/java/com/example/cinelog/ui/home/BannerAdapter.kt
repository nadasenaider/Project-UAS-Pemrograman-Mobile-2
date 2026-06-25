package com.example.cinelog.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinelog.R
import com.example.cinelog.data.remote.model.TmdbMovieDto
import com.example.cinelog.databinding.ItemBannerBinding
import com.example.cinelog.utils.Constants

class BannerAdapter(private val onClick: (TmdbMovieDto) -> Unit) :
    ListAdapter<TmdbMovieDto, BannerAdapter.BannerViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BannerViewHolder(private val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: TmdbMovieDto) {
            binding.tvBannerTitle.text = movie.displayTitle
            binding.tvBannerDesc.text = movie.overview

            val backdropUrl = if (!movie.backdropPath.isNullOrBlank()) {
                Constants.TMDB_IMAGE_BASE_URL + movie.backdropPath
            } else {
                Constants.TMDB_IMAGE_BASE_URL + movie.posterPath
            }

            Glide.with(binding.root.context)
                .load(backdropUrl)
                .placeholder(R.drawable.bg_onboarding)
                .error(R.drawable.bg_onboarding)
                .into(binding.ivBannerBackdrop)

            binding.btnBannerDetail.setOnClickListener { onClick(movie) }
            binding.root.setOnClickListener { onClick(movie) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TmdbMovieDto>() {
        override fun areItemsTheSame(oldItem: TmdbMovieDto, newItem: TmdbMovieDto): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TmdbMovieDto, newItem: TmdbMovieDto): Boolean =
            oldItem == newItem
    }
}
