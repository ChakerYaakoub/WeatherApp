package com.example.weatherapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.WeatherViewModel
import com.example.weatherapp.api.GeocodingResult
import androidx.compose.foundation.clickable

@Composable
fun SearchResultsOverlay(
    viewModel: WeatherViewModel,
    onLocationSelect: (GeocodingResult) -> Unit
) {
    val suggestions = viewModel.locationSuggestions.observeAsState()
    val isLoading = viewModel.isSearchLoading.observeAsState(initial = false)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            suggestions.value.isNullOrEmpty() -> {
                Text(
                    "No locations found",
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(suggestions.value ?: emptyList()) { location ->
                        ListItem(
                            headlineContent = { Text(location.name) },
                            supportingContent = { 
                                val locationDetails = buildString {
                                    append(location.country)
                                    location.admin1?.let { append(", $it") }
                                }
                                Text(locationDetails)
                            },
                            modifier = Modifier.clickable { onLocationSelect(location) }
                        )
                    }
                }
            }
        }
    }
} 