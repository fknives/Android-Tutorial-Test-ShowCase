package org.fnives.test.showcase.favourite

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
internal class FavouriteContentLocalStorageImplTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var sut: FavouriteContentLocalStorage
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())
        DatabaseInitialization.dispatcher = testDispatcher
        hiltRule.inject()
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

        val actual = async(coroutineContext) {
            sut.observeFavourites().take(2).toList()
        }
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
}
