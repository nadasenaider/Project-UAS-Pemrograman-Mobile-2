package com.example.cinelog.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinelog.R
import com.example.cinelog.data.remote.model.TmdbCastDto
import com.example.cinelog.databinding.ItemCastBinding
import com.example.cinelog.utils.Constants

class CastAdapter : ListAdapter<TmdbCastDto, CastAdapter.CastViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ItemCastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CastViewHolder(
        private val binding: ItemCastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cast: TmdbCastDto) {
            binding.tvCastName.text = cast.name
            binding.tvCastCharacter.text = cast.character

            val imageUrl = if (!cast.profilePath.isNullOrBlank()) {
                Constants.TMDB_IMAGE_BASE_URL + cast.profilePath
            } else {
                null
            }

            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_onboarding)
                .error(R.drawable.bg_onboarding)
                .into(binding.ivCastProfile)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TmdbCastDto>() {
            override fun areItemsTheSame(oldItem: TmdbCastDto, newItem: TmdbCastDto): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TmdbCastDto, newItem: TmdbCastDto): Boolean {
                return oldItem == newItem
            }
        }
    }
}
