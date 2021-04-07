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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
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
internal class ContentRepositoryTest {

    private lateinit var sut: ContentRepository
    private lateinit var mockContentRemoteSource: ContentRemoteSource
    private lateinit var testDispatcher: TestCoroutineDispatcher

    @BeforeEach
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        mockContentRemoteSource = mock()
        sut = ContentRepository(mockContentRemoteSource)
    }

    @Test
    fun GIVEN_no_interaction_THEN_remote_source_is_not_called() {
        verifyNoMoreInteractions(mockContentRemoteSource)
    }

    @Test
    fun GIVEN_no_response_from_remote_source_WHEN_content_observed_THEN_loading_is_returned() =
        runBlockingTest(testDispatcher) {
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

    @Test
    fun GIVEN_content_response_WHEN_content_observed_THEN_loading_AND_data_is_returned() =
        runBlockingTest(testDispatcher) {
            val expected = listOf(
                Resource.Loading(),
                Resource.Success(listOf(Content(ContentId("a"), "", "", ImageUrl(""))))
            )
            whenever(mockContentRemoteSource.get())
                .doReturn(listOf(Content(ContentId("a"), "", "", ImageUrl(""))))

            val actual = sut.contents.take(2).toList()

            Assertions.assertEquals(expected, actual)
        }

    @Test
    fun GIVEN_content_error_WHEN_content_observed_THEN_loading_AND_data_is_returned() =
        runBlockingTest(testDispatcher) {
            val exception = RuntimeException()
            val expected = listOf(
                Resource.Loading(),
                Resource.Error<List<Content>>(UnexpectedException(exception))
            )
            whenever(mockContentRemoteSource.get()).doThrow(exception)

            val actual = sut.contents.take(2).toList()

            Assertions.assertEquals(expected, actual)
        }

    @Test
    fun GIVEN_content_response_THEN_error_WHEN_fetched_THEN_returned_states_are_loading_data_loading_error() =
        runBlockingTest(testDispatcher) {
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

            val actual = async(testDispatcher) { sut.contents.take(4).toList() }
            testDispatcher.advanceUntilIdle()
            sut.fetch()
            testDispatcher.advanceUntilIdle()

            Assertions.assertEquals(expected, actual.await())
        }

    @Test
    fun GIVEN_content_response_THEN_error_WHEN_fetched_THEN_only_4_items_are_emitted() {
        Assertions.assertThrows(IllegalStateException::class.java) {
            runBlockingTest(testDispatcher) {
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

                val actual = async(testDispatcher) { sut.contents.take(5).toList() }
                testDispatcher.advanceUntilIdle()
                sut.fetch()
                testDispatcher.advanceUntilIdle()

                Assertions.assertEquals(expected, actual.await())
            }
        }
    }

    @Test
    fun GIVEN_saved_cache_WHEN_collected_THEN_cache_is_returned() = runBlockingTest {
        val content = Content(ContentId("1"), "", "", ImageUrl(""))
        val expected = listOf(Resource.Success(listOf(content)))
        whenever(mockContentRemoteSource.get()).doReturn(listOf(content))
        sut.contents.take(2).toList()

        val actual = sut.contents.take(1).toList()

        verify(mockContentRemoteSource, times(1)).get()
        Assertions.assertEquals(expected, actual)
    }
}
