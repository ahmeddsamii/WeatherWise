package com.example.weatherwise.network.api

import WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("weather")
    suspend fun getCurrentWeather(@Query("lat") lat: Double, @Query("lon") lon: Double, @Query("appid") apiKey: String
    ): Response<WeatherResponse>


}