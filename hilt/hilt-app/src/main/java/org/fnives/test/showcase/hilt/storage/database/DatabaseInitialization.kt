package org.fnives.test.showcase.hilt.storage.database

import android.content.Context
import androidx.room.Room
import org.fnives.test.showcase.hilt.storage.LocalDatabase
import org.fnives.test.showcase.hilt.storage.migation.Migration1To2

object DatabaseInitialization {

    fun create(context: Context): LocalDatabase =
        Room.databaseBuilder(context, LocalDatabase::class.java, "local_database")
            .addMigrations(Migration1To2())
            .allowMainThreadQueries()
            .build()
}
