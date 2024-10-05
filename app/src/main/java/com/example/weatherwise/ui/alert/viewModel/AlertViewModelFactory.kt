package com.example.weatherwise.ui.alert.viewModel

import WeatherRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AlertViewModelFactory(val repository: WeatherRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AlertViewModel::class.java)){
            AlertViewModel(repository) as T
        }else{
            throw IllegalArgumentException("viewModel class not found")
        }
    }
}