package org.fnives.test.showcase.hilt.core.content

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class TurbineGetAllContentUseCaseTest {

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
        val expected = listOf(Resource.Loading<List<FavouriteContent>>())

        sut.get().test {
            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
    }

    @DisplayName("GIVEN loading AND listOfFavourite WHEN observed THEN loading is shown")
    @Test
    fun loadingResourceWithFavouritesResultsInLoadingResource() = runTest {
        favouriteContentIdFlow.value = listOf(ContentId("a"))
        contentFlow.value = Resource.Loading()
        val expected = listOf(Resource.Loading<List<FavouriteContent>>())

        sut.get().test {
            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
    }

    @DisplayName("GIVEN error AND empty favourite WHEN observed THEN error is shown")
    @Test
    fun errorResourceWithNoFavouritesResultsInErrorResource() = runTest {
        favouriteContentIdFlow.value = emptyList()
        val exception = Throwable()
        contentFlow.value = Resource.Error(exception)
        val expected = listOf(Resource.Error<List<FavouriteContent>>(exception))

        sut.get().test {
            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
    }

    @DisplayName("GIVEN error AND listOfFavourite WHEN observed THEN error is shown")
    @Test
    fun errorResourceWithFavouritesResultsInErrorResource() = runTest {
        favouriteContentIdFlow.value = listOf(ContentId("b"))
        val exception = Throwable()
        contentFlow.value = Resource.Error(exception)
        val expected = listOf(Resource.Error<List<FavouriteContent>>(exception))

        sut.get().test {
            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
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
        val expected = listOf(Resource.Success(items))

        sut.get().test {
            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
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
        val expected = listOf(Resource.Success(items))

        sut.get().test {
            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
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
        val expected = listOf(Resource.Success(items))

        sut.get().test {
            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
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

        sut.get().test {
            contentFlow.value = Resource.Success(listOf(content))
            favouriteContentIdFlow.value = listOf(ContentId("a"))

            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
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

        sut.get().test {
            contentFlow.value = Resource.Success(listOf(content))
            favouriteContentIdFlow.value = emptyList()

            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
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

        sut.get().test {
            contentFlow.value = Resource.Success(listOf(content))
            contentFlow.value = Resource.Loading()

            expected.forEach { expectedItem ->
                Assertions.assertEquals(expectedItem, awaitItem())
            }
            Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
        }
    }
}
