package com.example.weatherapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.components.FavoriteWeatherCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesPage(
    viewModel: WeatherViewModel,
    onBackClick: () -> Unit,
    onFavoriteClick: (latitude: Double, longitude: Double, name: String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val favorites = viewModel.favorites.observeAsState(initial = emptyList())
        val favoritesWeather = viewModel.favoritesWeather.observeAsState(initial = emptyMap())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (favorites.value.isEmpty()) {
                item {
                    Text(
                        text = "No favorite locations added yet",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(favorites.value) { favorite ->
                    FavoriteWeatherCard(
                        favorite = favorite,
                        weather = favoritesWeather.value[favorite.name],
                        onRemove = { viewModel.removeFromFavorites(favorite) },
                        onClick = {
                            onFavoriteClick(
                                favorite.latitude,
                                favorite.longitude,
                                favorite.name
                            )
                        }
                    )
                }
            }
        }
    }
} 