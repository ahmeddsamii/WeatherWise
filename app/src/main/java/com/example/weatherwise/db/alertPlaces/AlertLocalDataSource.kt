package com.example.weatherwise.db.alertPlaces

import com.example.weatherwise.model.AlertDto

class AlertLocalDataSource(val alertDao: AlertDao) : IAlertLocalDataSource {

    override suspend fun addAlert(alertDto: AlertDto): Long {
        return alertDao.insertAlert(alertDto)
    }



    override suspend fun deleteAlert(alertDto: AlertDto): Int{
        return alertDao.deleteAlert(alertDto)

    }


    override fun getLocalAlertsByDate():List<AlertDto>{
        return alertDao.getAlertsByStartDate()
    }
}