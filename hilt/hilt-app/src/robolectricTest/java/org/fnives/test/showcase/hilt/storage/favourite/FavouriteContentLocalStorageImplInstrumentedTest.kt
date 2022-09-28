package org.fnives.test.showcase.hilt.storage.favourite

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.hilt.core.integration.fake.FakeFavouriteContentLocalStorage
import org.fnives.test.showcase.hilt.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.hilt.test.shared.testutils.storage.TestDatabaseInitialization
import org.fnives.test.showcase.model.content.ContentId
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.ParameterizedRobolectricTestRunner
import javax.inject.Inject

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(ParameterizedRobolectricTestRunner::class)
internal class FavouriteContentLocalStorageImplInstrumentedTest(
    private val favouriteContentLocalStorageFactory: (FavouriteContentLocalStorage) -> FavouriteContentLocalStorage,
) {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var sut: FavouriteContentLocalStorage
    private lateinit var testDispatcher: TestDispatcher

    @Inject
    lateinit var real: FavouriteContentLocalStorage

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        TestDatabaseInitialization.dispatcher = testDispatcher
        hiltRule.inject()
        sut = favouriteContentLocalStorageFactory(real)
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

        private fun createReal(real: FavouriteContentLocalStorage): FavouriteContentLocalStorage = real

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun favouriteContentLocalStorageFactories(): List<(FavouriteContentLocalStorage) -> FavouriteContentLocalStorage> = listOf(
            { createFake() },
            { createReal(it) }
        )
    }
}
