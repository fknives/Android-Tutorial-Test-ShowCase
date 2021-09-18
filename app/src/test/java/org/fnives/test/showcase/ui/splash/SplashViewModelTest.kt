package org.fnives.test.showcase.ui.splash

import com.jraska.livedata.test
import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.testutils.InstantExecutorExtension
import org.fnives.test.showcase.testutils.TestMainDispatcher
import org.fnives.test.showcase.ui.shared.Event
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(InstantExecutorExtension::class, TestMainDispatcher::class)
internal class SplashViewModelTest {

    private lateinit var mockIsUserLoggedInUseCase: IsUserLoggedInUseCase
    private lateinit var sut: SplashViewModel
    private val testCoroutineDispatcher get() = TestMainDispatcher.testDispatcher

    @BeforeEach
    fun setUp() {
        mockIsUserLoggedInUseCase = mock()
        sut = SplashViewModel(mockIsUserLoggedInUseCase)
    }

    @DisplayName("GIVEN not logged in user WHEN splash started THEN after half a second navigated to authentication")
    @Test
    fun loggedOutUserGoesToAuthentication() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(false)

        testCoroutineDispatcher.advanceTimeBy(500)

        sut.navigateTo.test().assertValue(Event(SplashViewModel.NavigateTo.AUTHENTICATION))
    }

    @DisplayName("GIVEN logged in user WHEN splash started THEN after half a second navigated to home")
    @Test
    fun loggedInUserGoestoHome() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(true)

        testCoroutineDispatcher.advanceTimeBy(500)

        sut.navigateTo.test().assertValue(Event(SplashViewModel.NavigateTo.HOME))
    }

    @DisplayName("GIVEN not logged in user WHEN splash started THEN before half a second no event is sent")
    @Test
    fun withoutEnoughTimeNoNavigationHappens() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(false)

        testCoroutineDispatcher.advanceTimeBy(100)

        sut.navigateTo.test().assertNoValue()
    }
}
