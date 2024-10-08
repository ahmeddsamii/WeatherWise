package com.example.weatherwise.network.api

import WeatherResponse
import com.example.weatherwise.model.WeatherForecastResponse

interface IWeatherRemoteDataSource {
    suspend fun getCurrentWeather(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ): WeatherResponse

    suspend fun getWeatherForecast(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ): WeatherForecastResponse
}