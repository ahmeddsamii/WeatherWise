package com.example.weatherwise.ui.favorite.view

import com.example.weatherwise.model.FavoritePlace

interface OnCardViewClicked {
    fun onCardClick(favoritePlace: FavoritePlace)
}