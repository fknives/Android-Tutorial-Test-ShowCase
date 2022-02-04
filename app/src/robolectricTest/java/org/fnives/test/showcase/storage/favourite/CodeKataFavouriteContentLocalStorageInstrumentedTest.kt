package org.fnives.test.showcase.storage.favourite

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Disabled

@Disabled("CodeKata")
@OptIn(ExperimentalCoroutinesApi::class)
class CodeKataFavouriteContentLocalStorageInstrumentedTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    /** GIVEN just created database WHEN querying THEN empty list is returned */
    @Test
    fun atTheStartOurDatabaseIsEmpty() =  runTest {
    }

    /** GIVEN content_id WHEN added to Favourite THEN it can be read out */
    @Test
    fun addingContentIdToFavouriteCanBeLaterReadOut() {
    }

    /** GIVEN content_id added WHEN removed to Favourite THEN it no longer can be read out */
    @Test
    fun contentIdAddedThenRemovedCanNoLongerBeReadOut() {
    }

    /** GIVEN empty database WHILE observing content WHEN favourite added THEN change is emitted */
    @Test
    fun addingFavouriteUpdatesExistingObservers() {
    }

    /** GIVEN non empty database WHILE observing content WHEN favourite removed THEN change is emitted */
    @Test
    fun removingFavouriteUpdatesExistingObservers() {
    }

    /** GIVEN an observed WHEN adding and removing from it THEN we only get the expected amount of updates */
    @Test
    fun noUnexpectedUpdates() {
    }
}
