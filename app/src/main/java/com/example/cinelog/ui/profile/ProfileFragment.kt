package com.example.cinelog.ui.profile

import android.content.Intent
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
import com.example.cinelog.R
import com.example.cinelog.databinding.FragmentProfileBinding
import com.example.cinelog.ui.ViewModelFactory
import com.example.cinelog.ui.collection.CollectionViewModel
import com.example.cinelog.utils.Constants
import com.example.cinelog.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CollectionViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var reviewsAdapter: MyReviewsAdapter
    private lateinit var favoriteAdapter: MyReviewsAdapter

    private val imagePicker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            // Persist permission for local URIs
            try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(it, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            sessionManager.saveUserImage(it.toString())
            setupUserInfo()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupGoogleSignIn()

        setupUserInfo()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(Constants.GOOGLE_CLIENT_ID)
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupUserInfo() {
        val name = sessionManager.getUserName()
        val email = sessionManager.getUserEmail()
        val imageUri = sessionManager.getUserImage()

        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email

        if (!imageUri.isNullOrEmpty()) {
            binding.ivProfileImage.visibility = View.VISIBLE
            binding.tvProfileInitials.visibility = View.GONE
            com.bumptech.glide.Glide.with(this)
                .load(android.net.Uri.parse(imageUri))
                .circleCrop()
                .into(binding.ivProfileImage)
        } else {
            binding.ivProfileImage.visibility = View.GONE
            binding.tvProfileInitials.visibility = View.VISIBLE
            // Generate initials
            val initials = name.split(" ")
                .filter { it.isNotBlank() }
                .mapNotNull { it.firstOrNull() }
                .take(2)
                .joinToString("")
                .uppercase()
            binding.tvProfileInitials.text = initials
        }
    }

    private fun setupRecyclerView() {
        // Init Reviews Adapter
        reviewsAdapter = MyReviewsAdapter { movie ->
            openMovieDetail(movie.id, movie.mediaType)
        }
        binding.rvMyReviews.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = reviewsAdapter
        }

        // Init Favorite Adapter
        favoriteAdapter = MyReviewsAdapter { movie ->
            openMovieDetail(movie.id, movie.mediaType)
        }
        binding.rvFavoriteFilms.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = favoriteAdapter
        }
    }

    private fun openMovieDetail(movieId: Int, mediaType: String?) {
        val bundle = Bundle().apply {
            putInt("movieId", movieId)
            putString("mediaType", mediaType)
        }
        findNavController().navigate(R.id.action_profileFragment_to_detailFragment, bundle)
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    sessionManager.clearSession()
                    Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_profileFragment_to_onboardingFragment)
                }
            }
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.cvAvatar.setOnClickListener {
            imagePicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun showEditProfileDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Edit Profile Name")

        val input = android.widget.EditText(requireContext())
        input.setText(sessionManager.getUserName())
        input.setPadding(40, 40, 40, 40)
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotBlank()) {
                sessionManager.saveUserSession(newName, sessionManager.getUserEmail())
                setupUserInfo() // Refresh UI
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun observeViewModel() {
        // Pantau total watchlist
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.watchlistCount.collectLatest { count ->
                binding.tvStatWatchlist.text = count.toString()
            }
        }

        // Pantau total watched
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.watchedCount.collectLatest { count ->
                binding.tvStatWatched.text = count.toString()
            }
        }

        // Pantau ulasan personal (film yang berstatus WATCHED dan memiliki rating)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.watchedMovies.collectLatest { list ->
                val reviewedMovies = list.filter { it.personalRating > 0f }
                
                // Set Up Chip Filter Logic
                val checkedChipId = binding.cgProfileReviews.checkedChipId
                val filteredList = when (checkedChipId) {
                    R.id.chipReviewMovie -> reviewedMovies.filter { it.category == "movie" }
                    R.id.chipReviewSeries -> reviewedMovies.filter { it.category == "series" }
                    R.id.chipReviewKDrama -> reviewedMovies.filter { it.category == "kdrama" }
                    R.id.chipReviewAnime -> reviewedMovies.filter { it.category == "anime" }
                    else -> reviewedMovies // chipReviewAll
                }

                binding.tvStatReviews.text = reviewedMovies.size.toString()
                
                reviewsAdapter.submitList(filteredList)
                if (filteredList.isEmpty()) {
                    binding.tvNoReviews.text = if (reviewedMovies.isEmpty()) 
                        "You haven't reviewed any movie yet." 
                    else "No reviews in this category."
                    binding.tvNoReviews.visibility = View.VISIBLE
                    binding.rvMyReviews.visibility = View.GONE
                } else {
                    binding.tvNoReviews.visibility = View.GONE
                    binding.rvMyReviews.visibility = View.VISIBLE
                }
            }
        }

        // Pantau film favorit
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteMovies.collectLatest { list ->
                favoriteAdapter.submitList(list)
                binding.tvNoFavorites.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.rvFavoriteFilms.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        // Pantau total waktu tonton
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalRuntime.collectLatest { totalMinutes ->
                val minutes = totalMinutes ?: 0
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                binding.tvTotalWatchTime.text = if (hours > 0) {
                    "${hours}h ${remainingMinutes}m"
                } else {
                    "${remainingMinutes}m"
                }
            }
        }

        // Trigger update saat chip diklik
        binding.cgProfileReviews.setOnCheckedStateChangeListener { _, _ ->
            // Trigger observeViewModel re-run with latest list
            // In a better architecture, we'd use a filter StateFlow in ViewModel.
            // But for now, we just re-run the observation logic by potentially 
            // notifying the adapter or simply relying on the flow (since it's persistent).
            // Actually, we need to re-apply the filter. 
            // Let's just re-collect or use a local variable.
            
            // Hacky but effective for this context:
            viewLifecycleOwner.lifecycleScope.launch {
                val list = viewModel.watchedMovies.value
                val reviewedMovies = list.filter { it.personalRating > 0f }
                val checkedChipId = binding.cgProfileReviews.checkedChipId
                val filteredList = when (checkedChipId) {
                    R.id.chipReviewMovie -> reviewedMovies.filter { it.category == "movie" }
                    R.id.chipReviewSeries -> reviewedMovies.filter { it.category == "series" }
                    R.id.chipReviewKDrama -> reviewedMovies.filter { it.category == "kdrama" }
                    R.id.chipReviewAnime -> reviewedMovies.filter { it.category == "anime" }
                    else -> reviewedMovies
                }
                reviewsAdapter.submitList(filteredList)
                binding.tvNoReviews.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
                binding.rvMyReviews.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
