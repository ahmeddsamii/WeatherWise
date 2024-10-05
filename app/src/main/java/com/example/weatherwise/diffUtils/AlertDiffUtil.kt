package com.example.weatherwise.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.example.weatherwise.model.AlertDto

class AlertDiffUtil:DiffUtil.ItemCallback<AlertDto>() {
    override fun areItemsTheSame(oldItem: AlertDto, newItem: AlertDto): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AlertDto, newItem: AlertDto): Boolean {
        return oldItem==newItem
    }
}