package com.example.weatherwise.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoritePlace(val address:String, @PrimaryKey val latitude:Double, val longitude:Double)
