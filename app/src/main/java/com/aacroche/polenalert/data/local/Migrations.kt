package com.aacroche.polenalert.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pollen_daily ADD COLUMN fetchedAt INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pollen_daily ADD COLUMN latitude REAL NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE pollen_daily ADD COLUMN longitude REAL NOT NULL DEFAULT 0")
    }
}