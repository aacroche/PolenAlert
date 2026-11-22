package com.aacroche.polenalert.domain.model

import java.time.Instant

data class PollenSnapshot(
    val days: List<DailyPollen>,
    val fetchedAt: Instant?
)
