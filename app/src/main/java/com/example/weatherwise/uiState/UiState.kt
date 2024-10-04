package com.example.weatherwise.uiState

sealed class UiState {
    class Success<T>(val data:T): UiState()
    class Failure(val errMessage:String): UiState()
    object Loading: UiState()
}