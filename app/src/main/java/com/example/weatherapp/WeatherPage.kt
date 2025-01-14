package com.example.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.api.NetworkResult
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.weatherapp.api.GeocodingResult
import com.example.weatherapp.components.AppHeader
import com.example.weatherapp.components.CurrentWeatherCard
import com.example.weatherapp.components.FavoriteWeatherCard
import com.example.weatherapp.components.SearchResultsOverlay
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn

@Composable
fun WeatherPage(
    viewModel: WeatherViewModel,
    onFavoritesClick: () -> Unit
) {
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val weatherState = viewModel.weatherData.observeAsState()
    val favorites = viewModel.favorites.observeAsState(initial = emptyList())
    val favoritesWeather = viewModel.favoritesWeather.observeAsState(initial = emptyMap())
    val currentLocationName = viewModel.currentLocationName.observeAsState(initial = "Loading...")
    var selectedDayIndex by remember { mutableStateOf(0) }
    val isCurrentLocation = viewModel.isCurrentLocation.observeAsState(initial = true)

    LaunchedEffect(Unit) {
        viewModel.getCurrentLocationWeather()
    }

    Scaffold(
        topBar = {
            AppHeader(
                onFavoriteClick = onFavoritesClick,
                isSearchVisible = isSearchVisible,
                onSearchVisibilityChange = { isSearchVisible = it },
                searchQuery = searchQuery,
                onSearchQueryChange = { 
                    searchQuery = it
                    viewModel.searchLocations(it)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val result = weatherState.value) {
                is NetworkResult.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        CurrentWeatherCard(
                            locationName = viewModel.currentLocationName.value ?: "",
                            weather = result.data,
                            isFavorite = favorites.value.any { it.name == currentLocationName.value },
                            onToggleFavorite = {
                                val currentLocation = GeocodingResult(
                                    id = 0,
                                    name = currentLocationName.value,
                                    latitude = result.data.latitude,
                                    longitude = result.data.longitude,
                                    country = "", // You might want to get this from API
                                    admin1 = null,
                                    admin2 = null,
                                    elevation = result.data.elevation ?: 0.0
                                )
                                if (favorites.value.any { it.name == currentLocationName.value }) {
                                    viewModel.removeFromFavorites(favorites.value.first { it.name == currentLocationName.value })
                                } else {
                                    viewModel.addToFavorites(currentLocation)
                                }
                            },
                            selectedDayIndex = selectedDayIndex,
                            onDaySelected = { newIndex ->
                                selectedDayIndex = newIndex
                            },
                            isCurrentLocation = isCurrentLocation.value
                        )

                        // Favorites section
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )

                        if (favorites.value.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "No favorites",
                                    modifier = Modifier
                                        .size(40.dp) // Reduced size
                                        .padding(bottom = 8.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )

                                Text(
                                    text = "No favorite locations yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Search for a city and add it to favorites",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        } else {
                            favorites.value.forEach { favorite ->
                                FavoriteWeatherCard(
                                    favorite = favorite,
                                    weather = favoritesWeather.value[favorite.name],
                                    onRemove = { viewModel.removeFromFavorites(favorite) },
                                    onClick = {
                                        viewModel.getWeatherForLocation(GeocodingResult(
                                            id = 0,
                                            name = favorite.name,
                                            latitude = favorite.latitude,
                                            longitude = favorite.longitude,
                                            country = favorite.country,
                                            admin1 = favorite.admin1,
                                            admin2 = favorite.admin2,
                                            elevation = favorite.elevation
                                        ))
                                    }
                                )
                            }
                        }
                    }
                }
                is NetworkResult.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is NetworkResult.Error -> {
    Column(
        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
                        Text(
                            text = "Error loading weather data",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
                        Button(
                            onClick = { viewModel.getCurrentLocationWeather() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Search Results Overlay
            if (isSearchVisible && searchQuery.length >= 2) {
                SearchResultsOverlay(
                    viewModel = viewModel,
                    onLocationSelect = { location ->
                        viewModel.getWeatherForLocation(location)
                        searchQuery = ""
                        isSearchVisible = false
                    }
                )
            }
        }
    }
}















