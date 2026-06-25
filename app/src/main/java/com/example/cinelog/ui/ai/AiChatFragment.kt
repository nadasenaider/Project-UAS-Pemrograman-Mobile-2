package com.example.cinelog.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cinelog.R
import com.example.cinelog.databinding.FragmentAiChatBinding
import com.example.cinelog.ui.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AiViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupKeyboardHandling()
        observeViewModel()
    }

    private fun setupKeyboardHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.llInput) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Tambahkan padding bawah sesuai tinggi keyboard
            // Jika keyboard tertutup, gunakan padding menu sistem (jika ada)
            v.setPadding(0, 0, 0, imeInsets.bottom + systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { movie ->
            val bundle = bundleOf(
                "movieId" to movie.id,
                "mediaType" to "movie"
            )
            findNavController().navigate(R.id.action_aiChatFragment_to_detailFragment, bundle)
        }

        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isNotBlank()) {
                viewModel.sendMessage(message)
                binding.etMessage.text.clear()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatMessages.collectLatest { messages ->
                chatAdapter.submitList(messages) {
                    // Scroll to bottom when new messages arrive
                    if (messages.isNotEmpty()) {
                        binding.rvChat.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isAiLoading.collectLatest { isLoading ->
                binding.btnSend.isEnabled = !isLoading
                binding.etMessage.isEnabled = !isLoading
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
