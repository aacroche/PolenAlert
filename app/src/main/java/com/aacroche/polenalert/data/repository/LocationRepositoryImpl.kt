package com.aacroche.polenalert.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.aacroche.polenalert.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class LocationRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    LocationRepository {
    override fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}