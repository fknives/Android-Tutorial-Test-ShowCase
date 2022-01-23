package org.fnives.test.showcase.testutils.configuration

import android.app.Instrumentation
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteOpenHelper

object RobolectricMigrationTestHelperFactory : SharedMigrationTestRuleFactory {
    override fun createSharedMigrationTestRule(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>
    ): SharedMigrationTestRule =
        RobolectricMigrationTestHelper(
            instrumentation,
            databaseClass
        )

    override fun createSharedMigrationTestRule(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>,
        specs: List<AutoMigrationSpec>
    ): SharedMigrationTestRule =
        RobolectricMigrationTestHelper(
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
        RobolectricMigrationTestHelper(
            instrumentation,
            databaseClass,
            specs,
            openFactory
        )
}
