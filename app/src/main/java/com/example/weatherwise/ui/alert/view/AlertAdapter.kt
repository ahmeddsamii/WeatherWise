package com.example.weatherwise.ui.alert.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weatherwise.databinding.AlertItemBinding
import com.example.weatherwise.diffUtils.AlertDiffUtil
import com.example.weatherwise.model.AlertDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertAdapter(val listener:OnDeleteAlert):ListAdapter<AlertDto, AlertAdapter.AlertViewHolder>(AlertDiffUtil()) {
    private lateinit var binding: AlertItemBinding

    class AlertViewHolder(val binding:AlertItemBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AlertItemBinding.inflate(layoutInflater,parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val currentItem = getItem(position)
        val date = convertTimestampToDate(currentItem.start)
        holder.binding.startDay.text = date
        holder.binding.deleteButton.setOnClickListener {
            listener.onClick(currentItem)
        }
    }



    private fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

}