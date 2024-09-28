package com.example.weatherwise.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.example.weatherwise.model.DailyWeather

class DailyWeatherDiffUtil: DiffUtil.ItemCallback<DailyWeather>() {
    override fun areItemsTheSame(oldItem: DailyWeather, newItem: DailyWeather): Boolean {
        return oldItem.dayOfWeek == newItem.dayOfWeek
    }

    override fun areContentsTheSame(oldItem: DailyWeather, newItem: DailyWeather): Boolean {
        return oldItem == newItem
    }
}