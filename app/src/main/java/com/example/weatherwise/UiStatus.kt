package com.example.weatherwise

sealed class UiStatus {
    class Success<T>(val data:T):UiStatus()
    class Failure(val errMessage:String):UiStatus()
    object Loading:UiStatus()
}