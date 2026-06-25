package com.example.cinelog.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cinelog.R
import com.example.cinelog.databinding.FragmentSearchBinding
import com.example.cinelog.ui.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.cinelog.utils.showErrorSnackbar

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var gridAdapter: MovieGridAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        gridAdapter = MovieGridAdapter { movie ->
            val bundle = Bundle().apply {
                putInt("movieId", movie.id)
                putString("mediaType", movie.mediaType)
            }
            findNavController().navigate(R.id.action_searchFragment_to_detailFragment, bundle)
        }
        binding.rvSearchResults.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = gridAdapter
        }
    }

    private fun setupListeners() {
        // Debounce search input untuk menghindari pemanggilan API berlebihan saat mengetik
        binding.etSearch.doAfterTextChanged { text ->
            searchJob?.cancel()
            val query = text?.toString()?.trim().orEmpty()
            
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(500) // delay 500ms
                viewModel.searchMovies(query)
            }
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text?.toString()?.trim().orEmpty()
                viewModel.searchMovies(query)
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.pbSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collectLatest { results ->
                gridAdapter.submitList(results)
                
                val query = binding.etSearch.text.toString().trim()
                if (results.isEmpty() && query.isNotEmpty()) {
                    binding.llEmptyState.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                } else if (query.isEmpty() && results.isEmpty()) {
                    binding.llEmptyState.visibility = View.VISIBLE
                    binding.rvSearchResults.visibility = View.GONE
                } else {
                    binding.llEmptyState.visibility = View.GONE
                    binding.rvSearchResults.visibility = View.VISIBLE
                }
            }
        }

        // Error message observation
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { errorCode ->
                errorCode?.let {
                    binding.root.showErrorSnackbar("Pencarian", it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
