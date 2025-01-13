package com.example.weatherapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_cities")
data class FavoriteCity(
    @PrimaryKey
    val name: String,
    val country: String,
    val admin1: String?,
    val admin2: String?,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double
) 