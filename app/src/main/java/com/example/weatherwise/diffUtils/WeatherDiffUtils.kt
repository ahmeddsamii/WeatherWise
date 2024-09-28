package com.example.weatherwise.diffUtils

import WeatherResponse
import androidx.recyclerview.widget.DiffUtil

class WeatherDiffUtils: DiffUtil.ItemCallback<WeatherResponse>() {
    override fun areItemsTheSame(oldItem: WeatherResponse, newItem: WeatherResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WeatherResponse, newItem: WeatherResponse): Boolean {
        return oldItem == newItem
    }
}