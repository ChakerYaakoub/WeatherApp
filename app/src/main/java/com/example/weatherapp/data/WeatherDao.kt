package com.example.weatherapp.data

import androidx.room.*
import com.example.weatherapp.model.CachedWeather
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM cached_weather WHERE cityId = :cityId")
    fun getWeatherForCity(cityId: String): Flow<CachedWeather?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: CachedWeather)

    @Query("DELETE FROM cached_weather WHERE cityId = :cityId")
    suspend fun deleteWeather(cityId: String)

    @Query("SELECT * FROM cached_weather")
    fun getAllCachedWeather(): Flow<List<CachedWeather>>
} 