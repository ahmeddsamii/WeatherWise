package com.example.weatherwise.ui.alert.viewModel

import WeatherRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.uiState.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AlertViewModel(val repository: WeatherRepository) : ViewModel() {

    private val _addedAlert = MutableStateFlow<UiState>(UiState.Loading)
    val addedAlert = _addedAlert.asStateFlow()


    private val _deletedAlert = MutableStateFlow<UiState>(UiState.Loading)
    val deletedAlert = _deletedAlert.asStateFlow()


    private val _allLocalAlerts = MutableStateFlow<UiState>(UiState.Loading)
    val allLocalAlerts = _allLocalAlerts.asStateFlow()

    private val _latitudeSharedFlow = MutableSharedFlow<Double>()
     val latitudeSharedFlow = _latitudeSharedFlow.asSharedFlow()

    private val _longitudeSharedFlow = MutableSharedFlow<Double>()
    val longitudeSharedFlow = _longitudeSharedFlow.asSharedFlow()

    private val _currentWeather = MutableStateFlow<UiState>(UiState.Loading)
    val currentWeather = _currentWeather.asStateFlow()





    fun addAlert(alertDto: AlertDto){
        viewModelScope.launch (Dispatchers.IO){
            repository.addAlert(alertDto)
                .catch {
                    _addedAlert.value = UiState.Failure(it.message!!)
                }.collect{
                    _addedAlert.value = UiState.Success(it)
                }
        }
    }


    fun deleteAlert(alertDto: AlertDto){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAlert(alertDto)
                .catch {
                    _deletedAlert.value = UiState.Failure(it.message!!)
                }.collect{
                    _deletedAlert.value = UiState.Success(it)
                    getAllLocalAlertsByDay()
                }
        }
    }


    fun getAllLocalAlertsByDay(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.getLocalAlertsByDate()
                .catch {
                    _allLocalAlerts.value = UiState.Failure(it.message!!)
                }.collect{
                    _allLocalAlerts.value = UiState.Success(it)
                }
        }
    }

    fun getCurrentWeather(latitude:Double,longitude:Double, apiKey:String, unit:String, lang:String){
        viewModelScope.launch {
            repository.getCurrentWeather(latitude,longitude,apiKey,unit,lang).catch {
                _currentWeather.value = UiState.Failure(it.message!!)
            }.collect {
                _currentWeather.value = UiState.Success(it)
            }
        }
    }


}