package com.example.weatherwise.db.favoritePlaces

import com.example.weatherwise.model.FavoritePlace

class FakePlaceLocalDataSource(var favoriteList: MutableList<FavoritePlace> = mutableListOf()):IPlacesLocalDataSource {
    override suspend fun addPlace(place: FavoritePlace): Long {
        return if (favoriteList.add(place)){
            1
        }else{
            0
        }
    }

    override suspend fun removePlace(place: FavoritePlace): Int {
        return if (favoriteList.remove(place)){
            1
        }else{
            0
        }
    }

    override fun getAllLocalFavoritePlaces(): List<FavoritePlace> {
        return favoriteList
    }
}