package com.example.weatherapp.api

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double?,
    val hourly_units: HourlyUnits,
    val hourly: HourlyData
)

data class HourlyUnits(
    val time: String,
    val temperature_2m: String,
    val relative_humidity_2m: String,
    val apparent_temperature: String,
    val rain: String,
    val wind_speed_10m: String
)

data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double?>,
    val relative_humidity_2m: List<Int?>,
    val apparent_temperature: List<Double?>,
    val rain: List<Double?>,
    val wind_speed_10m: List<Double?>
) 