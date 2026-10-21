package com.aacroche.polenalert.ui.today

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

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
    fun `when repository returns data then emit Loading then Success`() = runTest {
        val reading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
        val today = DailyPollen(date = "2023-05-01", readings = listOf(reading))
        val nextDay = DailyPollen(date = "2023-05-02", readings = listOf(reading))
        fakeRepository.result = DataResult.Success(listOf(today, nextDay))

        val viewModel = TodayViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(TodayUiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem() as TodayUiState.Success
            assertEquals(today, success.today)
            assertEquals(listOf(nextDay), success.nextDays)
        }
    }

    @Test
    fun `when repository returns empty list then emit Loading then OutOfSeason`() = runTest {
        fakeRepository.result = DataResult.Success(emptyList())

        val viewModel = TodayViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(TodayUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(TodayUiState.OutOfSeason, awaitItem())
        }
    }

    @Test
    fun `when repository returns error then emit Loading then Error`() = runTest {
        fakeRepository.result = DataResult.Error(DataError.NO_CONNECTION)

        val viewModel = TodayViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(TodayUiState.Loading, awaitItem())
            advanceUntilIdle()
            val errorState = awaitItem() as TodayUiState.Error
            assertEquals(DataError.NO_CONNECTION, errorState.error)
        }
    }

    @Test
    fun `when retry after error then emit Loading then Success`() = runTest {
        fakeRepository.result = DataResult.Error(DataError.NO_CONNECTION)

        val viewModel = TodayViewModel(getDailyPollenUseCase)

        viewModel.uiState.test {
            assertEquals(TodayUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(TodayUiState.Error(DataError.NO_CONNECTION), awaitItem())

            val reading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
            val today = DailyPollen(date = "2023-05-01", readings = listOf(reading))
            fakeRepository.result = DataResult.Success(listOf(today))

            viewModel.retry()
            advanceUntilIdle()

            assertEquals(TodayUiState.Loading, awaitItem())
            val success = awaitItem() as TodayUiState.Success
            assertEquals(today, success.today)
        }
    }


    class FakePollenRepository : PollenRepository {
        var result: DataResult<List<DailyPollen>> = DataResult.Success(emptyList())

        override suspend fun getDailyPollen(latitude: Double, longitude: Double): DataResult<List<DailyPollen>> {
            return result
        }
    }
}
