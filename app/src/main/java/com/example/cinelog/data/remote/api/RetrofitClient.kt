package com.example.cinelog.data.remote.api

import com.example.cinelog.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Interceptor untuk otomatis menyuntikkan api_key TMDB ke setiap request
    private val authInterceptor = Interceptor { chain ->
        val originalUrl = chain.request().url
        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("api_key", Constants.TMDB_API_KEY)
            .build()
        val newRequest = chain.request().newBuilder()
            .url(newUrl)
            .build()
        chain.proceed(newRequest)
    }

    // Logger Interceptor untuk mencetak log request-response di Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.TMDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: TmdbApiService by lazy {
        retrofit.create(TmdbApiService::class.java)
    }
}
