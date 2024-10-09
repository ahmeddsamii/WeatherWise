package com.example.weatherwise.ui.favorite.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.weatherwise.databinding.FavPlaceItemBinding
import com.example.weatherwise.diffUtils.FavoritePlaceDiffUtil
import com.example.weatherwise.model.FavoritePlace

class FavoriteAdapter(val deleteListener: OnFavoriteDeleteListener, val cardViewListener:OnCardViewClicked):ListAdapter<FavoritePlace,FavoriteAdapter.FavoriteItemViewHolder>(FavoritePlaceDiffUtil()) {
    lateinit var binding:FavPlaceItemBinding

    class FavoriteItemViewHolder(val binding:FavPlaceItemBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteItemViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = FavPlaceItemBinding.inflate(inflater,parent,false)
        return FavoriteItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.binding.countryName.text = currentItem.address
        holder.binding.delete.setOnClickListener {
            deleteListener.onClick(currentItem)
        }
        binding.cardView6.setOnClickListener {
            cardViewListener.onCardClick(currentItem)
        }
    }
}