package com.aacroche.polenalert.ui.location

import com.aacroche.polenalert.domain.model.DataError
import com.aacroche.polenalert.domain.model.Place


sealed interface LocationSearchUiState {
    data object Idle : LocationSearchUiState
    data object Loading : LocationSearchUiState
    data class Results(val places: List<Place>) : LocationSearchUiState
    data object NoResults : LocationSearchUiState
    data class Error(val error: DataError) : LocationSearchUiState
}