package com.example.weatherapp.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private var cache: Cache? = null
    private lateinit var appContext: Context
    private const val TAG = "RetrofitInstance"
    private const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"

    fun initialize(context: Context) {
        try {
            Log.d(TAG, "Initializing RetrofitInstance")
            appContext = context.applicationContext
            cache = Cache(appContext.cacheDir, (10 * 1024 * 1024).toLong())
            Log.d(TAG, "Cache initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing: ${e.message}", e)
        }
    }

    private val client by lazy {
        try {
            Log.d(TAG, "Creating OkHttpClient")
            OkHttpClient.Builder().apply {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                if (::appContext.isInitialized) {
                    addInterceptor { chain ->
                        try {
                            var request = chain.request()
                            request = if (hasNetwork(appContext))
                                request.newBuilder()
                                    .header("Cache-Control", "public, max-age=5")
                                    .build()
                            else
                                request.newBuilder()
                                    .header("Cache-Control", "public, only-if-cached, max-stale=604800")
                                    .build()
                            chain.proceed(request)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in interceptor: ${e.message}", e)
                            throw e
                        }
                    }
                    cache?.let { cache(it) }
                } else {
                    Log.e(TAG, "AppContext not initialized")
                }
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
            }.build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating OkHttpClient: ${e.message}", e)
            throw e
        }
    }

    private val weatherRetrofit by lazy {
        try {
            Log.d(TAG, "Creating Weather Retrofit instance")
            Retrofit.Builder()
                .baseUrl(WEATHER_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Weather Retrofit: ${e.message}", e)
            throw e
        }
    }

    private val geocodingRetrofit by lazy {
        try {
            Log.d(TAG, "Creating Geocoding Retrofit instance")
            Retrofit.Builder()
                .baseUrl(GEOCODING_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Geocoding Retrofit: ${e.message}", e)
            throw e
        }
    }

    val weatherApi: WeatherApi by lazy {
        weatherRetrofit.create(WeatherApi::class.java)
    }

    val geocodingApi: GeocodingApi by lazy {
        geocodingRetrofit.create(GeocodingApi::class.java)
    }

    private fun hasNetwork(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network: ${e.message}", e)
            false
        }
    }
}