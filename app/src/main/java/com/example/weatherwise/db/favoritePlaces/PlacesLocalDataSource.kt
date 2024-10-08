package com.example.weatherwise.db.favoritePlaces

import com.example.weatherwise.model.FavoritePlace

class PlacesLocalDataSource(val placesDao: PlacesDao) : IPlacesLocalDataSource {

    override suspend fun addPlace(place: FavoritePlace): Long {
        return placesDao.addPlace(place)
    }

    override suspend fun removePlace(place: FavoritePlace): Int {
        return placesDao.deletePlace(place)

    }

    override fun getAllLocalFavoritePlaces(): List<FavoritePlace> {
        return placesDao.getAllLocalPlaces()
    }
}