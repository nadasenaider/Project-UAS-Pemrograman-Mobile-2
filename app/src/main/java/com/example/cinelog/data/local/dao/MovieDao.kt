package com.example.cinelog.data.local.dao

import androidx.room.*
import com.example.cinelog.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Query("UPDATE movies SET status = :status WHERE id = :id")
    suspend fun updateMovieStatus(id: Int, status: String)

    @Query("UPDATE movies SET personalRating = :rating, personalNotes = :notes WHERE id = :id")
    suspend fun updatePersonalReview(id: Int, rating: Float, notes: String?)

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteMovies(): kotlinx.coroutines.flow.Flow<List<MovieEntity>>

    @Query("SELECT SUM(runtime) FROM movies WHERE status = 'WATCHED'")
    fun getTotalRuntime(): kotlinx.coroutines.flow.Flow<Int?>

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: Int)

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Int): MovieEntity?

    @Query("SELECT * FROM movies WHERE id = :id")
    fun getMovieByIdFlow(id: Int): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE status = :status ORDER BY addedAt DESC")
    fun getMoviesByStatus(status: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY addedAt DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT COUNT(*) FROM movies WHERE status = :status")
    fun getMovieCountByStatus(status: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM movies WHERE status = :status")
    suspend fun getMovieCountByStatusDirect(status: String): Int

    @Query("SELECT AVG(personalRating) FROM movies WHERE status = 'WATCHED' AND personalRating > 0")
    fun getAverageRating(): Flow<Float?>
}
