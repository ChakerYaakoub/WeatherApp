package com.example.weatherapp.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") query: String
    ): GeocodingResponse

    @GET("v1/reverse")
    suspend fun reverseGeocode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
} 