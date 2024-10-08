package com.example.weatherwise.network.api

import WeatherResponse
import com.example.weatherwise.Constants
import com.example.weatherwise.model.WeatherForecastResponse

class WeatherRemoteDataSource(val retrofit:RetrofitHelper) : IWeatherRemoteDataSource {
    override suspend fun getCurrentWeather(lat: Double, long: Double, apiKey: String, unit: String, lang: String): WeatherResponse {
        return retrofit.apiService.getCurrentWeather(lat,long, Constants.API_KEY,unit,lang)
    }

    override suspend fun getWeatherForecast(lat:Double, long:Double, apiKey:String, unit:String, lang:String): WeatherForecastResponse {
        return retrofit.apiService.getWeatherForecast(lat,long,apiKey,unit,lang)

    }
}