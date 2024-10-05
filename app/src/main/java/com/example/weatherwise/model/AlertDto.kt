package com.example.weatherwise.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "AlertTable")
data class AlertDto(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    val start: Long,
)


