package com.example.weatherwise.ui.favorite.viewModel

import WeatherRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwise.model.FavoritePlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteViewModel(val repo:WeatherRepository) : ViewModel() {

    private val _addedFavoritePlace = MutableLiveData<FavoritePlace>()
    val addedFavoritePlace:LiveData<FavoritePlace> = _addedFavoritePlace

    private val _removedFavoritePlace = MutableLiveData<FavoritePlace>()
    val removedFavoritePlace:LiveData<FavoritePlace> = _removedFavoritePlace

    private val _allLocalFavoritePlaces =  MutableLiveData<List<FavoritePlace>>()
    val allLocalFavoritePlaces:LiveData<List<FavoritePlace>> = _allLocalFavoritePlaces


    fun addFavoritePlace(favoritePlace: FavoritePlace){
        viewModelScope.launch(Dispatchers.IO) {
            repo.addPlace(favoritePlace)
            _addedFavoritePlace.postValue(favoritePlace)
        }
    }

    fun removeFavoritePlace(favoritePlace: FavoritePlace){
        viewModelScope.launch(Dispatchers.IO) {
            repo.removePlace(favoritePlace)
            _removedFavoritePlace.postValue(favoritePlace)
            getAllLocalFavoritePlaces()
        }
    }

    suspend fun getAllLocalFavoritePlaces(){
        repo.getAllLocalFavoritePlaces().collect{
            _allLocalFavoritePlaces.postValue(it)
        }
    }
}