package com.aacroche.polenalert.ui.profile

import app.cash.turbine.test
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import com.aacroche.polenalert.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

class FakeUserPreferencesRepository : UserPreferencesRepository {
    private val _preferences = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = _preferences

    var lastSavedName: String? = null

    override suspend fun setName(name: String) {
        lastSavedName = name
        _preferences.value = _preferences.value.copy(name = name)
    }

    override suspend fun setAllergy(type: PollenType, enabled: Boolean) {
        val currentAllergies = _preferences.value.allergies.toMutableSet()
        if (enabled) {
            currentAllergies.add(type)
        } else {
            currentAllergies.remove(type)
        }
        _preferences.value = _preferences.value.copy(allergies = currentAllergies)
    }
}
