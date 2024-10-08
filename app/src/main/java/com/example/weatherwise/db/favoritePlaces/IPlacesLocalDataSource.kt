package com.example.weatherwise.db.favoritePlaces

import com.example.weatherwise.model.FavoritePlace

interface IPlacesLocalDataSource {
    suspend fun addPlace(place: FavoritePlace): Long

    suspend fun removePlace(place: FavoritePlace): Int
    fun getAllLocalFavoritePlaces(): List<FavoritePlace>
}