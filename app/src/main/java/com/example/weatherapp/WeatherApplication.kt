package com.example.weatherapp

import android.app.Application
import android.util.Log
import com.example.weatherapp.api.RetrofitInstance
import com.example.weatherapp.data.WeatherDatabase
import com.example.weatherapp.location.LocationManager

class WeatherApplication : Application() {
    lateinit var database: WeatherDatabase
    lateinit var locationManager: LocationManager

    override fun onCreate() {
        super.onCreate()
        try {
            Log.d("WeatherApplication", "Initializing application...")
            database = WeatherDatabase.getDatabase(this)
            locationManager = LocationManager(this)
            RetrofitInstance.initialize(this)
            Log.d("WeatherApplication", "Application initialized successfully")
        } catch (e: Exception) {
            Log.e("WeatherApplication", "Failed to initialize application", e)
        }
    }
} 