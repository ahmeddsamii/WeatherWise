package com.example.weatherwise.db.alertPlaces

import com.example.weatherwise.model.AlertDto

interface IAlertLocalDataSource {
    suspend fun addAlert(alertDto: AlertDto): Long

    suspend fun deleteAlert(alertDto: AlertDto): Int
    fun getLocalAlertsByDate(): List<AlertDto>
}