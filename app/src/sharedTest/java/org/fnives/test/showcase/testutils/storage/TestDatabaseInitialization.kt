package org.fnives.test.showcase.testutils.storage

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.fnives.test.showcase.storage.LocalDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * Reloads the Database Koin module, so it uses the inMemory database with the switched out Executors.
 */
object TestDatabaseInitialization {

    fun create(context: Context, dispatcher: CoroutineDispatcher): LocalDatabase {
        val executor = dispatcher.asExecutor()
        return Room.inMemoryDatabaseBuilder(context, LocalDatabase::class.java)
            .setTransactionExecutor(executor)
            .setQueryExecutor(executor)
            .allowMainThreadQueries()
            .build()
    }

    fun overwriteDatabaseInitialization(dispatcher: CoroutineDispatcher) {
        loadKoinModules(
            module {
                single { create(androidContext(), dispatcher) }
            }
        )
    }
}
