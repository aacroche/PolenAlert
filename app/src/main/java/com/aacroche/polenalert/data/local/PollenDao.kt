package com.aacroche.polenalert.data.local

import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import java.time.LocalDate

@Dao
interface PollenDao {
    @Query("SELECT * FROM pollen_daily WHERE date >= :from ORDER BY date, type")
    fun observeFrom(from: LocalDate): Flow<List<PollenDailyEntity>>

    @Upsert
    suspend fun upsertAll(rows: List<PollenDailyEntity>)

    @Query("DELETE FROM pollen_daily WHERE date < :before")
    suspend fun deleteOlderThan(before: LocalDate)

    @Transaction
    suspend fun replaceFrom(today: LocalDate, rows: List<PollenDailyEntity>) {
        deleteOlderThan(today)
        upsertAll(rows)
    }
}