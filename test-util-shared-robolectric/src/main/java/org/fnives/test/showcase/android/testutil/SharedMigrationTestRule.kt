package org.fnives.test.showcase.android.testutil

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.rules.TestRule
import java.io.IOException

/**
 * Unifying API above [MigrationTestHelper][androidx.room.testing.MigrationTestHelper].
 *
 * This is intended to be used in MigrationTests that are shared. Meaning the same test can run on both Device and via Robolectric.
*/
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
