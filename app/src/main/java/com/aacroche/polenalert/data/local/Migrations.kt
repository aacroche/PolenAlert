package com.aacroche.polenalert.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pollen_daily ADD COLUMN fetchedAt INTEGER NOT NULL DEFAULT 0")
    }
}