package com.aacroche.polenalert.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.Place
import com.aacroche.polenalert.domain.repository.GeocodingRepository
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import java.util.Locale

class LocationSearchViewModel @Inject constructor(
    private val geocodingRepository: GeocodingRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<LocationSearchUiState> = _query
        .debounce(SEARCH_DEBOUNCE_MS)
        .map { it.trim() }
        .distinctUntilChanged()
        .transformLatest { text ->
            if (text.length < MIN_QUERY_LENGTH) {
                emit(LocationSearchUiState.Idle)
            } else {
                emit(LocationSearchUiState.Loading)
                val result = geocodingRepository.search(text, Locale.getDefault().language)
                emit(
                    when (result) {
                        is DataResult.Success ->
                            if (result.data.isEmpty()) LocationSearchUiState.NoResults
                            else LocationSearchUiState.Results(result.data)

                        is DataResult.Error -> LocationSearchUiState.Error(result.error)
                    }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocationSearchUiState.Idle)

    private val _placeSaved = MutableStateFlow(false)
    val placeSaved: StateFlow<Boolean> = _placeSaved.asStateFlow()

    fun onQueryChange(text: String) {
        _query.value = text
    }

    fun onPlaceSelected(place: Place) = save(place)

    fun onUseCurrentLocation() = save(null)

    private fun save(place: Place?) {
        viewModelScope.launch {
            preferencesRepository.setManualPlace(place)
            _placeSaved.value = true
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
        const val MIN_QUERY_LENGTH = 2
    }
}