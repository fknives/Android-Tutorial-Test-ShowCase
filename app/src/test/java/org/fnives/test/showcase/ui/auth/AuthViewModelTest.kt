package org.fnives.test.showcase.ui.auth

import com.jraska.livedata.test
import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.core.login.LoginUseCase
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.testutils.InstantExecutorExtension
import org.fnives.test.showcase.testutils.TestMainDispatcher
import org.fnives.test.showcase.ui.shared.Event
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.stream.Stream

@Suppress("TestFunctionName")
@ExtendWith(InstantExecutorExtension::class, TestMainDispatcher::class)
internal class AuthViewModelTest {

    private lateinit var sut: AuthViewModel
    private lateinit var mockLoginUseCase: LoginUseCase
    private val testDispatcher get() = TestMainDispatcher.testDispatcher

    @BeforeEach
    fun setUp() {
        mockLoginUseCase = mock()
        testDispatcher.pauseDispatcher()
        sut = AuthViewModel(mockLoginUseCase)
    }

    @Test
    fun GIVEN_initialized_viewModel_WHEN_observed_THEN_loading_false_other_fields_are_empty() {
        testDispatcher.resumeDispatcher()
        sut.username.test().assertNoValue()
        sut.password.test().assertNoValue()
        sut.loading.test().assertValue(false)
        sut.error.test().assertNoValue()
        sut.navigateToHome.test().assertNoValue()
    }

    @Test
    fun GIVEN_password_text_WHEN_onPasswordChanged_is_called_THEN_password_livedata_is_updated() {
        testDispatcher.resumeDispatcher()
        val passwordTestObserver = sut.password.test()

        sut.onPasswordChanged("a")
        sut.onPasswordChanged("al")

        passwordTestObserver.assertValueHistory("a", "al")
        sut.username.test().assertNoValue()
        sut.loading.test().assertValue(false)
        sut.error.test().assertNoValue()
        sut.navigateToHome.test().assertNoValue()
    }

    @Test
    fun GIVEN_username_text_WHEN_onUsernameChanged_is_called_THEN_username_livedata_is_updated() {
        testDispatcher.resumeDispatcher()
        val usernameTestObserver = sut.username.test()

        sut.onUsernameChanged("a")
        sut.onUsernameChanged("al")

        usernameTestObserver.assertValueHistory("a", "al")
        sut.password.test().assertNoValue()
        sut.loading.test().assertValue(false)
        sut.error.test().assertNoValue()
        sut.navigateToHome.test().assertNoValue()
    }

    @Test
    fun GIVEN_no_password_or_username_WHEN_login_is_Called_THEN_empty_credentials_are_used_in_usecase() {
        val loadingTestObserver = sut.loading.test()
        runBlocking {
            whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable()))
        }

        sut.onLogin()
        testDispatcher.advanceUntilIdle()

        loadingTestObserver.assertValueHistory(false, true, false)
        runBlocking { verify(mockLoginUseCase, times(1)).invoke(LoginCredentials("", "")) }
        verifyNoMoreInteractions(mockLoginUseCase)
    }

    @Test
    fun WHEN_login_is_Called_twise_THEN_use_case_is_only_called_once() {
        runBlocking { whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable())) }

        sut.onLogin()
        sut.onLogin()
        testDispatcher.advanceUntilIdle()

        runBlocking { verify(mockLoginUseCase, times(1)).invoke(LoginCredentials("", "")) }
        verifyNoMoreInteractions(mockLoginUseCase)
    }

    @Test
    fun GIVEN_password_and_username_WHEN_login_is_Called_THEN_empty_credentials_are_used_in_usecase() {
        runBlocking {
            whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable()))
        }
        sut.onPasswordChanged("pass")
        sut.onUsernameChanged("usr")

        sut.onLogin()
        testDispatcher.advanceUntilIdle()

        runBlocking {
            verify(mockLoginUseCase, times(1)).invoke(LoginCredentials("usr", "pass"))
        }
        verifyNoMoreInteractions(mockLoginUseCase)
    }

    @Test
    fun GIVEN_answer_error_WHEN_login_called_THEN_error_is_shown() {
        runBlocking {
            whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable()))
        }
        val loadingObserver = sut.loading.test()
        val errorObserver = sut.error.test()
        val navigateToHomeObserver = sut.navigateToHome.test()

        sut.onLogin()
        testDispatcher.advanceUntilIdle()

        loadingObserver.assertValueHistory(false, true, false)
        errorObserver.assertValueHistory(Event(AuthViewModel.ErrorType.GENERAL_NETWORK_ERROR))
        navigateToHomeObserver.assertNoValue()
    }

    @MethodSource("loginErrorStatusesArguments")
    @ParameterizedTest(name = "GIVEN_answer_success_loginStatus_{0}_WHEN_login_called_THEN_error_{1}_is_shown")
    fun GIVEN_answer_success_invalid_loginStatus_WHEN_login_called_THEN_error_is_shown(
        loginStatus: LoginStatus,
        errorType: AuthViewModel.ErrorType
    ) {
        runBlocking {
            whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Success(loginStatus))
        }
        val loadingObserver = sut.loading.test()
        val errorObserver = sut.error.test()
        val navigateToHomeObserver = sut.navigateToHome.test()

        sut.onLogin()
        testDispatcher.advanceUntilIdle()

        loadingObserver.assertValueHistory(false, true, false)
        errorObserver.assertValueHistory(Event(errorType))
        navigateToHomeObserver.assertNoValue()
    }

    @Test
    fun GIVEN_answer_success_login_status_success_WHEN_login_called_THEN_navigation_event_is_sent() {
        runBlocking {
            whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Success(LoginStatus.SUCCESS))
        }
        val loadingObserver = sut.loading.test()
        val errorObserver = sut.error.test()
        val navigateToHomeObserver = sut.navigateToHome.test()

        sut.onLogin()
        testDispatcher.advanceUntilIdle()

        loadingObserver.assertValueHistory(false, true, false)
        errorObserver.assertNoValue()
        navigateToHomeObserver.assertValueHistory(Event(Unit))
    }

    companion object {

        @JvmStatic
        fun loginErrorStatusesArguments(): Stream<Arguments?> = Stream.of(
            Arguments.of(LoginStatus.INVALID_CREDENTIALS, AuthViewModel.ErrorType.INVALID_CREDENTIALS),
            Arguments.of(LoginStatus.INVALID_PASSWORD, AuthViewModel.ErrorType.UNSUPPORTED_PASSWORD),
            Arguments.of(LoginStatus.INVALID_USERNAME, AuthViewModel.ErrorType.UNSUPPORTED_USERNAME)
        )
    }
}
