package com.aacroche.polenalert.di

import com.aacroche.polenalert.data.preferences.UserPreferencesRepositoryImpl
import com.aacroche.polenalert.data.repository.PollenRepositoryImpl
import com.aacroche.polenalert.domain.repository.PollenRepository
import com.aacroche.polenalert.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPollenRepository(pollenRepository: PollenRepositoryImpl): PollenRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(userPreferencesRepository: UserPreferencesRepositoryImpl): UserPreferencesRepository
}