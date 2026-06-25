package com.example.cinelog.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.cinelog.R
import com.example.cinelog.data.remote.model.TmdbMovieDto
import com.example.cinelog.databinding.FragmentHomeBinding
import com.example.cinelog.ui.ViewModelFactory
import com.example.cinelog.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.cinelog.utils.showErrorSnackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var popularAdapter: MovieHorizontalAdapter
    private lateinit var trendingAdapter: MovieHorizontalAdapter
    private lateinit var indonesianAdapter: MovieHorizontalAdapter
    private lateinit var westSeriesAdapter: MovieHorizontalAdapter
    private lateinit var kDramaAdapter: MovieHorizontalAdapter
    private lateinit var indoSeriesAdapter: MovieHorizontalAdapter
    private lateinit var topRatedAdapter: MovieHorizontalAdapter
    private lateinit var nowPlayingAdapter: MovieHorizontalAdapter
    private lateinit var animeAdapter: MovieHorizontalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerViews() {


        popularAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvPopular.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = popularAdapter
        }

        trendingAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvTrending.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = trendingAdapter
        }

        indonesianAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvIndonesian.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = indonesianAdapter
        }

        westSeriesAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvWestSeries.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = westSeriesAdapter
        }

        kDramaAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvKDrama.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = kDramaAdapter
        }

        indoSeriesAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvIndoSeries.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = indoSeriesAdapter
        }

        topRatedAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvTopRated.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = topRatedAdapter
        }

        nowPlayingAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvNowPlaying.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = nowPlayingAdapter
        }

        animeAdapter = MovieHorizontalAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        binding.rvAnime.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = animeAdapter
        }

        setupBannerViewPager()
    }

    private fun setupBannerViewPager() {
        bannerAdapter = BannerAdapter { movie ->
            navigateToDetail(movie.id, movie.mediaType)
        }
        
        binding.vpBanner.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 3
            
            // Premium Page Transformer
            val pageTransformer = androidx.viewpager2.widget.CompositePageTransformer()
            pageTransformer.addTransformer(androidx.viewpager2.widget.MarginPageTransformer(10))
            pageTransformer.addTransformer { page, position ->
                val r = 1 - Math.abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
            setPageTransformer(pageTransformer)
        }
        
        binding.indicatorHome.setViewPager(binding.vpBanner)
        bannerAdapter.registerAdapterDataObserver(binding.indicatorHome.adapterDataObserver)
    }

    private fun setupListeners() {
        // Navigasi ke pencarian
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        // Filter genre aktual dengan TMDB Genre IDs
        binding.cgHome.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipAll
            when (checkedId) {
                R.id.chipAll -> viewModel.filterByGenre(null)
                R.id.chipDrama -> viewModel.filterByGenre(18)
                R.id.chipAction -> viewModel.filterByGenre(28)
                R.id.chipSciFi -> viewModel.filterByGenre(878)
                R.id.chipHorror -> viewModel.filterByGenre(27)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.pbHome.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trendingMovies.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    trendingAdapter.submitList(movies)
                    bannerAdapter.submitList(movies.take(10)) // Show top 10 items in carousel
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.popularMovies.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    popularAdapter.submitList(movies)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.indonesianMovies.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    indonesianAdapter.submitList(movies)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.westSeries.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    westSeriesAdapter.submitList(movies)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.kDrama.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    kDramaAdapter.submitList(movies)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.indoSeries.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    indoSeriesAdapter.submitList(movies)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topRatedMovies.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    topRatedAdapter.submitList(movies)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nowPlayingMovies.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    nowPlayingAdapter.submitList(movies)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.animeMovies.collectLatest { movies ->
                if (movies.isNotEmpty()) {
                    animeAdapter.submitList(movies)
                }
            }
        }



        // Error message observation
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { errorCode ->
                errorCode?.let {
                    binding.root.showErrorSnackbar("Informasi", it)
                }
            }
        }
    }

    private fun navigateToDetail(movieId: Int, mediaType: String? = "movie") {
        val bundle = Bundle().apply {
            putInt("movieId", movieId)
            putString("mediaType", mediaType)
        }
        findNavController().navigate(R.id.action_homeFragment_to_detailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
