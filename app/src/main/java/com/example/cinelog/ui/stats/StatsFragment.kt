package com.example.cinelog.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cinelog.R
import com.example.cinelog.databinding.FragmentStatsBinding
import com.example.cinelog.ui.ViewModelFactory
import com.example.cinelog.ui.collection.CollectionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CollectionViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.cvRing.setOnClickListener {
            navigateToCollection("ALL")
        }
        binding.cvWatchlist.setOnClickListener {
            navigateToCollection("WATCHLIST")
        }
        binding.cvWatching.setOnClickListener {
            navigateToCollection("WATCHING")
        }
        binding.cvWatched.setOnClickListener {
            navigateToCollection("WATCHED")
        }
    }

    private fun navigateToCollection(filterType: String) {
        val bundle = Bundle().apply {
            putString("filterType", filterType)
        }
        findNavController().navigate(R.id.action_statsFragment_to_collectionFragment, bundle)
    }

    private fun observeViewModel() {
        // Watched Count
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.watchedCount.collectLatest { count ->
                binding.tvWatchedCount.text = count.toString()
                updateRingProgress()
            }
        }

        // Watching Count
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.watchingCount.collectLatest { count ->
                binding.tvWatchingCount.text = count.toString()
                updateRingProgress()
            }
        }

        // Watchlist Count
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.watchlistCount.collectLatest { count ->
                binding.tvWatchlistCount.text = count.toString()
                updateRingProgress()
            }
        }

        // Average Rating
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.averageRating.collectLatest { rating ->
                if (rating != null && rating > 0f) {
                    binding.tvAvgRating.text = String.format(Locale.US, "%.1f ★", rating)
                } else {
                    binding.tvAvgRating.text = "- ★"
                }
            }
        }
    }

    private fun updateRingProgress() {
        val watchlist = viewModel.watchlistCount.value
        val watching = viewModel.watchingCount.value
        val watched = viewModel.watchedCount.value
        
        val total = watchlist + watching + watched
        binding.tvTotalWatchedCount.text = total.toString()

        if (total > 0) {
            // Persentase film yang sudah selesai ditonton dari total jurnal
            val percentage = (watched.toFloat() / total.toFloat() * 100).toInt()
            binding.pbStatsRing.progress = percentage
        } else {
            binding.pbStatsRing.progress = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
