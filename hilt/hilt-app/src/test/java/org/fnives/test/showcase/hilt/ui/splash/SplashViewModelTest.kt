package org.fnives.test.showcase.hilt.ui.splash

import com.jraska.livedata.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.fnives.test.showcase.android.testutil.InstantExecutorExtension
import org.fnives.test.showcase.android.testutil.StandardTestMainDispatcher
import org.fnives.test.showcase.hilt.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.hilt.ui.shared.Event
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(InstantExecutorExtension::class, StandardTestMainDispatcher::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class SplashViewModelTest {

    private lateinit var mockIsUserLoggedInUseCase: IsUserLoggedInUseCase
    private lateinit var sut: SplashViewModel
    private val testScheduler get() = StandardTestMainDispatcher.testDispatcher.scheduler

    @BeforeEach
    fun setUp() {
        mockIsUserLoggedInUseCase = mock()
        sut = SplashViewModel(mockIsUserLoggedInUseCase)
    }

    @DisplayName("GIVEN not logged in user WHEN splash started THEN after half a second navigated to authentication")
    @Test
    fun loggedOutUserGoesToAuthentication() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(false)
        val navigateToTestObserver = sut.navigateTo.test()

        testScheduler.advanceTimeBy(501)

        navigateToTestObserver.assertValueHistory(Event(SplashViewModel.NavigateTo.AUTHENTICATION))
    }

    @DisplayName("GIVEN logged in user WHEN splash started THEN after half a second navigated to home")
    @Test
    fun loggedInUserGoesToHome() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(true)
        val navigateToTestObserver = sut.navigateTo.test()

        testScheduler.advanceTimeBy(501)

        navigateToTestObserver.assertValueHistory(Event(SplashViewModel.NavigateTo.HOME))
    }

    @DisplayName("GIVEN not logged in user WHEN splash started THEN before half a second no event is sent")
    @Test
    fun withoutEnoughTimeNoNavigationHappens() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(false)
        val navigateToTestObserver = sut.navigateTo.test()

        testScheduler.advanceTimeBy(500)

        navigateToTestObserver.assertNoValue()
    }
}
