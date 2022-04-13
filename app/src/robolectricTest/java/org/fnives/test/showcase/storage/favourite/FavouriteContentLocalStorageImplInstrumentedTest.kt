package org.fnives.test.showcase.storage.favourite

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.integration.fake.FakeFavouriteContentLocalStorage
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.testutils.storage.TestDatabaseInitialization
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.robolectric.ParameterizedRobolectricTestRunner

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
internal class FavouriteContentLocalStorageImplInstrumentedTest(
    private val favouriteContentLocalStorageFactory: KoinTest.() -> FavouriteContentLocalStorage
) : KoinTest {

    private lateinit var sut: FavouriteContentLocalStorage
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        TestDatabaseInitialization.overwriteDatabaseInitialization(testDispatcher)
        sut = favouriteContentLocalStorageFactory()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    /** GIVEN just created database WHEN querying THEN empty list is returned */
    @Test
    fun atTheStartOurDatabaseIsEmpty() = runTest(testDispatcher) {
        val actual = sut.observeFavourites().first()

        Assert.assertEquals(emptyList<ContentId>(), actual)
    }

    /** GIVEN content_id WHEN added to Favourite THEN it can be read out */
    @Test
    fun addingContentIdToFavouriteCanBeLaterReadOut() = runTest(testDispatcher) {
        val expected = listOf(ContentId("a"))

        sut.markAsFavourite(ContentId("a"))
        val actual = sut.observeFavourites().first()

        Assert.assertEquals(expected, actual)
    }

    /** GIVEN content_id added WHEN removed to Favourite THEN it no longer can be read out */
    @Test
    fun contentIdAddedThenRemovedCanNoLongerBeReadOut() = runTest(testDispatcher) {
        val expected = listOf<ContentId>()
        sut.markAsFavourite(ContentId("b"))

        sut.deleteAsFavourite(ContentId("b"))
        val actual = sut.observeFavourites().first()

        Assert.assertEquals(expected, actual)
    }

    /** GIVEN empty database WHILE observing content WHEN favourite added THEN change is emitted */
    @Test
    fun addingFavouriteUpdatesExistingObservers() = runTest(testDispatcher) {
        val expected = listOf(listOf(), listOf(ContentId("a")))
        val actual = async(coroutineContext) { sut.observeFavourites().take(2).toList() }
        advanceUntilIdle()

        sut.markAsFavourite(ContentId("a"))
        advanceUntilIdle()

        Assert.assertEquals(expected, actual.getCompleted())
    }

    /** GIVEN non empty database WHILE observing content WHEN favourite removed THEN change is emitted */
    @Test
    fun removingFavouriteUpdatesExistingObservers() = runTest(testDispatcher) {
        val expected = listOf(listOf(ContentId("a")), listOf())
        sut.markAsFavourite(ContentId("a"))

        val actual = async(coroutineContext) {
            sut.observeFavourites().take(2).toList()
        }
        advanceUntilIdle()

        sut.deleteAsFavourite(ContentId("a"))
        advanceUntilIdle()

        Assert.assertEquals(expected, actual.getCompleted())
    }

    /** GIVEN an observed WHEN adding and removing from it THEN we only get the expected amount of updates */
    @Test
    fun noUnexpectedUpdates() = runTest(testDispatcher) {
        val actual = async(coroutineContext) { sut.observeFavourites().take(4).toList() }
        advanceUntilIdle()

        sut.markAsFavourite(ContentId("a"))
        advanceUntilIdle()
        sut.deleteAsFavourite(ContentId("a"))
        advanceUntilIdle()

        Assert.assertFalse(actual.isCompleted)
        actual.cancel()
    }

    companion object {

        private fun createFake(): FavouriteContentLocalStorage = FakeFavouriteContentLocalStorage()

        private fun KoinTest.createReal(): FavouriteContentLocalStorage = get()

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun favouriteContentLocalStorageFactories(): List<KoinTest.() -> FavouriteContentLocalStorage> = listOf(
            { createFake() },
            { createReal() }
        )
    }
}
