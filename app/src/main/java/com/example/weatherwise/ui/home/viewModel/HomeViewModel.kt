package com.example.weatherwise.ui.home.viewModel

import WeatherRepository
import WeatherResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwise.model.ListElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val cRepo: WeatherRepository) : ViewModel() {

    private val _hoursList = MutableLiveData<List<ListElement>>()
    val hoursList: LiveData<List<ListElement>> = _hoursList

    private val _currentWeather = MutableLiveData<WeatherResponse>()
    val currentWeather:LiveData<WeatherResponse> = _currentWeather

    private val _dailyForecast = MutableLiveData<Map<String, List<ListElement>>>()
    val dailyForecast: LiveData<Map<String, List<ListElement>>> = _dailyForecast

    fun getHoursList(lat: Double, long: Double, apiKey: String, lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = cRepo.getWeatherForecast(lat, long, apiKey,lang)
            if (response.isSuccessful) {
                _hoursList.postValue(response.body()?.list)
            }
        }
    }


    fun getCurrentWeather(lat: Double, long: Double, apiKey: String, lang:String){
        viewModelScope.launch(Dispatchers.IO) {
            val response = cRepo.getCurrentWeather(lat,long,apiKey, lang)
            if (response.isSuccessful){
                _currentWeather.postValue(response.body())
            }
        }
    }



    private fun processForecastDataByDay(forecastList: List<ListElement>) {
        val groupedByDay = forecastList.groupBy { it.dtTxt.split(" ")[0] }
        _dailyForecast.postValue(groupedByDay)
    }

    suspend fun getForecastDataByDay(lat: Double, long: Double, apiKey: String, lang: String){
        viewModelScope.launch(Dispatchers.IO) {
            val response = cRepo.getWeatherForecast(lat, long, apiKey,lang)
            if (response.isSuccessful) {
                processForecastDataByDay(response.body()?.list!!)
            }
        }
    }
}
