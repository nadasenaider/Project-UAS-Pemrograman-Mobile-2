package com.example.cinelog.ui.auth

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cinelog.R
import com.example.cinelog.databinding.FragmentLoginBinding
import com.example.cinelog.utils.Constants
import com.example.cinelog.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    // Launcher untuk hasil dari jendela Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    handleGoogleSignInSuccess(account)
                }
            } catch (e: ApiException) {
                Log.e("LoginFragment", "Google Sign-In failed", e)
                // Menampilkan status code agar tahu penyebab gagal (misal 10 atau 12500)
                Toast.makeText(requireContext(), "Gagal: ${e.statusCode} - ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            // Jika user membatalkan pilihan atau ada error sistem sebelum pilih akun
            Toast.makeText(requireContext(), "Sign-In dibatalkan atau error sistem", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupGoogleSignIn()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnLogin.setOnClickListener {
            if (validateInput()) {
                val email = binding.etEmail.text.toString().trim()
                val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                
                sessionManager.saveUserSession(name, email)
                Toast.makeText(requireContext(), "Welcome back, $name!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }

        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(Constants.GOOGLE_CLIENT_ID)
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun handleGoogleSignInSuccess(account: GoogleSignInAccount) {
        val name = account.displayName ?: "Google User"
        val email = account.email ?: ""
        val photoUrl = account.photoUrl?.toString() ?: ""

        // Tampilkan Dialog Konfirmasi (Lanjutkan)
        val builder = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Konfirmasi Akun")
        builder.setMessage("Halo $name!\n\nApakah Anda ingin melanjutkan ke CineLog dengan akun ini?")
        
        builder.setPositiveButton("LANJUTKAN") { _, _ ->
            sessionManager.saveUserSession(name, email)
            if (photoUrl.isNotEmpty()) {
                sessionManager.saveUserImage(photoUrl)
            }
            Toast.makeText(requireContext(), "Selamat Datang, $name!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
        
        builder.setNegativeButton("BATAL") { dialog, _ ->
            googleSignInClient.signOut()
            dialog.dismiss()
        }
        
        builder.setCancelable(false)
        builder.show()
    }

    private fun validateInput(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
