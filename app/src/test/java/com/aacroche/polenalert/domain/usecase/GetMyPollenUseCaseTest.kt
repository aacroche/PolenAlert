package com.aacroche.polenalert.domain.usecase

import app.cash.turbine.test
import com.aacroche.polenalert.domain.model.DailyPollen
import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.PollenLevel
import com.aacroche.polenalert.domain.model.PollenReading
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import com.aacroche.polenalert.utils.FakePollenRepository
import com.aacroche.polenalert.utils.FakeUserPreferencesRepository
import com.aacroche.polenalert.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetMyPollenUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakePollenRepository: FakePollenRepository
    private lateinit var fakePreferencesRepository: FakeUserPreferencesRepository
    private lateinit var getMyPollenUseCase: GetMyPollenUseCase

    @Before
    fun setUp() {
        fakePollenRepository = FakePollenRepository()
        fakePreferencesRepository = FakeUserPreferencesRepository()
        getMyPollenUseCase = GetMyPollenUseCase(fakePollenRepository, fakePreferencesRepository)
    }

    @Test
    fun `when no allergies selected then emit NoAllergiesSelected`() = runTest {
        val reading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
        val dailyPollen = DailyPollen(date = "2026-10-27", readings = listOf(reading))
        fakePollenRepository.result = DataResult.Success(listOf(dailyPollen))
        fakePreferencesRepository.setPreferences(UserPreferences(name = "Test", allergies = emptySet()))

        getMyPollenUseCase(40.42, -3.70).test {
            val item = awaitItem() as MyPollenResult.NoAllergiesSelected
            assertEquals("Test", item.name)
        }
    }

    @Test
    fun `when olive selected then filter days to only contain olive readings`() = runTest {
        val oliveReading = PollenReading(type = PollenType.OLIVE, dailyMean = 30.0, level = PollenLevel.MEDIUM)
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
        val dailyPollen = DailyPollen(date = "2026-10-27", readings = listOf(oliveReading, grassReading))
        fakePollenRepository.result = DataResult.Success(listOf(dailyPollen))
        fakePreferencesRepository.setPreferences(UserPreferences(name = "Test", allergies = setOf(PollenType.OLIVE)))

        getMyPollenUseCase(40.42, -3.70).test {
            val item = awaitItem() as MyPollenResult.Data
            assertEquals("Test", item.name)
            assertEquals(1, item.days.first().readings.size)
            assertEquals(PollenType.OLIVE, item.days.first().readings.first().type)
        }
    }

    @Test
    fun `when repository returns error then emit Error`() = runTest {
        fakePollenRepository.result = DataResult.Error(DataError.NO_CONNECTION)
        fakePreferencesRepository.setPreferences(UserPreferences(name = "Test", allergies = setOf(PollenType.OLIVE)))

        getMyPollenUseCase(40.42, -3.70).test {
            val item = awaitItem() as MyPollenResult.Error
            assertEquals("Test", item.name)
            assertEquals(DataError.NO_CONNECTION, item.error)
        }
    }

    @Test
    fun `when preferences change then emit new result and api called only once`() = runTest {
        val oliveReading = PollenReading(type = PollenType.OLIVE, dailyMean = 30.0, level = PollenLevel.MEDIUM)
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
        val dailyPollen = DailyPollen(date = "2026-10-27", readings = listOf(oliveReading, grassReading))
        fakePollenRepository.result = DataResult.Success(listOf(dailyPollen))
        fakePreferencesRepository.setPreferences(UserPreferences(name = "Test", allergies = emptySet()))

        getMyPollenUseCase(40.42, -3.70).test {
            // Initial emit: No allergies
            val firstItem = awaitItem() as MyPollenResult.NoAllergiesSelected
            assertEquals("Test", firstItem.name)

            // Change preferences to include OLIVE
            fakePreferencesRepository.setPreferences(UserPreferences(name = "Test", allergies = setOf(PollenType.OLIVE)))

            // Second emit: Filtered data with OLIVE only
            val secondItem = awaitItem() as MyPollenResult.Data
            assertEquals(1, secondItem.days.first().readings.size)
            assertEquals(PollenType.OLIVE, secondItem.days.first().readings.first().type)

            // Verify API was called exactly once
            assertEquals(1, fakePollenRepository.calls)
        }
    }

    @Test
    fun `when multiple allergies selected then filter days to contain matching readings`() = runTest {
        val oliveReading = PollenReading(type = PollenType.OLIVE, dailyMean = 30.0, level = PollenLevel.MEDIUM)
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
        val mugwortReading = PollenReading(type = PollenType.MUGWORT, dailyMean = 5.0, level = PollenLevel.LOW)
        val dailyPollen = DailyPollen(date = "2026-10-27", readings = listOf(oliveReading, grassReading, mugwortReading))
        fakePollenRepository.result = DataResult.Success(listOf(dailyPollen))
        fakePreferencesRepository.setPreferences(
            UserPreferences(name = "Test", allergies = setOf(PollenType.OLIVE, PollenType.GRASS))
        )

        getMyPollenUseCase(40.42, -3.70).test {
            val item = awaitItem() as MyPollenResult.Data
            assertEquals("Test", item.name)
            val filteredTypes = item.days.first().readings.map { it.type }
            assertEquals(listOf(PollenType.OLIVE, PollenType.GRASS), filteredTypes)
        }
    }

    @Test
    fun `when selected allergy is not in readings then return empty readings for day`() = runTest {
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)
        val dailyPollen = DailyPollen(date = "2026-10-27", readings = listOf(grassReading))
        fakePollenRepository.result = DataResult.Success(listOf(dailyPollen))
        fakePreferencesRepository.setPreferences(UserPreferences(name = "Test", allergies = setOf(PollenType.OLIVE)))

        getMyPollenUseCase(40.42, -3.70).test {
            val item = awaitItem() as MyPollenResult.Data
            assertEquals("Test", item.name)
            assertEquals(0, item.days.first().readings.size)
        }
    }

    @Test
    fun `when user updates name then emit updated name in result`() = runTest {
        val oliveReading = PollenReading(type = PollenType.OLIVE, dailyMean = 30.0, level = PollenLevel.MEDIUM)
        val dailyPollen = DailyPollen(date = "2026-10-27", readings = listOf(oliveReading))
        fakePollenRepository.result = DataResult.Success(listOf(dailyPollen))
        fakePreferencesRepository.setPreferences(UserPreferences(name = "Alice", allergies = setOf(PollenType.OLIVE)))

        getMyPollenUseCase(40.42, -3.70).test {
            val firstItem = awaitItem() as MyPollenResult.Data
            assertEquals("Alice", firstItem.name)

            fakePreferencesRepository.setName("Bob")

            val secondItem = awaitItem() as MyPollenResult.Data
            assertEquals("Bob", secondItem.name)
        }
    }

    @Test
    fun `when 4 days of pollen data returned then filter readings for all 4 days`() = runTest {
        val oliveReading = PollenReading(type = PollenType.OLIVE, dailyMean = 30.0, level = PollenLevel.MEDIUM)
        val grassReading = PollenReading(type = PollenType.GRASS, dailyMean = 15.0, level = PollenLevel.LOW)

        val dailyData = listOf(
            DailyPollen(date = "2026-10-27", readings = listOf(oliveReading, grassReading)),
            DailyPollen(date = "2026-10-28", readings = listOf(oliveReading, grassReading)),
            DailyPollen(date = "2026-10-29", readings = listOf(oliveReading, grassReading)),
            DailyPollen(date = "2026-10-30", readings = listOf(oliveReading, grassReading))
        )
        fakePollenRepository.result = DataResult.Success(dailyData)
        fakePreferencesRepository.setPreferences(UserPreferences(name = "Test", allergies = setOf(PollenType.OLIVE)))

        getMyPollenUseCase(40.42, -3.70).test {
            val item = awaitItem() as MyPollenResult.Data
            assertEquals(4, item.days.size)
            item.days.forEach { day ->
                assertEquals(1, day.readings.size)
                assertEquals(PollenType.OLIVE, day.readings.first().type)
            }
        }
    }
}
