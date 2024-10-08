package com.example.weatherwise.ui.home.viewModel

import WeatherRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwise.uiState.UiState
import com.example.weatherwise.model.ListElement
import com.example.weatherwise.network.api.IWeatherRemoteDataSource
import com.example.weatherwise.repositories.IWeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(private val cRepo: IWeatherRepository) : ViewModel() {

    private val _hoursList = MutableStateFlow<UiState>(UiState.Loading)
    val hoursList = _hoursList.asStateFlow()

    private val _currentWeather = MutableStateFlow<UiState>(UiState.Loading)
    val currentWeather = _currentWeather.asStateFlow()

    private val _dailyForecast = MutableLiveData<Map<String, List<ListElement>>>()
    val dailyForecast: LiveData<Map<String, List<ListElement>>> = _dailyForecast

    fun getHoursList(lat: Double, long: Double, apiKey: String, unit: String, lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            cRepo.getWeatherForecast(lat, long, apiKey, unit, lang)
                .catch {
                    _hoursList.value = UiState.Failure(it.message!!)
                }.collect {
                    _hoursList.value = UiState.Success(it.list)
                }
        }
    }


    fun getCurrentWeather(lat: Double, long: Double, apiKey: String, unit: String, lang: String) {
        viewModelScope.launch {
            cRepo.getCurrentWeather(lat, long, apiKey, unit, lang)
                .catch { exception ->
                    _currentWeather.value = UiState.Failure(exception.message ?: "Unknown error")
                }
                .collect { response ->
                    _currentWeather.value = UiState.Success(response)
                }
        }
    }


    private fun processForecastDataByDay(forecastList: List<ListElement>) {
        val groupedByDay = forecastList.groupBy { it.dtTxt.split(" ")[0] }
        _dailyForecast.postValue(groupedByDay)
    }

    suspend fun getForecastDataByDay(
        lat: Double,
        long: Double,
        apiKey: String,
        unit: String,
        lang: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            cRepo.getWeatherForecast(lat, long, apiKey, unit, lang)
                .catch {
                    _hoursList.value = UiState.Failure(it.message!!)
                }.collect {
                    processForecastDataByDay(it.list)
                }
        }
    }


}