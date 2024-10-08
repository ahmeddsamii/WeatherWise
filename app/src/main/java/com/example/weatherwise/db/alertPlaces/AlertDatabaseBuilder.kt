package com.example.weatherwise.db.alertPlaces

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherwise.model.AlertDto

@Database(entities = [AlertDto::class], version = 5)
abstract class AlertDatabaseBuilder:RoomDatabase() {

    abstract fun alertDao():AlertDao

    companion object{
        private var INSTANCE: AlertDatabaseBuilder? = null
        fun getInstance(context: Context):AlertDatabaseBuilder{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(context,AlertDatabaseBuilder::class.java, "alertDB")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }



}