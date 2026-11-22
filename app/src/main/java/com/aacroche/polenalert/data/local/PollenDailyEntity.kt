package com.aacroche.polenalert.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.aacroche.polenalert.domain.model.PollenType
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "pollen_daily", primaryKeys = ["date", "type"])
data class PollenDailyEntity(
    val date: LocalDate,
    val type: PollenType,
    val dailyMean: Double,
    @ColumnInfo(defaultValue = "0") val fetchedAt: Instant,
)