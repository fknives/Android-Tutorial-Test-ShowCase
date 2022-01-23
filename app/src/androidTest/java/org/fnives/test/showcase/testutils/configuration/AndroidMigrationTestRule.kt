package org.fnives.test.showcase.testutils.configuration

import android.app.Instrumentation
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.junit.runner.Description
import org.junit.runners.model.Statement

class AndroidMigrationTestRule : SharedMigrationTestRule {

    private val migrationTestHelper: MigrationTestHelper

    constructor(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>
    ) {
        migrationTestHelper = MigrationTestHelper(instrumentation, databaseClass)
    }

    constructor(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>,
        specs: List<AutoMigrationSpec>
    ) {
        migrationTestHelper = MigrationTestHelper(instrumentation, databaseClass, specs)
    }

    constructor(
        instrumentation: Instrumentation,
        databaseClass: Class<out RoomDatabase>,
        specs: List<AutoMigrationSpec>,
        openFactory: SupportSQLiteOpenHelper.Factory
    ) {
        migrationTestHelper =
            MigrationTestHelper(instrumentation, databaseClass, specs, openFactory)
    }

    override fun apply(base: Statement, description: Description): Statement =
        migrationTestHelper.apply(base, description)

    override fun closeWhenFinished(db: RoomDatabase) =
        migrationTestHelper.closeWhenFinished(db)

    override fun closeWhenFinished(db: SupportSQLiteDatabase) =
        migrationTestHelper.closeWhenFinished(db)

    override fun createDatabase(name: String, version: Int): SupportSQLiteDatabase =
        migrationTestHelper.createDatabase(name, version)

    override fun runMigrationsAndValidate(
        name: String,
        version: Int,
        validateDroppedTables: Boolean,
        vararg migrations: Migration
    ): SupportSQLiteDatabase =
        migrationTestHelper.runMigrationsAndValidate(
            name,
            version,
            validateDroppedTables,
            *migrations
        )
}
