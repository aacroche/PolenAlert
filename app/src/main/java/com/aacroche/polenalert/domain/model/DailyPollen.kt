package com.aacroche.polenalert.domain.model

data class DailyPollen(val date: String, val readings: List<PollenReading>)
