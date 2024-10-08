package com.example.weatherwise.network.api

import WeatherResponse
import com.example.weatherwise.model.WeatherForecastResponse

class FakeRemoteDataSource(val weatherResponse: WeatherResponse, val WeatherForecastResponse:WeatherForecastResponse):IWeatherRemoteDataSource {
    override suspend fun getCurrentWeather(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ): WeatherResponse {
        return weatherResponse
    }

    override suspend fun getWeatherForecast(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ): WeatherForecastResponse {
        return WeatherForecastResponse
    }
}