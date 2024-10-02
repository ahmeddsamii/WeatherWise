package com.example.weatherwise.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.weatherwise.model.FavoritePlace
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacesDao {
    @Insert
    suspend fun addPlace(favoritePlace: FavoritePlace):Long

    @Delete
    suspend fun deletePlace(favoritePlace: FavoritePlace):Int

    @Query("SELECT * FROM FavoritePlace")
    fun getAllLocalPlaces():Flow<List<FavoritePlace>>
}