package com.aacroche.polenalert.data.repository

import com.aacroche.polenalert.data.local.PollenDailyEntity
import com.aacroche.polenalert.data.local.PollenDao
import com.aacroche.polenalert.data.remote.PollenApi
import com.aacroche.polenalert.data.remote.dto.AirQualityResponseDto
import com.aacroche.polenalert.data.remote.dto.HourlyPollenDto
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PollenRepositoryImplTest {

    private class FakePollenApi(
        private val response: AirQualityResponseDto? = null,
        private val error: Throwable? = null,
    ) : PollenApi {
        override suspend fun getPollen(
            latitude: Double,
            longitude: Double,
            hourly: String,
            timezone: String,
            forecastDays: Int,
        ): AirQualityResponseDto {
            error?.let { throw it }
            return response!!
        }
    }

    private class FakePollenDao : PollenDao {
        val insertedRows = mutableListOf<PollenDailyEntity>()
        var replaceFromCalled = false

        override fun observeFrom(from: LocalDate): Flow<List<PollenDailyEntity>> {
            return flowOf(insertedRows.filter { !it.date.isBefore(from) })
        }

        override suspend fun upsertAll(rows: List<PollenDailyEntity>) {
            insertedRows.addAll(rows)
        }

        override suspend fun deleteOlderThan(before: LocalDate) {
            insertedRows.removeAll { it.date.isBefore(before) }
        }

        override suspend fun replaceFrom(today: LocalDate, rows: List<PollenDailyEntity>) {
            replaceFromCalled = true
            deleteOlderThan(today)
            upsertAll(rows)
        }

        override suspend fun oldestFetchedAt(from: LocalDate): Long? {
            return insertedRows.filter { !it.date.isBefore(from) }.minOfOrNull { it.fetchedAt.toEpochMilli() }
        }
    }

    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-10-14T10:00:00Z"),
        ZoneId.of("UTC")
    )

    @Test
    fun `refresh with success saves rows to DAO`() = runTest {
        val dto = AirQualityResponseDto(
            latitude = 40.42,
            longitude = -3.70,
            timezone = "Europe/Madrid",
            hourly = HourlyPollenDto(
                time = listOf("2026-10-14T00:00", "2026-10-14T01:00"),
                grass = listOf(20.0, 40.0)
            )
        )
        val api = FakePollenApi(response = dto)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, fixedClock)

        val result = repository.refresh(40.42, -3.70)

        assertEquals(DataResult.Success(Unit), result)
        assertTrue(dao.replaceFromCalled)
        assertEquals(1, dao.insertedRows.size)
        assertEquals(LocalDate.parse("2026-10-14"), dao.insertedRows.first().date)
    }

    @Test
    fun `refresh with IOException returns NO_CONNECTION and DAO receives nothing`() = runTest {
        val api = FakePollenApi(error = IOException("Network error"))
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, fixedClock)

        val result = repository.refresh(40.42, -3.70)

        assertEquals(DataResult.Error(DataError.NO_CONNECTION), result)
        assertFalse(dao.replaceFromCalled)
        assertTrue(dao.insertedRows.isEmpty())
    }

    @Test
    fun `refresh with HttpException returns SERVER error`() = runTest {
        val httpException = HttpException(Response.error<Any>(500, "".toResponseBody()))
        val api = FakePollenApi(error = httpException)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, fixedClock)

        val result = repository.refresh(40.42, -3.70)

        assertEquals(DataResult.Error(DataError.SERVER), result)
        assertFalse(dao.replaceFromCalled)
    }
}
