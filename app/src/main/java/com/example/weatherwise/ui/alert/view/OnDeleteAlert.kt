package com.example.weatherwise.ui.alert.view

import com.example.weatherwise.model.AlertDto

interface OnDeleteAlert {
    fun onClick(alertDto: AlertDto)
}