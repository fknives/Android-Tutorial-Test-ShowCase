package org.fnives.test.showcase.hilt.core.content

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.hilt.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.model.content.ImageUrl
import org.fnives.test.showcase.model.shared.Resource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
internal class GetAllContentUseCaseTest {

    private lateinit var sut: GetAllContentUseCase
    private lateinit var mockContentRepository: ContentRepository
    private lateinit var mockFavouriteContentLocalStorage: FavouriteContentLocalStorage
    private lateinit var contentFlow: MutableStateFlow<Resource<List<Content>>>
    private lateinit var favouriteContentIdFlow: MutableStateFlow<List<ContentId>>

    @BeforeEach
    fun setUp() {
        mockFavouriteContentLocalStorage = mock()
        mockContentRepository = mock()
        favouriteContentIdFlow = MutableStateFlow(emptyList())
        contentFlow = MutableStateFlow(Resource.Loading())
        whenever(mockFavouriteContentLocalStorage.observeFavourites()).doReturn(
            favouriteContentIdFlow
        )
        whenever(mockContentRepository.contents).doReturn(contentFlow)
        sut = GetAllContentUseCase(mockContentRepository, mockFavouriteContentLocalStorage)
    }

    @DisplayName("GIVEN loading AND empty favourite WHEN observed THEN loading is shown")
    @Test
    fun loadingResourceWithNoFavouritesResultsInLoadingResource() = runTest {
        favouriteContentIdFlow.value = emptyList()
        contentFlow.value = Resource.Loading()
        val expected = Resource.Loading<List<FavouriteContent>>()

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @DisplayName("GIVEN loading AND listOfFavourite WHEN observed THEN loading is shown")
    @Test
    fun loadingResourceWithFavouritesResultsInLoadingResource() = runTest {
        favouriteContentIdFlow.value = listOf(ContentId("a"))
        contentFlow.value = Resource.Loading()
        val expected = Resource.Loading<List<FavouriteContent>>()

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @DisplayName("GIVEN error AND empty favourite WHEN observed THEN error is shown")
    @Test
    fun errorResourceWithNoFavouritesResultsInErrorResource() = runTest {
        favouriteContentIdFlow.value = emptyList()
        val exception = Throwable()
        contentFlow.value = Resource.Error(exception)
        val expected = Resource.Error<List<FavouriteContent>>(exception)

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @DisplayName("GIVEN error AND listOfFavourite WHEN observed THEN error is shown")
    @Test
    fun errorResourceWithFavouritesResultsInErrorResource() = runTest {
        favouriteContentIdFlow.value = listOf(ContentId("b"))
        val exception = Throwable()
        contentFlow.value = Resource.Error(exception)
        val expected = Resource.Error<List<FavouriteContent>>(exception)

        val actual = sut.get().take(1).toList()

        Assertions.assertEquals(listOf(expected), actual)
    }

    @DisplayName("GIVEN listOfContent AND empty favourite WHEN observed THEN favourites are returned")
    @Test
    fun successResourceWithNoFavouritesResultsInNoFavouritedItems() = runTest {
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

    @DisplayName("GIVEN listOfContent AND other favourite id WHEN observed THEN favourites are returned")
    @Test
    fun successResourceWithDifferentFavouritesResultsInNoFavouritedItems() = runTest {
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

    @DisplayName("GIVEN listOfContent AND same favourite id WHEN observed THEN favourites are returned")
    @Test
    fun successResourceWithSameFavouritesResultsInFavouritedItems() = runTest {
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

    @DisplayName("GIVEN loading then data then added favourite WHEN observed THEN loading then correct favourites are returned")
    @Test
    fun whileLoadingAndAddingItemsReactsProperly() = runTest {
        favouriteContentIdFlow.value = emptyList()
        val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
        contentFlow.value = Resource.Loading()
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(listOf(FavouriteContent(content, false))),
            Resource.Success(listOf(FavouriteContent(content, true)))
        )

        val actual = async(coroutineContext) {
            sut.get().take(3).toList()
        }
        advanceUntilIdle()

        contentFlow.value = Resource.Success(listOf(content))
        advanceUntilIdle()

        favouriteContentIdFlow.value = listOf(ContentId("a"))
        advanceUntilIdle()

        Assertions.assertEquals(expected, actual.getCompleted())
    }

    @DisplayName("GIVEN loading then data then removed favourite WHEN observed THEN loading then correct favourites are returned")
    @Test
    fun whileLoadingAndRemovingItemsReactsProperly() = runTest {
        favouriteContentIdFlow.value = listOf(ContentId("a"))
        val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
        contentFlow.value = Resource.Loading()
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(listOf(FavouriteContent(content, true))),
            Resource.Success(listOf(FavouriteContent(content, false)))
        )

        val actual = async(coroutineContext) {
            sut.get().take(3).toList()
        }
        advanceUntilIdle()

        contentFlow.value = Resource.Success(listOf(content))
        advanceUntilIdle()

        favouriteContentIdFlow.value = emptyList()
        advanceUntilIdle()

        Assertions.assertEquals(expected, actual.getCompleted())
    }

    @DisplayName("GIVEN loading then data then loading WHEN observed THEN loading then correct favourites then loading are returned")
    @Test
    fun loadingThenDataThenLoadingReactsProperly() = runTest {
        favouriteContentIdFlow.value = listOf(ContentId("a"))
        val content = Content(ContentId("a"), "b", "c", ImageUrl("d"))
        contentFlow.value = Resource.Loading()
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(listOf(FavouriteContent(content, true))),
            Resource.Loading()
        )

        val actual = async(coroutineContext) {
            sut.get().take(3).toList()
        }
        advanceUntilIdle()

        contentFlow.value = Resource.Success(listOf(content))
        advanceUntilIdle()

        contentFlow.value = Resource.Loading()
        advanceUntilIdle()

        Assertions.assertEquals(expected, actual.getCompleted())
    }
}
