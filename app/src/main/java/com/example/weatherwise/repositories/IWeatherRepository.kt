package com.example.weatherwise.repositories

import WeatherResponse
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.model.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow

interface IWeatherRepository {
    suspend fun getCurrentWeather(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ): Flow<WeatherResponse>

    suspend fun getWeatherForecast(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ): Flow<WeatherForecastResponse>

    suspend fun addPlace(place: FavoritePlace): Flow<Long>
    suspend fun removePlace(place: FavoritePlace): Flow<Int>
    fun getAllLocalFavoritePlaces(): Flow<List<FavoritePlace>>
    fun addAlert(alertDto: AlertDto): Flow<Long>
    fun deleteAlert(alertDto: AlertDto): Flow<Int>
    fun getLocalAlertsByDate(): Flow<List<AlertDto>>
}