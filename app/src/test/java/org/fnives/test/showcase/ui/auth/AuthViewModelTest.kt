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
import org.junit.jupiter.api.DisplayName
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
        sut = AuthViewModel(mockLoginUseCase)
    }

    @DisplayName("GIVEN initialized viewModel WHEN observed THEN loading false other fields are empty")
    @Test
    fun initialSetup() {
        testDispatcher.resumeDispatcher()

        sut.username.test().assertNoValue()
        sut.password.test().assertNoValue()
        sut.loading.test().assertValue(false)
        sut.error.test().assertNoValue()
        sut.navigateToHome.test().assertNoValue()
    }

    @DisplayName("GIVEN password text WHEN onPasswordChanged is called THEN password livedata is updated")
    @Test
    fun whenPasswordChangedLiveDataIsUpdated() {
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

    @DisplayName("GIVEN username text WHEN onUsernameChanged is called THEN username livedata is updated")
    @Test
    fun whenUsernameChangedLiveDataIsUpdated() {
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

    @DisplayName("GIVEN no password or username WHEN login is Called THEN empty credentials are used in usecase")
    @Test
    fun noPasswordUsesEmptyStringInLoginUseCase() {
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

    @DisplayName("WHEN login is called twice before finishing THEN use case is only called once")
    @Test
    fun onlyOneLoginIsSentOutWhenClickingRepeatedly() {
        runBlocking { whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable())) }

        sut.onLogin()
        sut.onLogin()
        testDispatcher.advanceUntilIdle()

        runBlocking { verify(mockLoginUseCase, times(1)).invoke(LoginCredentials("", "")) }
        verifyNoMoreInteractions(mockLoginUseCase)
    }

    @DisplayName("GIVEN password and username WHEN login is called THEN proper credentials are used in usecase")
    @Test
    fun argumentsArePassedProperlyToLoginUseCase() {
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

    @DisplayName("GIVEN AnswerError WHEN login called THEN error is shown")
    @Test
    fun loginErrorResultsInErrorState() {
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
    @ParameterizedTest(name = "GIVEN answer success loginStatus {0} WHEN login called THEN error {1} is shown")
    fun invalidStatusResultsInErrorState(
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

    @DisplayName("GIVEN answer success and login status success WHEN login called THEN navigation event is sent")
    @Test
    fun successLoginResultsInNavigation() {
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
