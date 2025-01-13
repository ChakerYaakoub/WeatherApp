package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlin.system.exitProcess
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.weatherapp.api.GeocodingResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("MainActivity", "Uncaught exception in thread $thread", throwable)
            exitProcess(1)
        }

        try {
            Log.d("MainActivity", "Starting app...")
            setContent {
                MaterialTheme {
                    val application = applicationContext as WeatherApplication
                    val viewModel = WeatherViewModel(
                        weatherDao = application.database.weatherDao(),
                        favoriteDao = application.database.favoriteDao(),
                        locationManager = application.locationManager
                    )

                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    when (currentScreen) {
                        is Screen.Home -> {
                            WeatherPage(
                                viewModel = viewModel,
                                onFavoritesClick = {
                                    currentScreen = Screen.Favorites
                                }
                            )
                        }
                        is Screen.Favorites -> {
                            FavoritesPage(
                                viewModel = viewModel,
                                onBackClick = {
                                    currentScreen = Screen.Home
                                },
                                onFavoriteClick = { lat, lon, name ->
                                    viewModel.getWeatherForLocation(
                                        GeocodingResult(
                                            id = 0,
                                            name = name,
                                            latitude = lat,
                                            longitude = lon,
                                            country = "",
                                            admin1 = null,
                                            admin2 = null,
                                            elevation = 0.0
                                        )
                                    )
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            setContent {
                MaterialTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "An error occurred: ${e.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1234
    }
}

sealed class Screen {
    object Home : Screen()
    object Favorites : Screen()
}

