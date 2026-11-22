package com.aacroche.polenalert.di

import android.content.Context
import androidx.room.Room
import com.aacroche.polenalert.data.local.MIGRATION_1_2
import com.aacroche.polenalert.data.local.PolenAlertDatabase
import com.aacroche.polenalert.data.local.PollenDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PolenAlertDatabase =
        Room.databaseBuilder(context, PolenAlertDatabase::class.java, "polenalert.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun providePollenDao(db: PolenAlertDatabase): PollenDao = db.pollenDao()

}