package com.example.weatherapp.data

import androidx.room.TypeConverter
import com.example.weatherapp.api.OpenMeteoResponse
import com.google.gson.Gson

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromOpenMeteoResponse(value: OpenMeteoResponse): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toOpenMeteoResponse(value: String): OpenMeteoResponse {
        return gson.fromJson(value, OpenMeteoResponse::class.java)
    }
} 