package com.aacroche.polenalert.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.aacroche.polenalert.domain.model.PollenType
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PollenDaoTest {

    private lateinit var db: PolenAlertDatabase
    private lateinit var dao: PollenDao

    private val today = LocalDate.of(2026, 11, 14)
    private fun row(date: LocalDate, type: PollenType = PollenType.GRASS, mean: Double = 10.0) =
        PollenDailyEntity(date, type, mean)

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PolenAlertDatabase::class.java,
        ).build()
        dao = db.pollenDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun upsertAll_sameKey_replacesInsteadOfDuplicating() = runTest {
        dao.upsertAll(listOf(row(today, mean = 5.0)))
        dao.upsertAll(listOf(row(today, mean = 40.0)))

        assertEquals(listOf(row(today, mean = 40.0)), dao.observeFrom(today).first())
    }

    @Test
    fun observeFrom_excludesPastDays_andOrdersByDate() = runTest {
        dao.upsertAll(listOf(row(today.plusDays(1)), row(today.minusDays(1)), row(today)))

        val result = dao.observeFrom(today).first()

        assertEquals(listOf(today, today.plusDays(1)), result.map { it.date })
    }

    @Test
    fun replaceFrom_deletesOldDays_andUpsertsNewOnes() = runTest {
        dao.upsertAll(listOf(row(today.minusDays(1)), row(today, mean = 5.0)))

        dao.replaceFrom(today, listOf(row(today, mean = 30.0), row(today.plusDays(1))))

        val all = dao.observeFrom(today.minusYears(1)).first()
        assertEquals(listOf(row(today, mean = 30.0), row(today.plusDays(1))), all)
    }

    @Test
    fun observeFrom_reEmitsWhenTableChanges() = runTest {
        dao.observeFrom(today).test {
            assertEquals(emptyList<PollenDailyEntity>(), awaitItem())

            dao.upsertAll(listOf(row(today)))

            assertEquals(listOf(row(today)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}