package org.fnives.test.showcase.hilt.test.shared.storage.migration

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.android.testutil.SharedMigrationTestRule
import org.fnives.test.showcase.hilt.storage.LocalDatabase
import org.fnives.test.showcase.hilt.storage.favourite.FavouriteEntity
import org.fnives.test.showcase.hilt.storage.migation.Migration1To2
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * reference:
 * https://medium.com/androiddevelopers/testing-room-migrations-be93cdb0d975
 * https://developer.android.com/training/data-storage/room/migrating-db-versions
 */
@RunWith(AndroidJUnit4::class)
open class MigrationToLatestInstrumentedSharedTest {

    @get:Rule
    val helper = SharedMigrationTestRule<LocalDatabase>(instrumentation = InstrumentationRegistry.getInstrumentation())

    private fun getMigratedRoomDatabase(): LocalDatabase {
        val database: LocalDatabase = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            LocalDatabase::class.java,
            TEST_DB
        )
            .addMigrations(Migration1To2())
            .build()
        // close the database and release any stream resources when the test finishes
        helper.closeWhenFinished(database)
        return database
    }

    @Test
    @Throws(IOException::class)
    open fun migrate1To2() {
        val expectedEntities = setOf(
            FavouriteEntity("123"),
            FavouriteEntity("124"),
            FavouriteEntity("125")
        )
        val version1DB = helper.createDatabase(
            name = TEST_DB,
            version = 1
        )
        version1DB.run {
            execSQL("INSERT OR IGNORE INTO `FavouriteEntity` (`contentId`) VALUES (\"123\")")
            execSQL("INSERT OR IGNORE INTO `FavouriteEntity` (`contentId`) VALUES (124)")
            execSQL("INSERT OR IGNORE INTO `FavouriteEntity` (`contentId`) VALUES (125)")
        }
        version1DB.close()

        val version2DB = helper.runMigrationsAndValidate(
            name = TEST_DB,
            version = 2,
            validateDroppedTables = true,
            Migration1To2()
        )
        version2DB.close()

        val favouriteDao = getMigratedRoomDatabase().favouriteDao

        val entities = runBlocking { favouriteDao.get().first() }.toSet()

        Assert.assertEquals(expectedEntities, entities)
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
