package com.aacroche.polenalert.domain.repository

import com.aacroche.polenalert.domain.model.DataResult
import com.aacroche.polenalert.domain.model.Place

interface GeocodingRepository {
    suspend fun search(query: String, language: String): DataResult<List<Place>>
}