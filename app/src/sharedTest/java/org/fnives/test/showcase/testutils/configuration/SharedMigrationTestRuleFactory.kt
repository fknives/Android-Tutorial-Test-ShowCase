package org.fnives.test.showcase.testutils.configuration

import android.app.Instrumentation
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteOpenHelper

interface SharedMigrationTestRuleFactory {

    fun createSharedMigrationTestRule(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>,
    ): SharedMigrationTestRule

    fun createSharedMigrationTestRule(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>,
        specs: List<AutoMigrationSpec>
    ): SharedMigrationTestRule

    fun createSharedMigrationTestRule(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>,
        specs: List<AutoMigrationSpec>,
        openFactory: SupportSQLiteOpenHelper.Factory
    ): SharedMigrationTestRule
}
