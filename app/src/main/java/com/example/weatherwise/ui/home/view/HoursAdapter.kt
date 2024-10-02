package com.example.weatherwise.ui.home.view

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weatherwise.Constants
import com.example.weatherwise.R
import com.example.weatherwise.databinding.HourItemBinding
import com.example.weatherwise.diffUtils.WeatherForecastDiffUtils
import com.example.weatherwise.model.ListElement
import com.example.weatherwise.model.TempUnit

class HoursAdapter: ListAdapter<ListElement, HoursAdapter.HourViewHolder>(WeatherForecastDiffUtils()){
    lateinit var binding: HourItemBinding
    lateinit var tempUnit: TempUnit
    private lateinit var tempSharedPreferences: SharedPreferences

    class HourViewHolder(var binding: HourItemBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tempSharedPreferences = parent.context.getSharedPreferences(Constants.TEMP_SHARED_PREFS, Context.MODE_PRIVATE)
        binding = HourItemBinding.inflate(inflater,parent,false)
        return HourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        tempUnit = when (tempSharedPreferences.getString(Constants.TEMP_SHARED_PREFS_KEY, "kelvin")) {
            "celsius" -> TempUnit("metric", "°C")
            "fahrenheit" -> TempUnit("imperial", "°F")
            else -> TempUnit("standard", "°K")
        }


        val currentItem = getItem(position)
        holder.binding.hour.text = currentItem.dtTxt

        val temp = when (tempUnit.apiParam) {
            "metric" -> (currentItem.main.temp.toInt() - 273.15).toInt().toString()
            "imperial" -> ((currentItem.main.temp.toInt() - 273.15) * 1.8 + 32).toInt().toString()
            else -> currentItem.main.temp.toInt()
        }
        holder.binding.temp.text = "$temp ${tempUnit.symbol}"
        currentItem.weather.forEach {
            getWeatherIcon(it.icon)
            holder.binding.icon.setImageResource(getWeatherIcon(it.icon))
        }

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