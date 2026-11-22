package com.aacroche.polenalert.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aacroche.polenalert.data.local.MIGRATION_1_2
import com.aacroche.polenalert.data.local.PolenAlertDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PolenAlertDatabase::class.java,
    )

    @Test
    fun migrate1To2_keepsRows_andSetsFetchedAtToZero() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO pollen_daily (date, type, dailyMean) VALUES ('2026-11-22', 'GRASS', 12.0)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        db.query("SELECT dailyMean, fetchedAt FROM pollen_daily").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(12.0, cursor.getDouble(0), 0.0)
            assertEquals(0L, cursor.getLong(1))
        }
    }

    private companion object { const val TEST_DB = "migration-test" }
}