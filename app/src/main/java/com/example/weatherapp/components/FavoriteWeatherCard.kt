package com.example.weatherapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.api.NetworkResult
import com.example.weatherapp.api.OpenMeteoResponse
import com.example.weatherapp.model.FavoriteCity

@Composable
fun FavoriteWeatherCard(
    favorite: FavoriteCity,
    weather: NetworkResult<OpenMeteoResponse>?,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${favorite.name}, ${favorite.country}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from favorites"
                    )
                }
            }

            when (weather) {
                is NetworkResult.Success -> {
                    Text("${weather.data.hourly.temperature_2m.firstOrNull()}°C")
                    Text("Humidity: ${weather.data.hourly.relative_humidity_2m.firstOrNull()}%")
                    Text("Wind: ${weather.data.hourly.wind_speed_10m.firstOrNull()} km/h")
                }
                is NetworkResult.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                is NetworkResult.Error -> {
                    Text(
                        text = weather.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                null -> {
                    Text("Loading weather data...")
                }
            }
        }
    }
} 