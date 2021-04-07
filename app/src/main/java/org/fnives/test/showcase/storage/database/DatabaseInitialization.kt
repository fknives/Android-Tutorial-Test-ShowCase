package org.fnives.test.showcase.storage.database

import android.content.Context
import androidx.room.Room
import org.fnives.test.showcase.storage.LocalDatabase

object DatabaseInitialization {

    fun create(context: Context): LocalDatabase =
        Room.databaseBuilder(context, LocalDatabase::class.java, "local_database")
            .allowMainThreadQueries()
            .build()
}
