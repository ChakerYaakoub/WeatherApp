package com.example.weatherapp.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,wind_speed_10m",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}