package com.example.weatherwise.network.api

import WeatherResponse
import com.example.weatherwise.Constants
import com.example.weatherwise.model.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private val instance = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = instance.create(ApiService::class.java)





}