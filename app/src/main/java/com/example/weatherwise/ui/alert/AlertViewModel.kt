package com.example.weatherwise.ui.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlertViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is alert fragment"
    }
    val text: LiveData<String> = _text
}