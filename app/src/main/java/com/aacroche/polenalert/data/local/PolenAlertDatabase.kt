package com.aacroche.polenalert.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PollenDailyEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class PolenAlertDatabase : RoomDatabase() {
    abstract fun pollenDao(): PollenDao
}