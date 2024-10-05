package com.example.weatherwise.ui.alert.viewModel

import WeatherRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwise.model.AlertDto
import com.example.weatherwise.uiState.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AlertViewModel(val repository: WeatherRepository) : ViewModel() {

    val _addedAlert = MutableStateFlow<UiState>(UiState.Loading)
    val addedAlert = _addedAlert.asStateFlow()


    val _deletedAlert = MutableStateFlow<UiState>(UiState.Loading)
    val deletedAlert = _deletedAlert.asStateFlow()


    val _allLocalAlerts = MutableStateFlow<UiState>(UiState.Loading)
    val allLocalAlerts = _allLocalAlerts.asStateFlow()




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


}