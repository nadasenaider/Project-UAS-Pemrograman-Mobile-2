package com.example.cinelog.ui.detail

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.cinelog.R
import com.example.cinelog.data.local.entity.MovieEntity
import com.example.cinelog.data.remote.model.TmdbMovieDetailDto
import com.example.cinelog.databinding.DialogAddToCollectionBinding
import com.example.cinelog.databinding.FragmentDetailBinding
import com.example.cinelog.ui.ViewModelFactory
import com.example.cinelog.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.cinelog.utils.showSuccessSnackbar
import com.example.cinelog.utils.showErrorSnackbar
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var castAdapter: CastAdapter
    private var currentMovieId: Int = -1
    private var loadedMovieDetail: TmdbMovieDetailDto? = null
    private var localState: MovieEntity? = null

    // Speech to Text related
    private var sttCallback: ((String) -> Unit)? = null
    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                sttCallback?.invoke(matches[0])
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(requireContext(), "Izin mikrofon diperlukan untuk merekam VN", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentMovieId = arguments?.getInt("movieId") ?: -1
        val mediaType = arguments?.getString("mediaType") ?: "movie"
        
        if (currentMovieId == -1) {
            Toast.makeText(requireContext(), "ID film tidak valid", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadMovieDetails(currentMovieId, mediaType)
    }

    private fun setupRecyclerView() {
        castAdapter = CastAdapter()
        binding.rvCast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnActionCollection.setOnClickListener {
            loadedMovieDetail?.let { detail ->
                showCollectionDialog(detail)
            }
        }

        binding.btnShare.setOnClickListener {
            loadedMovieDetail?.let { detail ->
                shareMovieImage(detail)
            }
        }

        binding.btnEditReview.setOnClickListener {
            loadedMovieDetail?.let { detail ->
                showCollectionDialog(detail)
            }
        }

        binding.btnFavorite.setOnClickListener {
            localState?.let { state ->
                viewModel.toggleFavorite(state.id, state.isFavorite)
            } ?: run {
                Toast.makeText(requireContext(), "Tambahkan ke koleksi dulu untuk jadi favorit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // Loading
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.pbDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Details
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieDetail.collectLatest { detail ->
                detail?.let {
                    loadedMovieDetail = it
                    bindMovieDetails(it)
                }
            }
        }

        // Cast
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieCast.collectLatest { cast ->
                castAdapter.submitList(cast)
            }
        }

        // Local state (Room DB status)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.localMovieState.collectLatest { state ->
                localState = state
                updateCollectionButtonState(state)
            }
        }

        // Error message
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { errorCode ->
                errorCode?.let {
                    binding.root.showErrorSnackbar("Gagal Memuat", it)
                }
            }
        }

        // Trailer
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trailerVideoId.collectLatest { videoId ->
                if (videoId != null) {
                    binding.btnPlayTrailer.visibility = View.VISIBLE
                    binding.btnPlayTrailer.setOnClickListener {
                        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                        try {
                            startActivity(appIntent)
                        } catch (e: Exception) {
                            startActivity(webIntent)
                        }
                    }
                } else {
                    binding.btnPlayTrailer.visibility = View.GONE
                }
            }
        }
    }

    private fun bindMovieDetails(detail: TmdbMovieDetailDto) {
        binding.tvDetailTitle.text = detail.title
        
        // Year & Genre & Runtime
        val year = if (!detail.releaseDate.isNullOrBlank() && detail.releaseDate.length >= 4) {
            detail.releaseDate.substring(0, 4)
        } else {
            "-"
        }
        val genres = detail.genres?.joinToString(", ") { it.name ?: "Unknown" } ?: "No genres"
        val runtimeHours = if (detail.runtime != null) {
            val hrs = detail.runtime / 60
            val mins = detail.runtime % 60
            if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
        } else {
            "-"
        }
        binding.tvDetailYearGenre.text = "$year • $genres • $runtimeHours"
        
        // Season & Episode info (Specific for Series/Anime/KDrama)
        if (detail.mediaType == "tv" || (detail.numberOfSeasons ?: 0) > 0) {
            binding.tvDetailSeasonEpisode.visibility = View.VISIBLE
            val seasonText = "Seasons: ${detail.numberOfSeasons ?: 0}"
            val episodeText = "Episodes: ${detail.numberOfEpisodes ?: 0}"
            binding.tvDetailSeasonEpisode.text = "$seasonText • $episodeText"
        } else {
            binding.tvDetailSeasonEpisode.visibility = View.GONE
        }


        // Score Card
        binding.tvDetailScore.text = String.format(Locale.US, "%.1f/10", detail.voteAverage ?: 0.0)
        binding.tvDetailLang.text = detail.originalLanguage?.uppercase() ?: "EN"
        binding.tvDetailMetascore.text = ((detail.voteAverage ?: 0.0) * 10 + 3).toInt().coerceAtMost(100).toString()

        // Overview
        binding.tvDetailOverview.text = detail.overview

        // Images
        val backdropUrl = if (!detail.backdropPath.isNullOrBlank()) {
            Constants.TMDB_IMAGE_BASE_URL + detail.backdropPath
        } else {
            Constants.TMDB_IMAGE_BASE_URL + detail.posterPath
        }
        val posterUrl = if (!detail.posterPath.isNullOrBlank()) {
            Constants.TMDB_IMAGE_BASE_URL + detail.posterPath
        } else {
            null
        }

        Glide.with(this)
            .load(backdropUrl)
            .placeholder(R.drawable.bg_onboarding)
            .error(R.drawable.bg_onboarding)
            .into(binding.ivDetailBackdrop)

        Glide.with(this)
            .load(posterUrl)
            .placeholder(R.drawable.bg_onboarding)
            .error(R.drawable.bg_onboarding)
            .into(binding.ivDetailPoster)
    }

    private fun updateCollectionButtonState(state: MovieEntity?) {
        if (state == null) {
            binding.btnActionCollection.text = "+ ADD TO COLLECTION"
            binding.cvPersonalReview.visibility = View.GONE
            binding.btnFavorite.visibility = View.GONE
        } else {
            binding.btnFavorite.visibility = View.VISIBLE
            if (state.isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
                binding.btnFavorite.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_gold))
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
                binding.btnFavorite.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_primary))
            }

            val statusText = when (state.status) {
                Constants.STATUS_WATCHLIST -> "WATCHLIST"
                Constants.STATUS_WATCHING -> "WATCHING"
                Constants.STATUS_WATCHED -> "WATCHED"
                else -> "IN COLLECTION"
            }
            binding.btnActionCollection.text = "✓ $statusText (UPDATE)"
            
            // Show review card if it is WATCHED
            if (state.status == Constants.STATUS_WATCHED) {
                binding.cvPersonalReview.visibility = View.VISIBLE
                binding.rbPersonal.rating = state.personalRating
                
                if (!state.personalNotes.isNullOrBlank()) {
                    binding.tvPersonalNotes.text = "\"${state.personalNotes}\""
                    binding.tvPersonalNotes.visibility = View.VISIBLE
                } else {
                    binding.tvPersonalNotes.visibility = View.GONE
                }
            } else {
                binding.cvPersonalReview.visibility = View.GONE
            }
        }
    }

    private fun showCollectionDialog(detail: TmdbMovieDetailDto) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAddToCollectionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.tvDialogMovieTitle.text = detail.title
        
        // Prepopulate if movie exists in local db
        localState?.let { state ->
            dialogBinding.btnDialogRemove.visibility = View.VISIBLE
            when (state.status) {
                Constants.STATUS_WATCHLIST -> dialogBinding.rbWatchlist.isChecked = true
                Constants.STATUS_WATCHING -> dialogBinding.rbWatching.isChecked = true
                Constants.STATUS_WATCHED -> {
                    dialogBinding.rbWatched.isChecked = true
                    dialogBinding.llWatchedReview.visibility = View.VISIBLE
                    dialogBinding.dialogRatingBar.rating = state.personalRating
                    dialogBinding.etDialogNotes.setText(state.personalNotes)
                }
            }
        }

        // Mic Button Listener for Speech to Text (End Icon in TextInputLayout)
        dialogBinding.tilNotes.setEndIconOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                return@setEndIconOnClickListener
            }

            startSpeechToText { result ->
                val currentText = dialogBinding.etDialogNotes.text?.toString() ?: ""
                val newText = if (currentText.isEmpty()) result else "$currentText $result"
                dialogBinding.etDialogNotes.setText(newText)
                dialogBinding.etDialogNotes.setSelection(newText.length)
            }
        }

        // Show AI button only if notes are not blank
        dialogBinding.etDialogNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dialogBinding.btnAiOptimize.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dialogBinding.btnAiOptimize.setOnClickListener {
            val text = dialogBinding.etDialogNotes.text.toString()
            if (text.isNotBlank()) {
                viewModel.beautifyReview(rawText = text, movieTitle = detail.title ?: "Unknown Movie")
            }
        }

        // Observe AI changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isAiLoading.collect { isLoading ->
                        dialogBinding.pbAiLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
                        dialogBinding.btnAiOptimize.isEnabled = !isLoading
                        dialogBinding.btnDialogSave.isEnabled = !isLoading
                    }
                }
                launch {
                    viewModel.beautifiedReview.collect { beautified ->
                        if (beautified != null) {
                            dialogBinding.etDialogNotes.setText(beautified)
                            dialogBinding.etDialogNotes.setSelection(beautified?.length ?: 0)
                            viewModel.resetBeautifiedReview()
                        }
                    }
                }
            }
        }

        // Listener for watched state to expand rating details
        dialogBinding.rgStatus.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbWatched) {
                dialogBinding.llWatchedReview.visibility = View.VISIBLE
            } else {
                dialogBinding.llWatchedReview.visibility = View.GONE
            }
        }

        dialogBinding.btnDialogSave.setOnClickListener {
            val status = when (dialogBinding.rgStatus.checkedRadioButtonId) {
                R.id.rbWatchlist -> Constants.STATUS_WATCHLIST
                R.id.rbWatching -> Constants.STATUS_WATCHING
                R.id.rbWatched -> Constants.STATUS_WATCHED
                else -> null
            }

            if (status == null) {
                Toast.makeText(requireContext(), "Please select a status", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rating = if (status == Constants.STATUS_WATCHED) {
                dialogBinding.dialogRatingBar.rating
            } else {
                0f
            }

            val notes = if (status == Constants.STATUS_WATCHED) {
                dialogBinding.etDialogNotes.text?.toString()?.trim()
            } else {
                null
            }

            viewModel.addMovieToCollection(detail, status, rating, notes)
            binding.root.showSuccessSnackbar(
                getString(R.string.success_add_collection),
                getString(R.string.msg_movie_saved_format, detail.title)
            )
            dialog.dismiss()
        }

        dialogBinding.btnDialogRemove.setOnClickListener {
            viewModel.removeMovieFromCollection(detail.id)
            Toast.makeText(requireContext(), "${detail.title} removed from collection", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun shareMovieImage(detail: TmdbMovieDetailDto) {
        val imageUrl = Constants.TMDB_IMAGE_BASE_URL + detail.posterPath
        
        Toast.makeText(requireContext(), "Menyiapkan gambar...", Toast.LENGTH_SHORT).show()
        
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveAndShareImage(resource, detail.title ?: "Movie")
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun saveAndShareImage(bitmap: Bitmap, title: String) {
        try {
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val stream = FileOutputStream("$cachePath/shared_image.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imageFile = File(cachePath, "shared_image.png")
            val contentUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", imageFile)

            if (contentUri != null) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(contentUri, requireContext().contentResolver.getType(contentUri))
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_TEXT, "Check out this movie on CineLog: $title")
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal menyiapkan gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSpeechToText(onResult: (String) -> Unit) {
        sttCallback = onResult
        val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Silakan bicara...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Fitur suara tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
