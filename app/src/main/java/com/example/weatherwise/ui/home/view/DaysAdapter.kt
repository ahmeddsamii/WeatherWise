package com.example.weatherwise.ui.home.view

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weatherwise.Constants
import com.example.weatherwise.R
import com.example.weatherwise.databinding.DayItemBinding
import com.example.weatherwise.diffUtils.DailyWeatherDiffUtil
import com.example.weatherwise.model.DailyWeather
import com.example.weatherwise.model.TempUnit
import kotlin.time.times

class DaysAdapter: ListAdapter<DailyWeather, DaysAdapter.DayViewHolder>(DailyWeatherDiffUtil()) {
    lateinit var binding:DayItemBinding
    private lateinit var tempSharedPreferences: SharedPreferences
    lateinit var tempUnit: TempUnit

    class DayViewHolder(val binding:DayItemBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DayItemBinding.inflate(inflater,parent,false)
        tempSharedPreferences = parent.context.getSharedPreferences(Constants.TEMP_SHARED_PREFS, Context.MODE_PRIVATE)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        tempUnit = when (tempSharedPreferences.getString(Constants.TEMP_SHARED_PREFS_KEY, "kelvin")) {
            "celsius" -> TempUnit("metric", "°C")
            "fahrenheit" -> TempUnit("imperial", "°F")
            else -> TempUnit("standard", "°K")
        }

        val currentDay = getItem(position)
        holder.binding.tvDay.text = currentDay.dayOfWeek

        val maxTemp = when (tempUnit.apiParam) {
            "metric" -> (currentDay.maxTemp.toInt() - 273.15).toInt().toString()
            "imperial" -> ((currentDay.maxTemp.toInt() - 273.15) * 1.8 + 32).toInt().toString()
            else -> currentDay.maxTemp
        }
        holder.binding.maxTemp.text = maxTemp

        val minTemp = when (tempUnit.apiParam) {
            "metric" -> (currentDay.minTemp.toInt() - 273.15).toInt().toString()
            "imperial" -> ((currentDay.minTemp.toInt() - 273.15) * 1.8 + 32).toInt().toString()
            else -> currentDay.minTemp
        }
        holder.binding.minTemp.text = "$minTemp ${tempUnit.symbol}"

        holder.binding.icon.setImageResource(getWeatherIcon(currentDay.imageIcon ?: ""))
    }





    private fun getWeatherIcon(icon: String): Int {
        val iconValue: Int
        when (icon) {
            "01d" -> iconValue = R.drawable.clear_sky
            "01n" -> iconValue = R.drawable.clear_sky
            "02d" -> iconValue = R.drawable.cloudy
            "02n" -> iconValue = R.drawable.cloudy
            "03n" -> iconValue = R.drawable.cloudy
            "03d" -> iconValue = R.drawable.cloudy
            "04d" -> iconValue = R.drawable.cloudy
            "04n" -> iconValue = R.drawable.cloudy
            "09d" -> iconValue = R.drawable.rain
            "09n" -> iconValue = R.drawable.rain
            "10d" -> iconValue = R.drawable.rain
            "10n" -> iconValue = R.drawable.rain
            "11d" -> iconValue = R.drawable.storm
            "11n" -> iconValue = R.drawable.storm
            "13d" -> iconValue = R.drawable.snow
            "13n" -> iconValue = R.drawable.snow
            "50d" -> iconValue = R.drawable.mist
            "50n" -> iconValue = R.drawable.mist
            else -> iconValue = R.drawable.custom_appbar_shape
        }
        return iconValue
    }
}