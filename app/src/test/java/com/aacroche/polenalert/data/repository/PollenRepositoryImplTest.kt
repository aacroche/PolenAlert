package com.aacroche.polenalert.data.repository

import com.aacroche.polenalert.data.remote.dto.AirQualityResponseDto
import com.aacroche.polenalert.data.remote.dto.HourlyPollenDto
import com.aacroche.polenalert.data.remote.PollenApi
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

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

    @Test
    fun `getDailyPollen returns Success with mapped days when API succeeds`() = runTest {
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
        val repository = PollenRepositoryImpl(api)

        val result = repository.getDailyPollen(40.42, -3.70)

        assertTrue(result is DataResult.Success)
        val (data) = result as DataResult.Success
        assertEquals(1, data.size)
        assertEquals("2026-10-14", data.first().date)
    }

    @Test
    fun `getDailyPollen returns Error NO_CONNECTION when API throws IOException`() = runTest {
        val api = FakePollenApi(error = IOException("Network error"))
        val repository = PollenRepositoryImpl(api)

        val result = repository.getDailyPollen(40.42, -3.70)

        assertEquals(DataResult.Error(DataError.NO_CONNECTION), result)
    }

    @Test
    fun `getDailyPollen returns Error SERVER when API throws HttpException`() = runTest {
        val httpException = HttpException(Response.error<Any>(500, "".toResponseBody()))
        val api = FakePollenApi(error = httpException)
        val repository = PollenRepositoryImpl(api)

        val result = repository.getDailyPollen(40.42, -3.70)

        assertEquals(DataResult.Error(DataError.SERVER), result)
    }
}
