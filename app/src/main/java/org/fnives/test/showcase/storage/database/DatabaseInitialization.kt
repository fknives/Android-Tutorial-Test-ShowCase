package org.fnives.test.showcase.storage.database

import android.content.Context
import androidx.room.Room
import org.fnives.test.showcase.storage.LocalDatabase
import org.fnives.test.showcase.storage.migation.Migration1To2

object DatabaseInitialization {

    fun create(context: Context): LocalDatabase =
        Room.databaseBuilder(context, LocalDatabase::class.java, "local_database")
            .addMigrations(Migration1To2())
            .allowMainThreadQueries()
            .build()
}
