package com.example.weatherwise.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.example.weatherwise.model.FavoritePlace

class FavoritePlaceDiffUtil: DiffUtil.ItemCallback<FavoritePlace>() {
    override fun areItemsTheSame(oldItem: FavoritePlace, newItem: FavoritePlace): Boolean {
        return oldItem.latitude == newItem.longitude
    }

    override fun areContentsTheSame(oldItem: FavoritePlace, newItem: FavoritePlace): Boolean {
        return oldItem == newItem
    }
}