package com.example.weatherapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.api.OpenMeteoResponse
import com.example.weatherapp.api.HourlyData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import java.time.format.DateTimeFormatter.ofPattern

@Composable
fun CurrentWeatherCard(
    locationName: String,
    weather: OpenMeteoResponse,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    selectedDayIndex: Int = 0,
    onDaySelected: (Int) -> Unit,
    isCurrentLocation: Boolean = false
) {
    val dayOffset = selectedDayIndex * 24
    val dayEndOffset = dayOffset + 24
    
    // Get temperatures for the selected day only
    val dayTemperatures = try {
        weather.hourly.temperature_2m
            .subList(dayOffset, dayEndOffset)
            .filterNotNull()
    } catch (e: Exception) {
        emptyList()
    }
    
    val currentTemp = dayTemperatures.firstOrNull()
    val minTemp = if (dayTemperatures.isNotEmpty()) dayTemperatures.minOrNull() else null
    val maxTemp = if (dayTemperatures.isNotEmpty()) dayTemperatures.maxOrNull() else null
    val avgTemp = if (dayTemperatures.isNotEmpty()) dayTemperatures.average() else null
    
    val currentHumidity = try {
        weather.hourly.relative_humidity_2m
            .subList(dayOffset, dayEndOffset)
            .firstOrNull()
    } catch (e: Exception) {
        null
    }
    
    val selectedDate = try {
        weather.hourly.time.getOrNull(dayOffset)?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
        }
    } catch (e: Exception) {
        null
    } ?: "--"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF87CEEB))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Favorite button in top right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // City name centered
            AutoSizeText(
                text = locationName.takeIf { it.isNotBlank() } ?: "Loading location..."
            )

            // Current Location or Date indicator
            if (isCurrentLocation || selectedDayIndex != 0) {
                Text(
                    text = if (isCurrentLocation) "Current Location" 
                           else selectedDate,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            if (selectedDayIndex == 0) {
                Text(
                    text = getCurrentTime(),
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Weather Display
            Text(
                text = getWeatherEmoji(currentHumidity ?: 0),
                fontSize = 64.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${currentTemp?.toInt() ?: "--"}°",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = getWeatherDescription(currentHumidity ?: 0),
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Min/Max/Avg Temperatures for selected day
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Min",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${minTemp?.toInt() ?: "--"}°",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Avg",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${avgTemp?.toInt() ?: "--"}°",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Max",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${maxTemp?.toInt() ?: "--"}°",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather Details (Humidity and Wind)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Humidity
                WeatherDetailRow(
                    icon = "💧",
                    value = "${currentHumidity ?: "--"}%",
                    label = "Humidity"
                )

                // Wind Speed
                val currentWindSpeed = try {
                    weather.hourly.wind_speed_10m
                        .subList(dayOffset, dayEndOffset)
                        .firstOrNull()
                } catch (e: Exception) {
                    null
                }
                
                WeatherDetailRow(
                    icon = "🌬️",
                    value = "${currentWindSpeed?.toInt() ?: "--"} km/h",
                    label = "Wind Speed"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hourly forecast for selected day
            HourlyForecastRow(
                hourlyData = weather.hourly,
                selectedDayIndex = selectedDayIndex
            )
            
            // Daily forecast
            DailyForecastSection(
                hourlyData = weather.hourly,
                selectedDayIndex = selectedDayIndex,
                onDaySelected = onDaySelected
            )
        }
    }
}

private fun getCurrentTime(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("EEE, h:mm a")
    return current.format(formatter)
}

private fun getWeatherDescription(humidity: Int): String {
    return when {
        humidity > 80 -> "Rainy"
        humidity > 60 -> "Cloudy"
        else -> "Sunny"
    }
}

private fun getWeatherEmoji(humidity: Int): String {
    return when {
        humidity > 80 -> "🌧🌧️"
        humidity > 60 -> "☁️"
        else -> "☀️"
    }
}

@Composable
private fun HourlyForecastRow(
    hourlyData: HourlyData,
    selectedDayIndex: Int
) {
    val currentHour = LocalDateTime.now().hour
    val startIndex = selectedDayIndex * 24
    val endIndex = startIndex + 24

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth(),
            state = rememberLazyListState(
                // Scroll to current hour if it's today
                initialFirstVisibleItemIndex = if (selectedDayIndex == 0) currentHour else 0
            )
        ) {
            items(
                items = (startIndex until endIndex).toList(),
                key = { it }
            ) { index ->
                val time = hourlyData.time[index]
                val temp = hourlyData.temperature_2m[index]
                val humidity = hourlyData.relative_humidity_2m[index]
                val isCurrentHour = selectedDayIndex == 0 && 
                    LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME).hour == currentHour
                
                HourlyWeatherItem(
                    time = time,
                    temperature = temp,
                    humidity = humidity,
                    isCurrentHour = isCurrentHour
                )
            }
        }
    }
}

@Composable
private fun HourlyWeatherItem(
    time: String,
    temperature: Double?,
    humidity: Int?,
    isCurrentHour: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .background(
                color = if (isCurrentHour)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isCurrentHour) 2.dp else 0.dp,
                color = if (isCurrentHour) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = formatHourFromDateTime(time),
            fontSize = 12.sp,
            color = if (isCurrentHour) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isCurrentHour) FontWeight.Bold else FontWeight.Normal,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = getWeatherEmoji(humidity ?: 0),
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${temperature?.toInt() ?: "--"}°",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

private fun formatHourFromDateTime(dateTimeString: String): String {
    val dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)
    return dateTime.format(DateTimeFormatter.ofPattern("h a"))
}

@Composable
private fun DailyForecastSection(
    hourlyData: HourlyData,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    val dailyData = remember(hourlyData) {
        hourlyData.time.chunked(24).take(5).mapIndexed { index, hours ->
            val temps = hourlyData.temperature_2m.subList(index * 24, (index + 1) * 24).filterNotNull()
            val avgTemp = temps.average()
            val minTemp = temps.minOrNull() ?: 0.0
            val maxTemp = temps.maxOrNull() ?: 0.0
            val date = LocalDateTime.parse(hours.first(), DateTimeFormatter.ISO_DATE_TIME)
            DayForecast(date, avgTemp, minTemp, maxTemp)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "5-Day Forecast",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dailyData.size) { index ->
                val day = dailyData[index]
                DailyWeatherItem(
                    dayForecast = day,
                    isSelected = selectedDayIndex == index,
                    onClick = {
                        onDaySelected(index)
                    }
                )
            }
        }
    }
}

data class DayForecast(
    val date: LocalDateTime,
    val avgTemp: Double,
    val minTemp: Double,
    val maxTemp: Double
)

@Composable
private fun DailyWeatherItem(
    dayForecast: DayForecast,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .background(
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = dayForecast.date.format(ofPattern("EEE")),
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = dayForecast.date.format(ofPattern("MMM d")),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${dayForecast.avgTemp.toInt()}°",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${dayForecast.minTemp.toInt()}°",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "${dayForecast.maxTemp.toInt()}°",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun WeatherDetailRow(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    var fontSize by remember { mutableStateOf(28.sp) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Visible,
        onTextLayout = { textLayoutResult ->
            if (!readyToDraw && textLayoutResult.hasVisualOverflow) {
                fontSize *= 0.9f
            } else {
                readyToDraw = true
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 4.dp)
    )
} 