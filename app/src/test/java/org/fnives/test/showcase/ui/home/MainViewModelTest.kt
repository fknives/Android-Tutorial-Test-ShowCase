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

    @Test
    fun WHEN_initialization_THEN_error_false_other_states_empty() {
        sut.errorMessage.test().assertValue(false)
        sut.content.test().assertNoValue()
        sut.loading.test().assertNoValue()
        sut.navigateToAuth.test().assertNoValue()
    }

    @Test
    fun GIVEN_initialized_viewModel_WHEN_loading_is_returned_THEN_loading_is_shown() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading()))
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.errorMessage.test().assertValue(false)
        sut.content.test().assertNoValue()
        sut.loading.test().assertValue(true)
        sut.navigateToAuth.test().assertNoValue()
    }

    @Test
    fun GIVEN_loading_then_data_WHEN_observing_content_THEN_proper_states_are_shown() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading(), Resource.Success(emptyList())))
        val errorMessageTestObserver = sut.errorMessage.test()
        val contentTestObserver = sut.content.test()
        val loadingTestObserver = sut.loading.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        errorMessageTestObserver.assertValueHistory(false, false, false)
        contentTestObserver.assertValueHistory(listOf())
        loadingTestObserver.assertValueHistory(true, false)
        sut.navigateToAuth.test().assertNoValue()
    }

    @Test
    fun GIVEN_loading_then_error_WHEN_observing_content_THEN_proper_states_are_shown() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading(), Resource.Error(Throwable())))
        val errorMessageTestObserver = sut.errorMessage.test()
        val contentTestObserver = sut.content.test()
        val loadingTestObserver = sut.loading.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        errorMessageTestObserver.assertValueHistory(false, false, true)
        contentTestObserver.assertValueHistory(emptyList())
        loadingTestObserver.assertValueHistory(true, false)
        sut.navigateToAuth.test().assertNoValue()
    }

    @Test
    fun GIVEN_loading_then_error_then_loading_then_data_WHEN_observing_content_THEN_proper_states_are_shown() {
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

        errorMessageTestObserver.assertValueHistory(false, false, true, false, false)
        contentTestObserver.assertValueHistory(emptyList(), content)
        loadingTestObserver.assertValueHistory(true, false, true, false)
        sut.navigateToAuth.test().assertNoValue()
    }

    @Test
    fun GIVEN_loading_viewModel_WHEN_refreshing_THEN_usecase_is_not_called() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading()))
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onRefresh()
        testDispatcher.advanceUntilIdle()

        verifyZeroInteractions(mockFetchContentUseCase)
    }

    @Test
    fun GIVEN_non_loading_viewModel_WHEN_refreshing_THEN_usecase_is_called() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf())
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onRefresh()
        testDispatcher.advanceUntilIdle()

        verify(mockFetchContentUseCase, times(1)).invoke()
        verifyNoMoreInteractions(mockFetchContentUseCase)
    }

    @Test
    fun GIVEN_loading_viewModel_WHEN_loging_out_THEN_usecase_is_called() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf(Resource.Loading()))
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onLogout()
        testDispatcher.advanceUntilIdle()

        runBlocking { verify(mockLogoutUseCase, times(1)).invoke() }
        verifyNoMoreInteractions(mockLogoutUseCase)
    }

    @Test
    fun GIVEN_non_loading_viewModel_WHEN_loging_out_THEN_usecase_is_called() {
        whenever(mockGetAllContentUseCase.get()).doReturn(flowOf())
        sut.content.test()
        testDispatcher.resumeDispatcher()
        testDispatcher.advanceUntilIdle()

        sut.onLogout()
        testDispatcher.advanceUntilIdle()

        runBlocking { verify(mockLogoutUseCase, times(1)).invoke() }
        verifyNoMoreInteractions(mockLogoutUseCase)
    }

    @Test
    fun GIVEN_success_content_list_viewModel_WHEN_toggling_a_nonexistent_contentId_THEN_nothing_happens() {
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

    @Test
    fun GIVEN_success_content_list_viewModel_WHEN_toggling_a_favourite_contentId_THEN_remove_favourite_usecase_is_called() {
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

    @Test
    fun GIVEN_success_content_list_viewModel_WHEN_toggling_a_not_favourite_contentId_THEN_add_favourite_usecase_is_called() {
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
