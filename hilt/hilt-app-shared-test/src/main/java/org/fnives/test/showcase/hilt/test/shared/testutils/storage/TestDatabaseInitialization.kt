package org.fnives.test.showcase.hilt.test.shared.testutils.storage

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import org.fnives.test.showcase.hilt.di.StorageModule
import org.fnives.test.showcase.hilt.storage.LocalDatabase
import javax.inject.Singleton

/**
 * Reloads the Database module, so it uses the inMemory database with the switched out Executors.
 *
 * This is needed so in AndroidTests not a real File based device is used.
 * This speeds tests up, and isolates them better, there will be no junk in the Database file from previous tests.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [StorageModule::class]
)
object TestDatabaseInitialization {

    var dispatcher: CoroutineDispatcher? = null

    @Suppress("ObjectPropertyName")
    private val _dispatcher: CoroutineDispatcher
        get() = dispatcher ?: throw IllegalStateException("TestDispatcher is not initialized")

    fun create(context: Context, dispatcher: CoroutineDispatcher = this._dispatcher): LocalDatabase {
        val executor = dispatcher.asExecutor()
        return Room.inMemoryDatabaseBuilder(context, LocalDatabase::class.java)
            .setTransactionExecutor(executor)
            .setQueryExecutor(executor)
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun provideLocalDatabase(@ApplicationContext context: Context): LocalDatabase =
        create(context)
}
