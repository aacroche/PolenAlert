package com.aacroche.polenalert.domain.model

data class UserPreferences(
    val name: String="",
    val allergies: Set<PollenType> = emptySet(),
)
