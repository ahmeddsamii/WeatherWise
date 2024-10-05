package com.example.weatherwise.db.alertPlaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.weatherwise.model.AlertDto
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Insert
    suspend fun insertAlert(alertDto: AlertDto):Long

    @Delete
    suspend fun deleteAlert(alertDto: AlertDto):Int

    @Query("SELECT * FROM AlertTable ")
    fun getAlertsByStartDate(): Flow<List<AlertDto>>
}