package com.aacroche.polenalert.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacroche.polenalert.domain.model.PollenType
import com.aacroche.polenalert.domain.model.UserPreferences
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<UserPreferences> = repository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    fun onNameSaved(name: String) = viewModelScope.launch { repository.setName(name) }
    fun onAllergyToggled(type: PollenType, enabled: Boolean) =
        viewModelScope.launch { repository.setAllergy(type, enabled) }
}