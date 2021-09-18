package org.fnives.test.showcase.core.content

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.fnives.test.showcase.core.shared.UnexpectedException
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.ImageUrl
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.content.ContentRemoteSource
import org.junit.jupiter.api.*
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@Disabled("CodeKata")
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
    fun happyFlow() = runBlockingTest {

    }

    @DisplayName("GIVEN content error WHEN content observed THEN loading AND data is returned")
    @Test
    fun errorFlow() = runBlockingTest {

    }

    @DisplayName("GIVEN saved cache WHEN collected THEN cache is returned")
    @Test
    fun verifyCaching() = runBlockingTest {

    }

    @DisplayName("GIVEN no response from remote source WHEN content observed THEN loading is returned")
    @Test
    fun loadingIsShownBeforeTheRequestIsReturned() = runBlockingTest {

    }

    @DisplayName("GIVEN content response THEN error WHEN fetched THEN returned states are loading data loading error")
    @Test
    fun whenFetchingRequestIsCalledAgain() = runBlockingTest {

    }

    @DisplayName("GIVEN content response THEN error WHEN fetched THEN only 4 items are emitted")
    @Test
    fun noAdditionalItemsEmitted() {

    }
}