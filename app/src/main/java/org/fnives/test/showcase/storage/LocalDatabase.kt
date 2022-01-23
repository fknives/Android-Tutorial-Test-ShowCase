package org.fnives.test.showcase.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import org.fnives.test.showcase.storage.favourite.FavouriteDao
import org.fnives.test.showcase.storage.favourite.FavouriteEntity

@Database(
    entities = [FavouriteEntity::class],
    version = 2,
    exportSchema = true
)
abstract class LocalDatabase : RoomDatabase() {

    abstract val favouriteDao: FavouriteDao
}
