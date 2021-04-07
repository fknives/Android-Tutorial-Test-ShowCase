package org.fnives.test.showcase.core.content

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.model.content.ImageUrl
import org.fnives.test.showcase.model.shared.Resource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
internal class GetAllContentUseCaseTest {

    private lateinit var sut: GetAllContentUseCase
    private lateinit var mockContentRepository: ContentRepository
    private lateinit var mockFavouriteContentLocalStorage: FavouriteContentLocalStorage
    private lateinit var contentFlow: MutableStateFlow<Resource<List<Content>>>
    private lateinit var favouriteContentIdFlow: MutableStateFlow<List<ContentId>>
    private lateinit var testDispatcher: TestCoroutineDispatcher

    @BeforeEach
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        mockFavouriteContentLocalStorage = mock()
        mockContentRepository = mock()
        favouriteContentIdFlow = MutableStateFlow(emptyList())
        contentFlow = MutableStateFlow(Resource.Loading())
        whenever(mockFavouriteContentLocalStorage.observeFavourites()).doReturn(favouriteContentIdFlow)
        whenever(mockContentRepository.contents).doReturn(contentFlow)
        sut = GetAllContentUseCase(mockContentRepository, mockFavouriteContentLocalStorage)
    }

    @Test
    fun GIVEN_loading_AND_empty_favourite_WHEN_observed_THEN_loading_is_shown() = runBlockingTest(testDispatcher) {
        favouriteContentIdFlow.value = emptyList()
        contentFlow.value = Resource.Loading()
        val expected = Resource.Loading<List<FavouriteContent>>()

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @Test
    fun GIVEN_loading_AND_listOfFavourite_WHEN_observed_THEN_loading_is_shown() = runBlockingTest(testDispatcher) {
        favouriteContentIdFlow.value = listOf(ContentId("a"))
        contentFlow.value = Resource.Loading()
        val expected = Resource.Loading<List<FavouriteContent>>()

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @Test
    fun GIVEN_error_AND_empty_favourite_WHEN_observed_THEN_error_is_shown() = runBlockingTest(testDispatcher) {
        favouriteContentIdFlow.value = emptyList()
        val exception = Throwable()
        contentFlow.value = Resource.Error(exception)
        val expected = Resource.Error<List<FavouriteContent>>(exception)

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @Test
    fun GIVEN_error_AND_listOfFavourite_WHEN_observed_THEN_error_is_shown() = runBlockingTest(testDispatcher) {
        favouriteContentIdFlow.value = listOf(ContentId("b"))
        val exception = Throwable()
        contentFlow.value = Resource.Error(exception)
        val expected = Resource.Error<List<FavouriteContent>>(exception)

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @Test
    fun GIVEN_listOfContent_AND_empty_favourite_WHEN_observed_THEN_favourites_are_returned() = runBlockingTest(testDispatcher) {
        favouriteContentIdFlow.value = emptyList()
        val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
        contentFlow.value = Resource.Success(listOf(content))
        val items = listOf(
            FavouriteContent(content, false)
        )
        val expected = Resource.Success(items)

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @Test
    fun GIVEN_listOfContent_AND_other_favourite_id_WHEN_observed_THEN_favourites_are_returned() =
        runBlockingTest(testDispatcher) {
            favouriteContentIdFlow.value = listOf(ContentId("x"))
            val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
            contentFlow.value = Resource.Success(listOf(content))
            val items = listOf(
                FavouriteContent(content, false)
            )
            val expected = Resource.Success(items)

            val actual = sut.get().take(1).toList()

            Assertions.assertEquals(listOf(expected), actual)
        }

    @Test
    fun GIVEN_listOfContent_AND_same_favourite_id_WHEN_observed_THEN_favourites_are_returned() =
        runBlockingTest(testDispatcher) {
            favouriteContentIdFlow.value = listOf(ContentId("a"))
            val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
            contentFlow.value = Resource.Success(listOf(content))
            val items = listOf(
                FavouriteContent(content, true)
            )
            val expected = Resource.Success(items)

            val actual = sut.get().take(1).toList()

            Assertions.assertEquals(listOf(expected), actual)
        }

    @Test
    fun GIVEN_loading_then_data_then_added_favourite_WHEN_observed_THEN_loading_then_correct_favourites_are_returned() =
        runBlockingTest(testDispatcher) {
            favouriteContentIdFlow.value = emptyList()
            val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
            contentFlow.value = Resource.Loading()
            val expected = listOf(
                Resource.Loading(),
                Resource.Success(listOf(FavouriteContent(content, false))),
                Resource.Success(listOf(FavouriteContent(content, true)))
            )

            val actual = async(testDispatcher) {
                sut.get().take(3).toList()
            }
            testDispatcher.advanceUntilIdle()

            contentFlow.value = Resource.Success(listOf(content))
            testDispatcher.advanceUntilIdle()

            favouriteContentIdFlow.value = listOf(ContentId("a"))
            testDispatcher.advanceUntilIdle()

            Assertions.assertEquals(expected, actual.await())
        }

    @Test
    fun GIVEN_loading_then_data_then_removed_favourite_WHEN_observed_THEN_loading_then_correct_favourites_are_returned() =
        runBlockingTest(testDispatcher) {
            favouriteContentIdFlow.value = listOf(ContentId("a"))
            val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
            contentFlow.value = Resource.Loading()
            val expected = listOf(
                Resource.Loading(),
                Resource.Success(listOf(FavouriteContent(content, true))),
                Resource.Success(listOf(FavouriteContent(content, false)))
            )

            val actual = async(testDispatcher) {
                sut.get().take(3).toList()
            }
            testDispatcher.advanceUntilIdle()

            contentFlow.value = Resource.Success(listOf(content))
            testDispatcher.advanceUntilIdle()

            favouriteContentIdFlow.value = emptyList()
            testDispatcher.advanceUntilIdle()

            Assertions.assertEquals(expected, actual.await())
        }

    @Test
    fun GIVEN_loading_then_data_then_loading_WHEN_observed_THEN_loading_then_correct_favourites_then_loadingare_returned() =
        runBlockingTest(testDispatcher) {
            favouriteContentIdFlow.value = listOf(ContentId("a"))
            val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
            contentFlow.value = Resource.Loading()
            val expected = listOf(
                Resource.Loading(),
                Resource.Success(listOf(FavouriteContent(content, true))),
                Resource.Loading()
            )

            val actual = async(testDispatcher) {
                sut.get().take(3).toList()
            }
            testDispatcher.advanceUntilIdle()

            contentFlow.value = Resource.Success(listOf(content))
            testDispatcher.advanceUntilIdle()

            contentFlow.value = Resource.Loading()
            testDispatcher.advanceUntilIdle()

            Assertions.assertEquals(expected, actual.await())
        }
}
