package com.example.weatherapp.api

data class GeocodingResponse(
    val results: List<GeocodingResult>?,
    val generationtime_ms: Double
)

data class GeocodingResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val country: String,
    val admin1: String?,
    val admin2: String?
) 