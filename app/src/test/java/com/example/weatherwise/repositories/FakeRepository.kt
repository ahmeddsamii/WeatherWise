package com.example.weatherwise.repositories

import WeatherResponse
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.model.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow

class FakeRepository():IWeatherRepository {
    override suspend fun getCurrentWeather(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ): Flow<WeatherResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getWeatherForecast(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ): Flow<WeatherForecastResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun addPlace(place: FavoritePlace): Flow<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun removePlace(place: FavoritePlace): Flow<Int> {
        TODO("Not yet implemented")
    }

    override fun getAllLocalFavoritePlaces(): Flow<List<FavoritePlace>> {
        TODO("Not yet implemented")
    }

    override fun addAlert(alertDto: AlertDto): Flow<Long> {
        TODO("Not yet implemented")
    }

    override fun deleteAlert(alertDto: AlertDto): Flow<Int> {
        TODO("Not yet implemented")
    }

    override fun getLocalAlertsByDate(): Flow<List<AlertDto>> {
        TODO("Not yet implemented")
    }
}