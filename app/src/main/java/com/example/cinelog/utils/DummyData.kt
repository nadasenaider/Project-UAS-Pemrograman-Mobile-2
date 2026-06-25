package com.example.cinelog.utils

import com.example.cinelog.data.remote.model.TmdbMovieDto
import com.example.cinelog.data.remote.model.TmdbMovieDetailDto
import com.example.cinelog.data.remote.model.TmdbGenreDto
import com.example.cinelog.data.remote.model.TmdbCastDto
import com.example.cinelog.data.remote.model.TmdbCreditsResponse
import com.example.cinelog.data.remote.model.TmdbCompanyDto

object DummyData {

    // Dummy data untuk film-film trending & populer jika API key bermasalah atau offline
    val dummyMovies = listOf(
        TmdbMovieDto(
            id = 872585,
            title = "Oppenheimer",
            posterPath = "/8Gxv2wY47YjyuPB7qZuqX3HYaXW.jpg",
            backdropPath = "/m8WWOC6r142CtSR2rQD4706gSqp.jpg",
            releaseDate = "2023-07-19",
            voteAverage = 8.2,
            overview = "Kisah fisikawan teoretis Amerika J. Robert Oppenheimer dan perannya dalam Proyek Manhattan yang menghasilkan bom atom pertama di dunia."
        ),
        TmdbMovieDto(
            id = 438631,
            title = "Dune: Part Two",
            posterPath = "/czembZBccSRL67i34aXMnncyuQ6.jpg",
            backdropPath = "/xOMo8BRK7PqaJ80dZ2Gg64Cc86e.jpg",
            releaseDate = "2024-02-27",
            voteAverage = 8.3,
            overview = "Paul Atreides bersatu dengan Chani dan Fremen saat membalas dendam terhadap para konspirator yang menghancurkan keluarganya."
        ),
        TmdbMovieDto(
            id = 157336,
            title = "Interstellar",
            posterPath = "/gEU2QvH353eRP7nBv64jE46nd1I.jpg",
            backdropPath = "/xJHokZbljvjC1OHrWKIQZec66ir.jpg",
            releaseDate = "2014-11-05",
            voteAverage = 8.4,
            overview = "Sekelompok penjelajah melakukan perjalanan melalui lubang cacing di luar angkasa dalam upaya untuk memastikan kelangsungan hidup umat manusia."
        ),
        TmdbMovieDto(
            id = 27205,
            title = "Inception",
            posterPath = "/ljsZTbVjmqrPyzTRrrmS6l6wR5z.jpg",
            backdropPath = "/s3TsuCVHQCX2QqYqilmS60F71Yg.jpg",
            releaseDate = "2010-07-15",
            voteAverage = 8.3,
            overview = "Seorang pencuri yang mencuri rahasia perusahaan melalui penggunaan teknologi berbagi mimpi diberi tugas sebaliknya: menanam ide ke dalam pikiran seorang CEO."
        ),
        TmdbMovieDto(
            id = 155,
            title = "The Dark Knight",
            posterPath = "/qJ2tWGBCeb6mN4mjm1hxmqStzsu.jpg",
            backdropPath = "/nMKd8Qdz1HO1bBE244KcYjH3jKl.jpg",
            releaseDate = "2008-07-16",
            voteAverage = 8.5,
            overview = "Ketika ancaman yang dikenal sebagai Joker mengacaukan kota Gotham, Batman harus menerima salah satu tes psikologis dan fisik terbesar untuk melawan ketidakadilan."
        ),
        TmdbMovieDto(
            id = 299536,
            title = "Avengers: Infinity War",
            posterPath = "/7WsyChwLEasfbnt1RYK0Bn0674q.jpg",
            backdropPath = "/bOGmA261j6pX31766OIuABwNR4y.jpg",
            releaseDate = "2018-04-25",
            voteAverage = 8.3,
            overview = "Avengers dan sekutu mereka harus bersedia mengorbankan segalanya dalam upaya untuk mengalahkan Thanos yang kuat sebelum ia menghancurkan alam semesta."
        )
    )

    fun getDummyMovieDetail(movieId: Int): TmdbMovieDetailDto {
        val baseMovie = dummyMovies.find { it.id == movieId } ?: dummyMovies[0]
        return TmdbMovieDetailDto(
            id = baseMovie.id,
            title = baseMovie.title,
            posterPath = baseMovie.posterPath,
            backdropPath = baseMovie.backdropPath,
            releaseDate = baseMovie.releaseDate,
            voteAverage = baseMovie.voteAverage,
            overview = baseMovie.overview,
            runtime = 180, // Oppenheimer = 180 min
            genres = listOf(
                TmdbGenreDto(18, "Drama"),
                TmdbGenreDto(36, "History"),
                TmdbGenreDto(878, "Sci-Fi")
            ),
            budget = 100000000L,
            productionCompanies = listOf(
                TmdbCompanyDto(1, "Universal Pictures"),
                TmdbCompanyDto(2, "Syncopy")
            ),
            originalLanguage = "en"
        )
    }

    fun getDummyMovieCredits(movieId: Int): TmdbCreditsResponse {
        return TmdbCreditsResponse(
            cast = listOf(
                TmdbCastDto(1, "Cillian Murphy", "J. Robert Oppenheimer", "/v9G61B5U7T3kC0U6D3X9b7k3.jpg"),
                TmdbCastDto(2, "Emily Blunt", "Katherine 'Kitty' Oppenheimer", "/n6434b7v9G61B5U7T3kC0U6D3.jpg"),
                TmdbCastDto(3, "Matt Damon", "Leslie Groves", "/mattdamoncastpath.jpg"),
                TmdbCastDto(4, "Robert Downey Jr.", "Lewis Strauss", "/rdjcastpath.jpg"),
                TmdbCastDto(5, "Florence Pugh", "Jean Tatlock", "/florencepughcastpath.jpg")
            )
        )
    }

    // Dummy user reviews untuk FeedFragment
    data class DummyReview(
        val userName: String,
        val userAvatar: String?,
        val movieTitle: String,
        val moviePoster: String?,
        val rating: Float,
        val reviewText: String,
        val timeAgo: String
    )

    val dummyReviews = listOf(
        DummyReview(
            userName = "Fahri Ramadhan",
            userAvatar = null,
            movieTitle = "Oppenheimer",
            moviePoster = "/8Gxv2wY47YjyuPB7qZuqX3HYaXW.jpg",
            rating = 4.5f,
            reviewText = "Sinematografi dan scoring dari Ludwig Göransson luar biasa! Cillian Murphy memberikan penampilan terbaik dalam karirnya.",
            timeAgo = "2 hours ago"
        ),
        DummyReview(
            userName = "Sarah Amalia",
            userAvatar = null,
            movieTitle = "Dune: Part Two",
            moviePoster = "/czembZBccSRL67i34aXMnncyuQ6.jpg",
            rating = 5.0f,
            reviewText = "Masterpiece fiksi ilmiah generasi ini. Denis Villeneuve tidak pernah gagal membuat visual yang megah dan memukau.",
            timeAgo = "5 hours ago"
        ),
        DummyReview(
            userName = "Budi Santoso",
            userAvatar = null,
            movieTitle = "Interstellar",
            moviePoster = "/gEU2QvH353eRP7nBv64jE46nd1I.jpg",
            rating = 4.8f,
            reviewText = "Menonton film ini untuk kesepuluh kalinya dan adegan perpustakaan/tesseract masih membuatku merinding.",
            timeAgo = "1 day ago"
        )
    )
}
