package com.aacroche.polenalert.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PollenDao {
    @Query("SELECT * FROM pollen_daily WHERE date >= :from ORDER BY date, type")
    fun observeFrom(from: LocalDate): Flow<List<PollenDailyEntity>>

    @Upsert
    suspend fun upsertAll(rows: List<PollenDailyEntity>)

    @Query("DELETE FROM pollen_daily")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(rows: List<PollenDailyEntity>) {
        deleteAll()
        upsertAll(rows)
    }

    @Query("SELECT MIN(fetchedAt) FROM pollen_daily WHERE date >= :from")
    suspend fun oldestFetchedAt(from: LocalDate): Long?

    @Query("SELECT latitude, longitude FROM pollen_daily WHERE date >= :from LIMIT 1")
    suspend fun cachedLocation(from: LocalDate): CachedLocation?
}