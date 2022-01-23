package org.fnives.test.showcase.testutils.configuration

import android.app.Instrumentation
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.junit.rules.TestRule
import java.io.IOException

interface SharedMigrationTestRule : TestRule {

    @Throws(IOException::class)
    fun createDatabase(name: String, version: Int): SupportSQLiteDatabase

    @Throws(IOException::class)
    fun runMigrationsAndValidate(
        name: String,
        version: Int,
        validateDroppedTables: Boolean,
        vararg migrations: Migration
    ): SupportSQLiteDatabase

    fun closeWhenFinished(db: SupportSQLiteDatabase)
    fun closeWhenFinished(db: RoomDatabase)
}

inline fun <reified DB : RoomDatabase> createSharedMigrationTestRule(
    instrumentation: Instrumentation
): SharedMigrationTestRule =
    SpecificTestConfigurationsFactory.createSharedMigrationTestRuleFactory()
        .createSharedMigrationTestRule(
            instrumentation,
            DB::class.java
        )

inline fun <reified DB : RoomDatabase> createSharedMigrationTestRule(
    instrumentation: Instrumentation,
    specs: List<AutoMigrationSpec>
): SharedMigrationTestRule =
    SpecificTestConfigurationsFactory.createSharedMigrationTestRuleFactory()
        .createSharedMigrationTestRule(
            instrumentation,
            DB::class.java,
            specs
        )

inline fun <reified DB : RoomDatabase> createSharedMigrationTestRule(
    instrumentation: Instrumentation,
    specs: List<AutoMigrationSpec>,
    openFactory: SupportSQLiteOpenHelper.Factory
): SharedMigrationTestRule =
    SpecificTestConfigurationsFactory.createSharedMigrationTestRuleFactory()
        .createSharedMigrationTestRule(
            instrumentation,
            DB::class.java,
            specs,
            openFactory
        )
