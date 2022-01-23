package org.fnives.test.showcase.core.content

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Disabled("CodeKata")
@OptIn(ExperimentalCoroutinesApi::class)
class CodeKataContentRepositoryTest {

    @BeforeEach
    fun setUp() {
    }

    @DisplayName("GIVEN no interaction THEN remote source is not called")
    @Test
    fun fetchingIsLazy() {
    }

    @DisplayName("GIVEN content response WHEN content observed THEN loading AND data is returned")
    @Test
    fun happyFlow() = runTest {
    }

    @DisplayName("GIVEN content error WHEN content observed THEN loading AND data is returned")
    @Test
    fun errorFlow() = runTest {
    }

    @DisplayName("GIVEN saved cache WHEN collected THEN cache is returned")
    @Test
    fun verifyCaching() = runTest {
    }

    @DisplayName("GIVEN no response from remote source WHEN content observed THEN loading is returned")
    @Test
    fun loadingIsShownBeforeTheRequestIsReturned() = runTest {
    }

    @DisplayName("GIVEN content response THEN error WHEN fetched THEN returned states are loading data loading error")
    @Test
    fun whenFetchingRequestIsCalledAgain() = runTest {
    }

    @DisplayName("GIVEN content response THEN error WHEN fetched THEN only 4 items are emitted")
    @Test
    fun noAdditionalItemsEmitted() {
    }
}
