package com.example.weatherwise.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherwise.model.FavoritePlace

@Database(entities = [FavoritePlace::class], version = 3)
abstract class PlacesLocalDataSource:RoomDatabase() {
    abstract fun PlacesDao():PlacesDao

    companion object{
        private var INSTANCE:PlacesLocalDataSource? = null
        fun getInstance(context: Context):PlacesLocalDataSource{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(context, PlacesLocalDataSource::class.java, "placesDB")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}