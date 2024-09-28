package com.example.weatherwise.ui.home.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weatherwise.R
import com.example.weatherwise.databinding.DayItemBinding
import com.example.weatherwise.diffUtils.DailyWeatherDiffUtil
import com.example.weatherwise.model.DailyWeather

class DaysAdapter: ListAdapter<DailyWeather, DaysAdapter.DayViewHolder>(DailyWeatherDiffUtil()) {
    lateinit var binding:DayItemBinding

    class DayViewHolder(val binding:DayItemBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DayItemBinding.inflate(inflater,parent,false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val currentDay = getItem(position)
        holder.binding.tvDay.text = currentDay.dayOfWeek
        holder.binding.maxTemp.text = currentDay.maxTemp
        holder.binding.minTemp.text = currentDay.minTemp+"°K"
    }
}