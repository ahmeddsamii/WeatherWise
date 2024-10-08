package com.example.weatherwise.db.alertPlaces

import com.example.weatherwise.model.AlertDto

class FakeAlertLocalDataSource(private var localDataBaseList:MutableList<AlertDto> = mutableListOf()):IAlertLocalDataSource {
    override suspend fun addAlert(alertDto: AlertDto): Long {
        return if (localDataBaseList.add(alertDto)){
            1
        }else{
            0
        }
    }

    override suspend fun deleteAlert(alertDto: AlertDto): Int {
        return if (localDataBaseList.remove(alertDto)){
            1
        }else{
            0
        }
    }

    override fun getLocalAlertsByDate(): List<AlertDto> {
        return localDataBaseList
    }
}