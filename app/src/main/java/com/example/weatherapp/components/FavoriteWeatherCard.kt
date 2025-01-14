package com.example.weatherapp.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.api.NetworkResult
import com.example.weatherapp.api.OpenMeteoResponse
import com.example.weatherapp.model.FavoriteCity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign


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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Location name
                Text(
                    text = favorite.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Location details (if available)
                favorite.admin1?.let { admin ->
                    Text(
                        text = "$admin, ${favorite.country}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weather information
                when (weather) {
                    is NetworkResult.Success -> {
                        val currentTemp = weather.data.hourly.temperature_2m.firstOrNull()
                        val currentHumidity = weather.data.hourly.relative_humidity_2m.firstOrNull()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Temperature
                            WeatherInfoColumn(
                                value = "${currentTemp?.toInt() ?: "--"}°",
                                label = "Temperature",
                                icon = "🌡️"
                            )

                            // Humidity
                            WeatherInfoColumn(
                                value = "${currentHumidity ?: "--"}%",
                                label = "Humidity",
                                icon = "💧"
                            )

                            // Wind Speed
                            val currentWindSpeed = weather.data.hourly.wind_speed_10m.firstOrNull()
                            WeatherInfoColumn(
                                value = "${currentWindSpeed?.toInt() ?: "--"} km/h",
                                label = "Wind",
                                icon = "🌬️"
                            )
                        }
                    }
                    is NetworkResult.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(8.dp)
                        )
                    }
                    is NetworkResult.Error -> {
                        Text(
                            text = "Unable to load weather",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    null -> {
                        Text(
                            text = "No weather data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Delete button at the bottom
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            TextButton(
                onClick = onRemove,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Remove from favorites",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun getWeatherEmoji(humidity: Int): String {
    return when {
        humidity > 80 -> "🌧️"
        humidity > 60 -> "☁️"
        else -> "☀️"
    }
}

@Composable
private fun WeatherInfoColumn(
    value: String,
    label: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
} 