package com.example.cinelog.ui.onboarding

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cinelog.R
import com.example.cinelog.databinding.FragmentOnboardingBinding
import com.example.cinelog.utils.SessionManager

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())

        // Data untuk tiap halaman onboarding
        val onboardingItems = listOf(
            OnboardingItem(
                "Temukan Keajaiban Sinema",
                "Mulai perjalananmu dalam mencatat dan meninjau setiap film yang berkesan dalam hidupmu."
            ),
            OnboardingItem(
                "Cerdas dengan Fitur AI",
                "Dilengkapi fitur Chat AI untuk mencari rekomendasi film, serta Voice-to-Text untuk menulis ulasan profesional secara otomatis."
            ),
            OnboardingItem(
                "Siap Menjelajah?",
                "Bergabunglah dengan komunitas pecinta film dan kelola perpustakaan digital pribadimu sekarang!"
            )
        )

        val adapter = OnboardingAdapter(onboardingItems)
        binding.vpOnboarding.adapter = adapter

        // Logika Tombol Next
        binding.btnNext.setOnClickListener {
            val current = binding.vpOnboarding.currentItem
            if (current < onboardingItems.size - 1) {
                binding.vpOnboarding.setCurrentItem(current + 1, true)
            }
        }

        // Logika perubahan halaman (Swipe)
        binding.vpOnboarding.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
                
                if (position == onboardingItems.size - 1) {
                    // Halaman Terakhir: Tampilkan Get Started, Sembunyikan Next
                    binding.btnGetStarted.visibility = View.VISIBLE
                    binding.btnNext.visibility = View.INVISIBLE
                } else {
                    // Bukan Halaman Terakhir: Sembunyikan Get Started, Tampilkan Next
                    binding.btnGetStarted.visibility = View.INVISIBLE
                    binding.btnNext.visibility = View.VISIBLE
                }
            }
        })

        // Navigasi saat tombol "Get Started" diklik
        binding.btnGetStarted.setOnClickListener {
            sessionManager.setCompletedOnboarding(true)
            findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
        }

        // Navigasi saat teks tautan login diklik
        binding.tvLoginLink.setOnClickListener {
            sessionManager.setCompletedOnboarding(true)
            findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
        }
    }

    private fun updateDots(position: Int) {
        val active = R.drawable.dot_active
        val inactive = R.drawable.dot_inactive
        
        binding.dot1.setImageResource(if (position == 0) active else inactive)
        binding.dot2.setImageResource(if (position == 1) active else inactive)
        binding.dot3.setImageResource(if (position == 2) active else inactive)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
