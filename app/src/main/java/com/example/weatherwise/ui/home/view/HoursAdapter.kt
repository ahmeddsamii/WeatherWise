package com.example.weatherwise.ui.home.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weatherwise.databinding.HourItemBinding
import com.example.weatherwise.diffUtils.WeatherForecastDiffUtils
import com.example.weatherwise.model.ListElement

class HoursAdapter: ListAdapter<ListElement, HoursAdapter.HourViewHolder>(WeatherForecastDiffUtils()){
    lateinit var binding: HourItemBinding

    class HourViewHolder(var binding: HourItemBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = HourItemBinding.inflate(inflater,parent,false)
        return HourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.binding.hour.text = currentItem.dtTxt
        holder.binding.temp.text = currentItem.main.temp.toInt().toString()
    }
}