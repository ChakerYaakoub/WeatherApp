package com.example.weatherapp.data

import androidx.room.*
import com.example.weatherapp.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_cities")
    fun getAllFavorites(): Flow<List<FavoriteCity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteCity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteCity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_cities WHERE name = :name LIMIT 1)")
    suspend fun isFavorite(name: String): Boolean
} 