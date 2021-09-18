package org.fnives.test.showcase.favourite

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
internal class FavouriteContentLocalStorageImplTest : KoinTest {

    private val sut by inject<FavouriteContentLocalStorage>()
    private lateinit var testDispatcher: TestCoroutineDispatcher

    @Before
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        DatabaseInitialization.dispatcher = testDispatcher
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun GIVEN_content_id_WHEN_added_to_Favourite_THEN_it_can_be_read_out() = runBlocking {
            val expected = listOf(ContentId("a"))

            sut.markAsFavourite(ContentId("a"))
            val actual = sut.observeFavourites().first()

            Assert.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_content_id_added_WHEN_removed_to_Favourite_THEN_it_no_longer_can_be_read_out() = runBlocking {
        val expected = listOf<ContentId>()
        sut.markAsFavourite(ContentId("b"))

        sut.deleteAsFavourite(ContentId("b"))
        val actual = sut.observeFavourites().first()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_empty_database_WHILE_observing_content_WHEN_favourite_added_THEN_change_is_emitted() =
        runBlocking<Unit> {
            val expected = listOf(listOf(), listOf(ContentId("a")))

            val testDispatcher = TestCoroutineDispatcher()
            val actual = async(testDispatcher) {
                sut.observeFavourites().take(2).toList()
            }
            testDispatcher.advanceUntilIdle()

            sut.markAsFavourite(ContentId("a"))

            Assert.assertEquals(expected, actual.await())
        }

    @Test
    fun GIVEN_non_empty_database_WHILE_observing_content_WHEN_favourite_removed_THEN_change_is_emitted() =
        runBlocking<Unit> {
            val expected = listOf(listOf(ContentId("a")), listOf())
            sut.markAsFavourite(ContentId("a"))

            val testDispatcher = TestCoroutineDispatcher()
            val actual = async(testDispatcher) {
                sut.observeFavourites().take(2).toList()
            }
            testDispatcher.advanceUntilIdle()

            sut.deleteAsFavourite(ContentId("a"))

            Assert.assertEquals(expected, actual.await())
        }
}
