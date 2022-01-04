package org.fnives.test.showcase.ui.home

import com.jraska.livedata.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.core.content.AddContentToFavouriteUseCase
import org.fnives.test.showcase.core.content.FetchContentUseCase
import org.fnives.test.showcase.core.content.GetAllContentUseCase
import org.fnives.test.showcase.core.content.RemoveContentFromFavouritesUseCase
import org.fnives.test.showcase.core.login.LogoutUseCase
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.model.content.ImageUrl
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.testutils.InstantExecutorExtension
import org.fnives.test.showcase.testutils.TestMainDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
@ExtendWith(InstantExecutorExtension::class, TestMainDispatcher::class)
internal class MainViewModelTest {

    private lateinit var sut: MainViewModel
    private lateinit var mockGetAllContentUseCase: GetAllContentUseCase
    private lateinit var mockLogoutUseCase: LogoutUseCase
    private lateinit var mockFetchContentUseCase: FetchContentUseCase
    private lateinit var mockAddContentToFavouriteUseCase: AddContentToFavouriteUseCase
    private lateinit var mockRemoveContentFromFavouritesUseCase: RemoveContentFromFavouritesUseCase
    private val testDispatcher get() = TestMainDispatcher.testDispatcher

    @BeforeEach
    fun setUp() {
        mockGetAllContentUseCase = mock()
        mockLogoutUseCase = mock()
        mockFetchContentUseCase = mock()
        mockAddContentToFavouriteUseCase = mock()
        mockRemoveContentFromFavouritesUseCase = mock()
        testDispatcher.pauseDispatcher()
        sut = MainViewModel(
            getAllContentUseCase = mockGetAllContentUseCase,
            logoutUseCase = mockLogoutUseCase,
            fetchContentUseCase = mockFetchContentUseCase,
            addContentToFavouriteUseCase = mockAddContentToFavouriteUseCase,
            removeContentFromFavouritesUseCase = mockRemoveContentFromFavouritesUseCase
        )
    }

    @DisplayName("WHEN initialization THEN error false other states empty")
    @Test
    fun initialStateIsCorrect() {
        sut.errorMessage.test().assertValue(false)
        sut.content.test().assertNoValue()
        sut.loading.test().assertNoValue()
        sut.navigateToAuth.test().assertNoValue()
    }

    @DisplayName("GIVEN initialized viewModel WHEN loading is returned THEN loading is shown")
    @Test
    fun loadingDataShowsInLoadingUIState() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading()))
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.errorMessage.test().assertValue(false)
        sut.content.test().assertNoValue()
        sut.loading.test().assertValue(true)
        sut.navigateToAuth.test().assertNoValue()
    }

    @DisplayName("GIVEN loading then data WHEN observing content THEN proper states are shown")
    @Test
    fun loadingThenLoadedDataResultsInProperUIStates() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading(), Resource.Success(emptyList())))
        val errorMessageTestObserver = sut.errorMessage.test()
        val contentTestObserver = sut.content.test()
        val loadingTestObserver = sut.loading.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        errorMessageTestObserver.assertValueHistory(false)
        contentTestObserver.assertValueHistory(listOf())
        loadingTestObserver.assertValueHistory(true, false)
        sut.navigateToAuth.test().assertNoValue()
    }

    @DisplayName("GIVEN loading then error WHEN observing content THEN proper states are shown")
    @Test
    fun loadingThenErrorResultsInProperUIStates() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading(), Resource.Error(Throwable())))
        val errorMessageTestObserver = sut.errorMessage.test()
        val contentTestObserver = sut.content.test()
        val loadingTestObserver = sut.loading.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        errorMessageTestObserver.assertValueHistory(false, true)
        contentTestObserver.assertValueHistory(emptyList())
        loadingTestObserver.assertValueHistory(true, false)
        sut.navigateToAuth.test().assertNoValue()
    }

    @DisplayName("GIVEN loading then error then loading then data WHEN observing content THEN proper states are shown")
    @Test
    fun loadingThenErrorThenLoadingThenDataResultsInProperUIStates() {
        val content = listOf(
            FavouriteContent(Content(ContentId(""), "", "", ImageUrl("")), false)
        )
        whenever(mockGetAllContentUseCase.get()).doReturn(
            flowOf(
                Resource.Loading(),
                Resource.Error(Throwable()),
                Resource.Loading(),
                Resource.Success(content)
            )
        )
        val errorMessageTestObserver = sut.errorMessage.test()
        val contentTestObserver = sut.content.test()
        val loadingTestObserver = sut.loading.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        errorMessageTestObserver.assertValueHistory(false, true, false)
        contentTestObserver.assertValueHistory(emptyList(), content)
        loadingTestObserver.assertValueHistory(true, false, true, false)
        sut.navigateToAuth.test().assertNoValue()
    }

    @DisplayName("GIVEN loading viewModel WHEN refreshing THEN usecase is not called")
    @Test
    fun fetchIsIgnoredIfViewModelIsStillLoading() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading()))
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onRefresh()
        testDispatcher.advanceUntilIdle()

        verifyZeroInteractions(mockFetchContentUseCase)
    }

    @DisplayName("GIVEN non loading viewModel WHEN refreshing THEN usecase is called")
    @Test
    fun fetchIsCalledIfViewModelIsLoaded() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf())
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onRefresh()
        testDispatcher.advanceUntilIdle()

        verify(mockFetchContentUseCase, times(1)).invoke()
        verifyNoMoreInteractions(mockFetchContentUseCase)
    }

    @DisplayName("GIVEN loading viewModel WHEN loging out THEN usecase is called")
    @Test
    fun loadingViewModelStillCalsLogout() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading()))
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onLogout()
        testDispatcher.advanceUntilIdle()

        runBlocking { verify(mockLogoutUseCase, times(1)).invoke() }
        verifyNoMoreInteractions(mockLogoutUseCase)
    }

    @DisplayName("GIVEN non loading viewModel WHEN loging out THEN usecase is called")
    @Test
    fun nonLoadingViewModelStillCalsLogout() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf())
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onLogout()
        testDispatcher.advanceUntilIdle()

        runBlocking { verify(mockLogoutUseCase, times(1)).invoke() }
        verifyNoMoreInteractions(mockLogoutUseCase)
    }

    @DisplayName("GIVEN success content list viewModel WHEN toggling a nonexistent contentId THEN nothing happens")
    @Test
    fun interactionWithNonExistentContentIdIsIgnored() {
        val contents = listOf(
            FavouriteContent(Content(ContentId("a"), "", "", ImageUrl("")), false),
            FavouriteContent(Content(ContentId("b"), "", "", ImageUrl("")), true)
        )
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Success(contents)))
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onFavouriteToggleClicked(ContentId("c"))
        testDispatcher.advanceUntilIdle()

        verifyZeroInteractions(mockRemoveContentFromFavouritesUseCase)
        verifyZeroInteractions(mockAddContentToFavouriteUseCase)
    }

    @DisplayName("GIVEN success content list viewModel WHEN toggling a favourite contentId THEN remove favourite usecase is called")
    @Test
    fun togglingFavouriteContentCallsRemoveFromFavourite() {
        val contents = listOf(
            FavouriteContent(Content(ContentId("a"), "", "", ImageUrl("")), false),
            FavouriteContent(Content(ContentId("b"), "", "", ImageUrl("")), true)
        )
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Success(contents)))
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onFavouriteToggleClicked(ContentId("b"))
        testDispatcher.advanceUntilIdle()

        runBlocking { verify(mockRemoveContentFromFavouritesUseCase, times(1)).invoke(ContentId("b")) }
        verifyNoMoreInteractions(mockRemoveContentFromFavouritesUseCase)
        verifyZeroInteractions(mockAddContentToFavouriteUseCase)
    }

    @DisplayName("GIVEN success content list viewModel WHEN toggling a not favourite contentId THEN add favourite usecase is called")
    @Test
    fun togglingNonFavouriteContentCallsAddToFavourite() {
        val contents = listOf(
            FavouriteContent(Content(ContentId("a"), "", "", ImageUrl("")), false),
            FavouriteContent(Content(ContentId("b"), "", "", ImageUrl("")), true)
        )
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Success(contents)))
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onFavouriteToggleClicked(ContentId("a"))
        testDispatcher.advanceUntilIdle()

        verifyZeroInteractions(mockRemoveContentFromFavouritesUseCase)
        runBlocking { verify(mockAddContentToFavouriteUseCase, times(1)).invoke(ContentId("a")) }
        verifyNoMoreInteractions(mockAddContentToFavouriteUseCase)
    }
}
