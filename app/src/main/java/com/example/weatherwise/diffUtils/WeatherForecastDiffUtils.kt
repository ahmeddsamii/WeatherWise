package com.example.weatherwise.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.example.weatherwise.model.ListElement
import com.example.weatherwise.model.WeatherForecastResponse

class WeatherForecastDiffUtils: DiffUtil.ItemCallback<ListElement>() {
    override fun areItemsTheSame(oldItem: ListElement, newItem: ListElement): Boolean {
        return oldItem.dt == newItem.dt
    }

    override fun areContentsTheSame(oldItem: ListElement, newItem: ListElement): Boolean {
        return oldItem == newItem
    }

}