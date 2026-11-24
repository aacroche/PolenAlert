package com.aacroche.polenalert.data.repository

import app.cash.turbine.test
import com.aacroche.polenalert.data.local.CachedLocation
import com.aacroche.polenalert.data.local.PollenDailyEntity
import com.aacroche.polenalert.data.local.PollenDao
import com.aacroche.polenalert.data.remote.PollenApi
import com.aacroche.polenalert.data.remote.dto.AirQualityResponseDto
import com.aacroche.polenalert.data.remote.dto.HourlyPollenDto
import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenType
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class PollenRepositoryImplTest {

    private class FakePollenApi(
        private val response: AirQualityResponseDto? = null,
        private val error: Throwable? = null,
    ) : PollenApi {
        var calls = 0
        override suspend fun getPollen(
            latitude: Double,
            longitude: Double,
            hourly: String,
            timezone: String,
            forecastDays: Int,
        ): AirQualityResponseDto {
            calls++
            delay(1_000)
            error?.let { throw it }
            return response!!
        }
    }

    private class FakePollenDao : PollenDao {
        val insertedRows = mutableListOf<PollenDailyEntity>()
        var replaceAllCalled = false

        override fun observeFrom(from: LocalDate): Flow<List<PollenDailyEntity>> {
            return flowOf(insertedRows.filter { !it.date.isBefore(from) })
        }

        override suspend fun upsertAll(rows: List<PollenDailyEntity>) {
            for (row in rows) {
                insertedRows.removeAll { it.date == row.date && it.type == row.type }
                insertedRows.add(row)
            }
        }

        override suspend fun deleteAll() {
            insertedRows.clear()
        }

        override suspend fun replaceAll(rows: List<PollenDailyEntity>) {
            replaceAllCalled = true
            deleteAll()
            upsertAll(rows)
        }

        override suspend fun oldestFetchedAt(from: LocalDate): Long? {
            return insertedRows.filter { !it.date.isBefore(from) }
                .minOfOrNull { it.fetchedAt.toEpochMilli() }
        }

        override suspend fun cachedLocation(from: LocalDate): CachedLocation? {
            return insertedRows.filter { !it.date.isBefore(from) }
                .firstOrNull()?.let { CachedLocation(it.latitude, it.longitude) }
        }
    }

    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-10-14T10:00:00Z"),
        ZoneId.of("UTC")
    )

    private val defaultCoords = Coordinates(40.42, -3.70)
    private val madrid = Coordinates(40.4168, -3.7038)
    private val sevilla = Coordinates(37.3891, -5.9845)

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

        val result = repository.refresh(defaultCoords)

        assertEquals(DataResult.Success(Unit), result)
        assertTrue(dao.replaceAllCalled)
        assertEquals(1, dao.insertedRows.size)
        assertEquals(LocalDate.parse("2026-10-14"), dao.insertedRows.first().date)
    }

    @Test
    fun `refresh with IOException returns NO_CONNECTION and DAO receives nothing`() = runTest {
        val api = FakePollenApi(error = IOException("Network error"))
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, fixedClock)

        val result = repository.refresh(defaultCoords)

        assertEquals(DataResult.Error(DataError.NO_CONNECTION), result)
        assertFalse(dao.replaceAllCalled)
        assertTrue(dao.insertedRows.isEmpty())
    }

    @Test
    fun `refresh with HttpException returns SERVER error`() = runTest {
        val httpException = HttpException(Response.error<Any>(500, "".toResponseBody()))
        val api = FakePollenApi(error = httpException)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, fixedClock)

        val result = repository.refresh(defaultCoords)

        assertEquals(DataResult.Error(DataError.SERVER), result)
        assertFalse(dao.replaceAllCalled)
    }

    private val now = Instant.parse("2026-11-22T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 11, 22)

    private fun cachedRow(fetchedAgo: Duration, coordinates: Coordinates = defaultCoords) =
        PollenDailyEntity(
            date = today,
            type = PollenType.GRASS,
            dailyMean = 10.0,
            fetchedAt = now.minus(fetchedAgo),
            latitude = coordinates.latitude,
            longitude = coordinates.longitude
        )

    private val successDto = AirQualityResponseDto(
        latitude = 40.42,
        longitude = -3.70,
        timezone = "Europe/Madrid",
        hourly = HourlyPollenDto(
            time = listOf("2026-11-22T00:00", "2026-11-22T01:00"),
            grass = listOf(20.0, 40.0)
        )
    )

    @Test
    fun `fresca y cerca - no llama`() = runTest {
        val httpException = HttpException(Response.error<Any>(500, "".toResponseBody()))
        val api = FakePollenApi(error = httpException)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, clock)
        dao.upsertAll(listOf(cachedRow(Duration.ofMinutes(10), coordinates = madrid)))

        val result = repository.refresh(madrid)

        assertEquals(DataResult.Success(Unit), result)
        assertEquals(0, api.calls)
    }

    @Test
    fun `fresca en otra ciudad - llama y guarda la nueva ubicacion`() = runTest {
        val sevillaDto = AirQualityResponseDto(
            latitude = 37.3891,
            longitude = -5.9845,
            timezone = "Europe/Madrid",
            hourly = HourlyPollenDto(
                time = listOf("2026-11-22T00:00", "2026-11-22T01:00"),
                grass = listOf(20.0, 40.0)
            )
        )
        val api = FakePollenApi(response = sevillaDto)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, clock)
        dao.upsertAll(listOf(cachedRow(Duration.ofMinutes(10), coordinates = madrid)))

        val result = repository.refresh(sevilla)

        assertEquals(DataResult.Success(Unit), result)
        assertEquals(1, api.calls)
        assertTrue(dao.insertedRows.isNotEmpty())
        assertEquals(sevilla.latitude, dao.insertedRows.first().latitude, 0.001)
        assertEquals(sevilla.longitude, dao.insertedRows.first().longitude, 0.001)
    }

    @Test
    fun `observeDailyPollen desde otra ciudad - snapshot vacio`() = runTest {
        val api = FakePollenApi(response = successDto)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, clock)
        dao.upsertAll(listOf(cachedRow(Duration.ofMinutes(10), coordinates = madrid)))

        repository.observeDailyPollen(sevilla).test {
            val snapshot = awaitItem()
            assertTrue(snapshot.days.isEmpty())
            assertNull(snapshot.fetchedAt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stale cache calls the api and saves fetchedAt`() = runTest {
        val api = FakePollenApi(response = successDto)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, clock)
        dao.upsertAll(listOf(cachedRow(Duration.ofHours(2), coordinates = defaultCoords)))

        repository.refresh(defaultCoords)

        assertEquals(1, api.calls)
        assertTrue(dao.insertedRows.all { it.fetchedAt == now })
    }

    @Test
    fun `force calls the api even with fresh cache`() = runTest {
        val api = FakePollenApi(response = successDto)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, clock)
        dao.upsertAll(listOf(cachedRow(Duration.ofMinutes(10), coordinates = defaultCoords)))

        repository.refresh(defaultCoords, force = true)

        assertEquals(1, api.calls)
    }

    @Test
    fun `concurrent refreshes only call the api once`() = runTest {
        val api = FakePollenApi(response = successDto)
        val dao = FakePollenDao()
        val repository = PollenRepositoryImpl(api, dao, clock)

        val first = async { repository.refresh(defaultCoords) }
        val second = async { repository.refresh(defaultCoords) }
        first.await()
        second.await()

        assertEquals(1, api.calls)
    }
}
