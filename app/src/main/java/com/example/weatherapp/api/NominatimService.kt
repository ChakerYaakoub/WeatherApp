package com.example.weatherapp.api

import retrofit2.http.GET
import retrofit2.http.Query

data class NominatimResponse(
    val display_name: String,
    val address: Address
)

data class Address(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null
)

interface NominatimService {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json"
    ): NominatimResponse
} 