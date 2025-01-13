package com.example.weatherapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherapp.api.OpenMeteoResponse
import com.example.weatherapp.data.Converters

@Entity(tableName = "cached_weather")
@TypeConverters(Converters::class)
data class CachedWeather(
    @PrimaryKey
    val cityId: String,
    val response: OpenMeteoResponse,
    val timestamp: Long
) 