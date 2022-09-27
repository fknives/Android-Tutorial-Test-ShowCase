package org.fnives.test.showcase.hilt.storage.migation

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration1To2 : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE FavouriteEntity RENAME TO FavouriteEntityOld")
        database.execSQL("CREATE TABLE FavouriteEntity(content_id TEXT NOT NULL PRIMARY KEY)")
        database.execSQL("INSERT INTO FavouriteEntity(content_id) SELECT contentId FROM FavouriteEntityOld")
        database.execSQL("DROP TABLE FavouriteEntityOld")
    }
}
