package org.fnives.test.showcase.ui.splash

import com.jraska.livedata.test
import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.testutils.InstantExecutorExtension
import org.fnives.test.showcase.testutils.TestMainDispatcher
import org.fnives.test.showcase.ui.shared.Event
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
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

    @Test
    fun GIVEN_not_logged_in_user_WHEN_splash_started_THEN_after_half_a_second_navigated_to_authentication() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(false)

        testCoroutineDispatcher.advanceTimeBy(500)

        sut.navigateTo.test().assertValue(Event(SplashViewModel.NavigateTo.AUTHENTICATION))
    }

    @Test
    fun GIVEN_logged_in_user_WHEN_splash_started_THEN_after_half_a_second_navigated_to_home() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(true)

        testCoroutineDispatcher.advanceTimeBy(500)

        sut.navigateTo.test().assertValue(Event(SplashViewModel.NavigateTo.HOME))
    }

    @Test
    fun GIVEN_not_logged_in_user_WHEN_splash_started_THEN_before_half_a_second_no_event_is_sent() {
        whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(false)

        testCoroutineDispatcher.advanceTimeBy(100)

        sut.navigateTo.test().assertNoValue()
    }
}
