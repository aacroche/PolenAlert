package com.aacroche.polenalert.ui.forecast

import app.cash.turbine.test
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.PollenReading
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.repository.PollenRepository
import com.aacroche.polenalert.domain.usecase.GetDailyPollenUseCase
import com.aacroche.polenalert.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ForecastViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakePollenRepository
    private lateinit var getDailyPollenUseCase: GetDailyPollenUseCase

    @Before
    fun setUp() {
        fakeRepository = FakePollenRepository()
        getDailyPollenUseCase = GetDailyPollenUseCase(fakeRepository)
    }

    @Test
    fun `when repository returns required days of data then emit Loading then Success`() = runTest {
        val reading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
        val dailyData = listOf(
            DailyPollen(date = "2023-05-01", readings = listOf(reading)),
            DailyPollen(date = "2023-05-02", readings = listOf(reading)),
            DailyPollen(date = "2023-05-03", readings = listOf(reading)),
            DailyPollen(date = "2023-05-04", readings = listOf(reading))
        )
        fakeRepository.result = DataResult.Success(dailyData)

        val viewModel = ForecastViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(ForecastUiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem() as ForecastUiState.Success
            assertEquals(4, success.days.size)
            assertEquals(LocalDate.of(2023, 5, 1), success.days.first())
            assertEquals(1, success.forecasts.size)
            assertEquals(PollenType.GRASS, success.forecasts.first().type)
        }
    }

    @Test
    fun `when repository returns fewer than required days then emit Loading then Empty`() = runTest {
        val reading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
        val dailyData = listOf(
            DailyPollen(date = "2023-05-01", readings = listOf(reading)),
            DailyPollen(date = "2023-05-02", readings = listOf(reading))
        )
        fakeRepository.result = DataResult.Success(dailyData)

        val viewModel = ForecastViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(ForecastUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ForecastUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `when toForecasts returns empty list then emit Loading then Empty`() = runTest {
        val reading = PollenReading(type = PollenType.GRASS, dailyMean = 0.0, level = PollenLevel.NONE)
        val dailyData = listOf(
            DailyPollen(date = "2023-05-01", readings = listOf(reading)),
            DailyPollen(date = "2023-05-02", readings = listOf(reading)),
            DailyPollen(date = "2023-05-03", readings = listOf(reading)),
            DailyPollen(date = "2023-05-04", readings = listOf(reading))
        )
        fakeRepository.result = DataResult.Success(dailyData)

        val viewModel = ForecastViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(ForecastUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ForecastUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `when repository returns error then emit Loading then Error`() = runTest {
        fakeRepository.result = DataResult.Error(DataError.NO_CONNECTION)

        val viewModel = ForecastViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(ForecastUiState.Loading, awaitItem())
            advanceUntilIdle()
            val errorState = awaitItem() as ForecastUiState.Error
            assertEquals(DataError.NO_CONNECTION, errorState.error)
        }
    }

    @Test
    fun `when retry after error then emit Loading then Success`() = runTest {
        fakeRepository.result = DataResult.Error(DataError.NO_CONNECTION)

        val viewModel = ForecastViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(ForecastUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(ForecastUiState.Error(DataError.NO_CONNECTION), awaitItem())

            val reading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
            val dailyData = listOf(
                DailyPollen(date = "2023-05-01", readings = listOf(reading)),
                DailyPollen(date = "2023-05-02", readings = listOf(reading)),
                DailyPollen(date = "2023-05-03", readings = listOf(reading)),
                DailyPollen(date = "2023-05-04", readings = listOf(reading))
            )
            fakeRepository.result = DataResult.Success(dailyData)

            viewModel.retry()
            advanceUntilIdle()

            assertEquals(ForecastUiState.Loading, awaitItem())
            val success = awaitItem() as ForecastUiState.Success
            assertEquals(4, success.days.size)
        }
    }

    class FakePollenRepository : PollenRepository {
        var result: DataResult<List<DailyPollen>> = DataResult.Success(emptyList())

        override suspend fun getDailyPollen(latitude: Double, longitude: Double): DataResult<List<DailyPollen>> {
            return result
        }
    }
}
