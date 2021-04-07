package org.fnives.test.showcase.storage.database

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.fnives.test.showcase.storage.LocalDatabase

object DatabaseInitialization {

    lateinit var dispatcher: CoroutineDispatcher

    fun create(context: Context): LocalDatabase {
        val executor = dispatcher.asExecutor()
        return Room.inMemoryDatabaseBuilder(context, LocalDatabase::class.java)
            .setTransactionExecutor(executor)
            .setQueryExecutor(executor)
            .allowMainThreadQueries()
            .build()
    }
}
