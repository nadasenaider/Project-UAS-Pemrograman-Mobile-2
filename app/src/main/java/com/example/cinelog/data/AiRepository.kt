package com.example.cinelog.data

import com.example.cinelog.utils.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AiRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = Constants.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )

    private val chat = generativeModel.startChat()

    private val systemInstruction = """
        Kamu adalah CineLog AI Assistant, asisten film pintar dan ramah.
        Tugasmu adalah membantu pengguna menemukan film atau serial TV yang cocok berdasarkan preferensi mereka.
        
        Aturan Penting:
        1. Selalu membalas dalam Bahasa Indonesia yang ramah dan membantu.
        2. Gunakan informasi tentang koleksi film pengguna jika diberikan untuk memberikan rekomendasi yang lebih personal.
        3. Jika kamu merekomendasikan film, sebutkan alasannya secara singkat.
        4. Kamu HARUS menyertakan daftar judul film yang kamu rekomendasikan di bagian akhir pesan dengan format khusus: [MOVIES: Judul1, Judul2, Judul3]. 
           Format ini penting agar aplikasi bisa menampilkan kartu film secara visual.
        5. Jika merekomendasikan lebih dari 5 film, batasi hanya 5 film terbaik saja.
    """.trimIndent()

    suspend fun sendMessage(userPrompt: String, context: String? = null): String {
        val fullPrompt = if (context != null) {
            "Instruksi Sistem: $systemInstruction\n\nKonteks Koleksi Pengguna: $context\n\nPesan Pengguna: $userPrompt"
        } else {
            "Instruksi Sistem: $systemInstruction\n\nPesan Pengguna: $userPrompt"
        }

        return try {
            val response = chat.sendMessage(fullPrompt)
            response.text ?: "Maaf, saya tidak bisa memproses permintaan Anda saat ini."
        } catch (e: Exception) {
            e.printStackTrace()
            "Terjadi kesalahan saat menghubungi asisten AI: ${e.message}"
        }
    }
}


