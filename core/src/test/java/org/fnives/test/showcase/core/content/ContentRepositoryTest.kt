package org.fnives.test.showcase.core.content

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.shared.UnexpectedException
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.ImageUrl
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.content.ContentRemoteSource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
internal class ContentRepositoryTest {

    private lateinit var sut: ContentRepository
    private lateinit var mockContentRemoteSource: ContentRemoteSource

    @BeforeEach
    fun setUp() {
        mockContentRemoteSource = mock()
        sut = ContentRepository(mockContentRemoteSource)
    }

    @DisplayName("GIVEN no interaction THEN remote source is not called")
    @Test
    fun fetchingIsLazy() {
        verifyNoMoreInteractions(mockContentRemoteSource)
    }

    @DisplayName("GIVEN content response WHEN content observed THEN loading AND data is returned")
    @Test
    fun happyFlow() = runTest {
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(listOf(Content(ContentId("a"), "", "", ImageUrl(""))))
        )
        whenever(mockContentRemoteSource.get()).doReturn(
            listOf(Content(ContentId("a"), "", "", ImageUrl("")))
        )

        val actual = sut.contents.take(2).toList()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN content error WHEN content observed THEN loading AND data is returned")
    @Test
    fun errorFlow() = runTest {
        val exception = RuntimeException()
        val expected = listOf(
            Resource.Loading(),
            Resource.Error<List<Content>>(UnexpectedException(exception))
        )
        whenever(mockContentRemoteSource.get()).doThrow(exception)

        val actual = sut.contents.take(2).toList()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN saved cache WHEN collected THEN cache is returned")
    @Test
    fun verifyCaching() = runTest {
        val content = Content(ContentId("1"), "", "", ImageUrl(""))
        val expected = listOf(Resource.Success(listOf(content)))
        whenever(mockContentRemoteSource.get()).doReturn(listOf(content))
        sut.contents.take(2).toList()

        val actual = sut.contents.take(1).toList()

        verify(mockContentRemoteSource, times(1)).get()
        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN no response from remote source WHEN content observed THEN loading is returned")
    @Test
    fun loadingIsShownBeforeTheRequestIsReturned() = runTest {
        val expected = Resource.Loading<List<Content>>()
        val suspendedRequest = CompletableDeferred<Unit>()
        whenever(mockContentRemoteSource.get()).doSuspendableAnswer {
            suspendedRequest.await()
            emptyList()
        }

        val actual = sut.contents.take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
        suspendedRequest.complete(Unit)
    }

    @DisplayName("GIVEN content response THEN error WHEN fetched THEN returned states are loading data loading error")
    @Test
    fun whenFetchingRequestIsCalledAgain() = runTest() {
        val exception = RuntimeException()
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(emptyList()),
            Resource.Loading(),
            Resource.Error<List<Content>>(UnexpectedException(exception))
        )
        var first = true
        whenever(mockContentRemoteSource.get()).doAnswer {
            if (first) emptyList<Content>().also { first = false } else throw exception
        }

        val actual = async {
            sut.contents.take(4).toList()
        }
        advanceUntilIdle()
        sut.fetch()
        advanceUntilIdle()

        Assertions.assertEquals(expected, actual.getCompleted())
    }

    @Test
    fun a() {
        repeat(10000) {
            setUp()
            whenFetchingRequestIsCalledAgain()
        }
    }

    @DisplayName("GIVEN content response THEN error WHEN fetched THEN only 4 items are emitted")
    @Test
    fun noAdditionalItemsEmitted() = runTest {
        val exception = RuntimeException()
        var first = true
        whenever(mockContentRemoteSource.get()).doAnswer {
            if (first) emptyList<Content>().also { first = false } else throw exception
        }

        val actual = async(coroutineContext) { sut.contents.take(5).toList() }
        advanceUntilIdle()
        sut.fetch()
        advanceUntilIdle()

        Assertions.assertFalse(actual.isCompleted)
        actual.cancel()
    }
}
