package com.example.weatherwise.db.alertPlaces

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherwise.model.AlertDto

@Database(entities = [AlertDto::class], version = 1)
abstract class AlertLocalDatabase:RoomDatabase() {

    abstract fun alertDao():AlertDao

    companion object{
        private var INSTANCE: AlertLocalDatabase? = null
        fun getInstance(context: Context):AlertLocalDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(context,AlertLocalDatabase::class.java, "alertDB").build()
                INSTANCE = instance
                instance
            }
        }
    }
}