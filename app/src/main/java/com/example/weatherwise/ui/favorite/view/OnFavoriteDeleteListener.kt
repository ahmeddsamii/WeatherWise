package com.example.weatherwise.ui.favorite.view

import com.example.weatherwise.model.FavoritePlace

interface OnFavoriteDeleteListener {
    fun onClick(favoritePlace: FavoritePlace)
}