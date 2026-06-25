package com.example.cinelog.ui.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cinelog.R
import com.example.cinelog.databinding.FragmentCollectionBinding
import com.example.cinelog.ui.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CollectionFragment : Fragment() {

    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CollectionViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var collectionAdapter: CollectionAdapter
    private var activeFilter = FILTER_ALL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        handleArguments()
        observeViewModel()
    }

    private fun handleArguments() {
        val filter = arguments?.getString("filterType") ?: "ALL"
        when (filter) {
            "ALL" -> binding.chipAll.isChecked = true
            "WATCHLIST" -> binding.chipWatchlist.isChecked = true
            "WATCHING" -> binding.chipWatching.isChecked = true
            "WATCHED" -> binding.chipWatched.isChecked = true
        }
    }

    private fun setupRecyclerView() {
        collectionAdapter = CollectionAdapter { movie ->
            val bundle = Bundle().apply {
                putInt("movieId", movie.id)
                putString("mediaType", movie.mediaType)
            }
            findNavController().navigate(R.id.action_collectionFragment_to_detailFragment, bundle)
        }
        binding.rvCollection.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = collectionAdapter
        }
    }

    private fun setupListeners() {
        binding.cgCollection.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipAll
            activeFilter = when (checkedId) {
                R.id.chipAll -> FILTER_ALL
                R.id.chipWatchlist -> FILTER_WATCHLIST
                R.id.chipWatching -> FILTER_WATCHING
                R.id.chipWatched -> FILTER_WATCHED
                else -> FILTER_ALL
            }
            triggerFilterUpdate()
        }
    }

    private fun observeViewModel() {
        // Kita trigger filter update pertama kali
        triggerFilterUpdate()
    }

    private fun triggerFilterUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (activeFilter) {
                FILTER_ALL -> {
                    viewModel.allMovies.collectLatest { list ->
                        submitList(list)
                    }
                }
                FILTER_WATCHLIST -> {
                    viewModel.watchlistMovies.collectLatest { list ->
                        submitList(list)
                    }
                }
                FILTER_WATCHING -> {
                    viewModel.watchingMovies.collectLatest { list ->
                        submitList(list)
                    }
                }
                FILTER_WATCHED -> {
                    viewModel.watchedMovies.collectLatest { list ->
                        submitList(list)
                    }
                }
            }
        }
    }

    private fun submitList(list: List<com.example.cinelog.data.local.entity.MovieEntity>) {
        collectionAdapter.submitList(list)
        if (list.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.rvCollection.visibility = View.GONE
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.rvCollection.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val FILTER_ALL = 0
        private const val FILTER_WATCHLIST = 1
        private const val FILTER_WATCHING = 2
        private const val FILTER_WATCHED = 3
    }
}
