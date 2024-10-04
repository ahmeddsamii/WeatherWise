package com.example.weatherwise.ui.favorite.viewModel

import WeatherRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwise.model.FavoritePlace
import com.example.weatherwise.uiState.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FavoriteViewModel(val repo:WeatherRepository) : ViewModel() {

    private val _addedFavoritePlace = MutableStateFlow<UiState>(UiState.Loading)
    val addedFavoritePlace = _addedFavoritePlace.asStateFlow()

    private val _removedFavoritePlace = MutableStateFlow<UiState>(UiState.Loading)
    val removedFavoritePlace = _removedFavoritePlace.asStateFlow()

    private val _allLocalFavoritePlaces =  MutableStateFlow<UiState>(UiState.Loading)
    val allLocalFavoritePlaces = _allLocalFavoritePlaces.asStateFlow()


    fun addFavoritePlace(favoritePlace: FavoritePlace){
        viewModelScope.launch(Dispatchers.IO) {
            repo.addPlace(favoritePlace).catch {
                _addedFavoritePlace.value = UiState.Failure(it.message!!)
            }.collect{
                _addedFavoritePlace.value = UiState.Success(it)
            }
        }
    }

    fun removeFavoritePlace(favoritePlace: FavoritePlace){
        viewModelScope.launch(Dispatchers.IO) {
            repo.removePlace(favoritePlace).catch {
                _removedFavoritePlace.value = UiState.Failure(it.message!!)
            }.collect{
                _removedFavoritePlace.value = UiState.Success(it)
                getAllLocalFavoritePlaces()
            }
        }
    }

    suspend fun getAllLocalFavoritePlaces(){
        repo.getAllLocalFavoritePlaces().catch {
            _addedFavoritePlace.value = UiState.Failure(it.message!!)
        }
            .collect{
            _allLocalFavoritePlaces.value = UiState.Success(it)
        }
    }
}