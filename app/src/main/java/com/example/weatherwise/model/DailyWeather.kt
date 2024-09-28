package com.example.weatherwise.model

data class DailyWeather(val dayOfWeek:String, val imageIcon:String?=null, val maxTemp:String, val minTemp:String)