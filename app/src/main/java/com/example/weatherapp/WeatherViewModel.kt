package com.example.weatherapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.NetworkResult
import com.example.weatherapp.api.OpenMeteoResponse
import com.example.weatherapp.api.RetrofitInstance
import com.example.weatherapp.api.GeocodingResult
import com.example.weatherapp.model.FavoriteCity
import com.example.weatherapp.model.CachedWeather
import com.example.weatherapp.data.WeatherDao
import com.example.weatherapp.data.FavoriteDao
import com.example.weatherapp.location.LocationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class WeatherViewModel(
    private val weatherDao: WeatherDao,
    private val favoriteDao: FavoriteDao,
    private val locationManager: LocationManager
) : ViewModel() {
    private val _weatherData = MutableLiveData<NetworkResult<OpenMeteoResponse>>()
    val weatherData: LiveData<NetworkResult<OpenMeteoResponse>> = _weatherData

    private val _locationSuggestions = MutableLiveData<List<GeocodingResult>>()
    val locationSuggestions: LiveData<List<GeocodingResult>> = _locationSuggestions

    private val _isSearchLoading = MutableLiveData<Boolean>()
    val isSearchLoading: LiveData<Boolean> = _isSearchLoading

    private val _favorites = MutableLiveData<List<FavoriteCity>>(emptyList())
    val favorites: LiveData<List<FavoriteCity>> = _favorites

    private val _favoritesWeather = MutableLiveData<Map<String, NetworkResult<OpenMeteoResponse>>>(emptyMap())
    val favoritesWeather: LiveData<Map<String, NetworkResult<OpenMeteoResponse>>> = _favoritesWeather

    private var searchJob: Job? = null

    private val _currentLocationName = MutableLiveData<String>()
    val currentLocationName: LiveData<String> = _currentLocationName

    init {
        // Load favorites from database when ViewModel is created
        viewModelScope.launch {
            favoriteDao.getAllFavorites().collect { favorites ->
                _favorites.value = favorites
                refreshAllFavorites()
            }
        }
    }

    fun addToFavorites(location: GeocodingResult) {
        viewModelScope.launch {
            val favorite = FavoriteCity(
                name = location.name,
                country = location.country,
                admin1 = location.admin1,
                admin2 = location.admin2,
                latitude = location.latitude,
                longitude = location.longitude,
                elevation = location.elevation
            )
            
            // Save to database
            favoriteDao.insertFavorite(favorite)
            
            // Update UI
            val currentList = _favorites.value.orEmpty().toMutableList()
            if (!currentList.contains(favorite)) {
                currentList.add(favorite)
                _favorites.value = currentList
                refreshFavoriteWeather(favorite)
            }
        }
    }

    fun removeFromFavorites(favorite: FavoriteCity) {
        viewModelScope.launch {
            // Remove from database
            favoriteDao.deleteFavorite(favorite)
            
            // Update UI
            val currentList = _favorites.value.orEmpty().toMutableList()
            currentList.remove(favorite)
            _favorites.value = currentList
            
            val currentWeather = _favoritesWeather.value.orEmpty().toMutableMap()
            currentWeather.remove(favorite.name)
            _favoritesWeather.value = currentWeather
        }
    }

    private fun refreshFavoriteWeather(favorite: FavoriteCity) {
        viewModelScope.launch {
            try {
                val currentWeather = _favoritesWeather.value.orEmpty().toMutableMap()
                currentWeather[favorite.name] = NetworkResult.Loading
                _favoritesWeather.value = currentWeather

                val weatherResponse = RetrofitInstance.weatherApi.getWeatherData(
                    latitude = favorite.latitude,
                    longitude = favorite.longitude
                )
                
                // Cache the weather data
                weatherDao.insertWeather(CachedWeather(
                    cityId = favorite.name,
                    response = weatherResponse,
                    timestamp = System.currentTimeMillis()
                ))
                
                val updatedWeather = _favoritesWeather.value.orEmpty().toMutableMap()
                updatedWeather[favorite.name] = NetworkResult.Success(weatherResponse)
                _favoritesWeather.value = updatedWeather
            } catch (e: Exception) {
                // Try to get cached data on error
                try {
                    val cachedWeather = weatherDao.getWeatherForCity(favorite.name).first()
                    if (cachedWeather != null) {
                        val updatedWeather = _favoritesWeather.value.orEmpty().toMutableMap()
                        updatedWeather[favorite.name] = NetworkResult.Success(cachedWeather.response)
                        _favoritesWeather.value = updatedWeather
                    } else {
                        val updatedWeather = _favoritesWeather.value.orEmpty().toMutableMap()
                        updatedWeather[favorite.name] = NetworkResult.Error("Failed to load weather data")
                        _favoritesWeather.value = updatedWeather
                    }
                } catch (e: Exception) {
                    val updatedWeather = _favoritesWeather.value.orEmpty().toMutableMap()
                    updatedWeather[favorite.name] = NetworkResult.Error(e.message ?: "Unknown error occurred")
                    _favoritesWeather.value = updatedWeather
                }
            }
        }
    }

    fun searchLocations(query: String) {
        if (query.length < 2) {
            _locationSuggestions.value = emptyList()
            _isSearchLoading.value = false
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearchLoading.value = true
            delay(500) // Debounce search
            try {
                val response = RetrofitInstance.geocodingApi.searchLocation(query)
                _locationSuggestions.value = response.results ?: emptyList()
            } catch (e: Exception) {
                _locationSuggestions.value = emptyList()
            } finally {
                _isSearchLoading.value = false
            }
        }
    }

    fun getWeatherForLocation(location: GeocodingResult) {
        viewModelScope.launch {
            try {
                _weatherData.value = NetworkResult.Loading
                // Update location name
                _currentLocationName.value = buildString {
                    append(location.name)
                    location.admin1?.let { append(", $it") }
                }
                
                val weatherResponse = RetrofitInstance.weatherApi.getWeatherData(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                _weatherData.value = NetworkResult.Success(weatherResponse)
            } catch (e: Exception) {
                _weatherData.value = NetworkResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun getCurrentLocationWeather() {
        viewModelScope.launch {
            try {
                _weatherData.value = NetworkResult.Loading
                val location = locationManager.getCurrentLocation()
                
                if (location != null) {
                    // Get location name using reverse geocoding
                    try {
                        val geocodingResult = RetrofitInstance.geocodingApi.reverseGeocode(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            language = "en",
                            format = "json"
                        )
                        
                        // Update location name
                        _currentLocationName.value = geocodingResult.results?.firstOrNull()?.let { result ->
                            buildString {
                                append(result.name)
                                result.admin1?.let { admin -> append(", $admin") }
                            }
                        } ?: "Unknown Location"
                    } catch (e: Exception) {
                        Log.e("WeatherViewModel", "Error getting location name", e)
                        _currentLocationName.value = "Current Location"
                    }

                    // Get weather data
                    val weatherResponse = RetrofitInstance.weatherApi.getWeatherData(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                    _weatherData.value = NetworkResult.Success(weatherResponse)
                    
                    // Cache the weather data
                    weatherDao.insertWeather(CachedWeather(
                        cityId = "current_location",
                        response = weatherResponse,
                        timestamp = System.currentTimeMillis()
                    ))
                } else {
                    _currentLocationName.value = "Location Unavailable"
                    // ... rest of the error handling code ...
                }
            } catch (e: Exception) {
                _currentLocationName.value = "Error Loading Location"
                // ... rest of the error handling code ...
            }
        }
    }

    fun refreshAllFavorites() {
        _favorites.value?.forEach { refreshFavoriteWeather(it) }
    }
}













