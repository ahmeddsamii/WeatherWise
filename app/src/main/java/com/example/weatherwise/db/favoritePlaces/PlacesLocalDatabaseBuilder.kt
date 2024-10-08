package com.example.weatherwise.db.favoritePlaces

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherwise.model.FavoritePlace

@Database(entities = [FavoritePlace::class], version = 5)
abstract class PlacesLocalDatabaseBuilder:RoomDatabase() {
    abstract fun placesDao(): PlacesDao

    companion object{
        private var INSTANCE: PlacesLocalDatabaseBuilder? = null
        fun getInstance(context: Context): PlacesLocalDatabaseBuilder {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(context, PlacesLocalDatabaseBuilder::class.java, "placesDB")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }




}