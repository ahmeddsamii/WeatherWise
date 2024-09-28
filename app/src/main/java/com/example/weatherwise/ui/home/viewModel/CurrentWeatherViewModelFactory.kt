package com.example.weatherwise.ui.home.viewModel

import WeatherRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CurrentWeatherViewModelFactory(private val cRepo:WeatherRepository)
    :ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(HomeViewModel::class.java)){
            HomeViewModel(cRepo) as T
        }else{
            throw IllegalArgumentException("viewModel class not found")
        }
    }
}