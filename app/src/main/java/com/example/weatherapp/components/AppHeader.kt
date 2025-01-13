package com.example.weatherapp.components
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    onFavoriteClick: () -> Unit,
    isSearchVisible: Boolean,
    onSearchVisibilityChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    TopAppBar(
        title = {
            if (isSearchVisible) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search location...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                Text("Weather App")
            }
        },
        actions = {
            IconButton(onClick = { onSearchVisibilityChange(!isSearchVisible) }) {
                Icon(
                    imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchVisible) "Close search" else "Open search"
                )
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorites"
                )
            }
        }
    )
} 