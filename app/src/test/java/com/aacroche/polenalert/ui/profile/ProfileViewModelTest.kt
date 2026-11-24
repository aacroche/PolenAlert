package com.aacroche.polenalert.ui.profile

import app.cash.turbine.test
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import com.aacroche.polenalert.utils.FakeUserPreferencesRepository
import com.aacroche.polenalert.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeUserPreferencesRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeUserPreferencesRepository()
        viewModel = ProfileViewModel(fakeRepository)
    }

    @Test
    fun `initial state emits default user preferences`() = runTest {
        viewModel.uiState.test {
            assertEquals(UserPreferences(), awaitItem())
        }
    }

    @Test
    fun `onNameSaved calls repository setName and updates state`() = runTest {
        viewModel.uiState.test {
            assertEquals(UserPreferences(), awaitItem())

            viewModel.onNameSaved("Alice")
            advanceUntilIdle()

            assertEquals("Alice", fakeRepository.lastSavedName)
            assertEquals(UserPreferences(name = "Alice"), awaitItem())
        }
    }

    @Test
    fun `onAllergyToggled enables and disables allergy correctly`() = runTest {
        viewModel.uiState.test {
            assertEquals(UserPreferences(), awaitItem())

            viewModel.onAllergyToggled(PollenType.GRASS, true)
            advanceUntilIdle()
            assertEquals(setOf(PollenType.GRASS), awaitItem().allergies)

            viewModel.onAllergyToggled(PollenType.GRASS, false)
            advanceUntilIdle()
            assertEquals(emptySet<PollenType>(), awaitItem().allergies)
        }
    }
}

