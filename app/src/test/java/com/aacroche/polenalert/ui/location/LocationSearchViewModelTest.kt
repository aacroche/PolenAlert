package com.aacroche.polenalert.ui.location

import com.aacroche.polenalert.domain.model.Coordinates
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.Place
import com.aacroche.polenalert.utils.FakeGeocodingRepository
import com.aacroche.polenalert.utils.FakeUserPreferencesRepository
import com.aacroche.polenalert.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationSearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeGeocodingRepository: FakeGeocodingRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var viewModel: LocationSearchViewModel

    @Before
    fun setUp() {
        fakeGeocodingRepository = FakeGeocodingRepository()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
        viewModel = LocationSearchViewModel(
            geocodingRepository = fakeGeocodingRepository,
            preferencesRepository = fakeUserPreferencesRepository
        )
    }

    @Test
    fun `debounce - onQueryChange S, Se, Sev seguidos mas advanceTimeBy 301 resulta en 1 call y lastQuery Sev`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.results.collect {}
        }

        viewModel.onQueryChange("S")
        viewModel.onQueryChange("Se")
        viewModel.onQueryChange("Sev")

        advanceTimeBy(301)

        assertEquals(1, fakeGeocodingRepository.calls)
        assertEquals("Sev", fakeGeocodingRepository.lastQuery)
    }

    @Test
    fun `texto corto - onQueryChange S mas advanceTimeBy 301 resulta en Idle y 0 calls`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.results.collect {}
        }

        viewModel.onQueryChange("S")

        advanceTimeBy(301)

        assertEquals(LocationSearchUiState.Idle, viewModel.results.value)
        assertEquals(0, fakeGeocodingRepository.calls)
    }

    @Test
    fun `sin resultados - result Success emptyList resulta en NoResults`() = runTest {
        fakeGeocodingRepository.result = DataResult.Success(emptyList())

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.results.collect {}
        }

        viewModel.onQueryChange("Sevilla")

        advanceTimeBy(301)

        assertEquals(LocationSearchUiState.NoResults, viewModel.results.value)
    }

    @Test
    fun `elegir ciudad - onPlaceSelected sevilla guarda en repository y placeSaved es true`() = runTest {
        val sevilla = Place(
            name = "Sevilla",
            region = "Andalucía",
            country = "España",
            coordinates = Coordinates(37.3891, -5.9845)
        )

        viewModel.onPlaceSelected(sevilla)
        advanceUntilIdle()

        assertEquals(sevilla, fakeUserPreferencesRepository.preferences.first().manualPlace)
        assertTrue(viewModel.placeSaved.value)
    }

    @Test
    fun `usar mi ubicacion - onUseCurrentLocation pone manualPlace a null y placeSaved a true`() = runTest {
        val initialPlace = Place(
            name = "Madrid",
            region = "Madrid",
            country = "España",
            coordinates = Coordinates(40.4168, -3.7038)
        )
        fakeUserPreferencesRepository.setManualPlace(initialPlace)

        viewModel.onUseCurrentLocation()
        advanceUntilIdle()

        assertNull(fakeUserPreferencesRepository.preferences.first().manualPlace)
        assertTrue(viewModel.placeSaved.value)
    }
}
