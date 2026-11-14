package com.aacroche.polenalert.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromDate(date: LocalDate): String = date.toString()        // "2026-11-14"
    @TypeConverter fun toDate(value: String): LocalDate = LocalDate.parse(value)
}