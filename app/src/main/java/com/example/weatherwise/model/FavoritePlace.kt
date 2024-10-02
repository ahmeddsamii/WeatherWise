package com.example.weatherwise.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoritePlace(@PrimaryKey val address:String, val latitude:Double, val longitude:Double)
