package org.fnives.test.showcase.testutils.configuration

import android.app.Instrumentation
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteOpenHelper

object AndroidMigrationTestRuleFactory : SharedMigrationTestRuleFactory {
    override fun createSharedMigrationTestRule(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>
    ): SharedMigrationTestRule =
        AndroidMigrationTestRule(
            instrumentation,
            databaseClass
        )

    override fun createSharedMigrationTestRule(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>,
        specs: List<AutoMigrationSpec>
    ): SharedMigrationTestRule =
        AndroidMigrationTestRule(
            instrumentation,
            databaseClass,
            specs
        )

    override fun createSharedMigrationTestRule(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>,
        specs: List<AutoMigrationSpec>,
        openFactory: SupportSQLiteOpenHelper.Factory
    ): SharedMigrationTestRule =
        AndroidMigrationTestRule(
            instrumentation,
            databaseClass,
            specs,
            openFactory,
        )
}
