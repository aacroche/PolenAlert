package com.aacroche.polenalert.domain.model

import app.cash.turbine.test
import com.aacroche.polenalert.domain.usecase.GetDailyPollenUseCase
import com.aacroche.polenalert.ui.today.TodayUiState
import com.aacroche.polenalert.ui.today.TodayViewModel
import com.aacroche.polenalert.ui.today.TodayViewModelTest.FakePollenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class DailyPollenTest {
    private lateinit var fakeRepository: FakePollenRepository
    private lateinit var getDailyPollenUseCase: GetDailyPollenUseCase
    private val clock = Clock.fixed(
        Instant.parse("2026-10-27T07:30:00Z"),
        ZoneId.of("Europe/Madrid")      // 08:30 en Madrid
    )

    @Before
    fun setUp() {
        fakeRepository = FakePollenRepository()
        getDailyPollenUseCase = GetDailyPollenUseCase(fakeRepository)
    }

    @Test
    fun `when there is more than one pollen reading then emit Success with dominant reading`() = runTest {
        val oliveReading = PollenReading(type = PollenType.OLIVE, dailyMean = 53.0, level = PollenLevel.MEDIUM)
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 23.0, level = PollenLevel.LOW)
        val today = DailyPollen(date = "2023-05-01", readings = listOf(oliveReading, grassReading))
        fakeRepository.result = DataResult.Success(listOf(today))

        val viewModel = TodayViewModel(getDailyPollenUseCase, clock)

        viewModel.uiState.test {
            advanceUntilIdle()
            val success = awaitItem() as TodayUiState.Success
            assertEquals(PollenType.OLIVE, success.dominant.type)
        }
    }
    @Test
    fun `when there is more than one pollen with same level reading then emit Success with bigger dailyMean`() = runTest {
        val mugwortReading = PollenReading(type = PollenType.MUGWORT, dailyMean = 5.0, level = PollenLevel.LOW)
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 20.0, level = PollenLevel.LOW)
        val today = DailyPollen(date = "2023-05-01", readings = listOf(mugwortReading, grassReading))
        fakeRepository.result = DataResult.Success(listOf(today))

        val viewModel = TodayViewModel(getDailyPollenUseCase, clock)

        viewModel.uiState.test {
            assertEquals(TodayUiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem() as TodayUiState.Success
            assertEquals(PollenType.GRASS, success.dominant.type)
        }
    }
    @Test
    fun `when reading list is empty then emit Success with NONE reading`() = runTest {
        val today = DailyPollen(date = "2023-05-01", readings = emptyList())
        fakeRepository.result = DataResult.Success(listOf(today))

        val viewModel = TodayViewModel(getDailyPollenUseCase, clock)

        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(TodayUiState.OutOfSeason, awaitItem())
        }
    }
}