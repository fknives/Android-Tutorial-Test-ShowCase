package org.fnives.test.showcase.ui.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.fnives.test.showcase.core.login.LoginUseCase
import org.fnives.test.showcase.testutils.InstantExecutorExtension
import org.fnives.test.showcase.testutils.TestMainDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock

@ExtendWith(InstantExecutorExtension::class, TestMainDispatcher::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CodeKataAuthViewModel {

    private lateinit var sut: AuthViewModel
    private lateinit var mockLoginUseCase: LoginUseCase
    private val testScheduler get() = TestMainDispatcher.testDispatcher.scheduler

    @BeforeEach
    fun setUp() {
        mockLoginUseCase = mock()
        sut = AuthViewModel(mockLoginUseCase)
    }

    @DisplayName("GIVEN initialized viewModel WHEN observed THEN loading false other fields are empty")
    @Test
    fun initialSetup() {
    }

    @DisplayName("GIVEN password text WHEN onPasswordChanged is called THEN password livedata is updated")
    @Test
    fun whenPasswordChangedLiveDataIsUpdated() {
    }

    @DisplayName("GIVEN username text WHEN onUsernameChanged is called THEN username livedata is updated")
    @Test
    fun whenUsernameChangedLiveDataIsUpdated() {
    }

    @DisplayName("GIVEN no password or username WHEN login is Called THEN empty credentials are used in usecase")
    @Test
    fun noPasswordUsesEmptyStringInLoginUseCase() {
    }

    @DisplayName("WHEN login is called twice before finishing THEN use case is only called once")
    @Test
    fun onlyOneLoginIsSentOutWhenClickingRepeatedly() {
    }

    @DisplayName("GIVEN password and username WHEN login is called THEN proper credentials are used in usecase")
    @Test
    fun argumentsArePassedProperlyToLoginUseCase() {
    }

    @DisplayName("GIVEN AnswerError WHEN login called THEN error is shown")
    @Test
    fun loginUnexpectedErrorResultsInErrorState() {
    }

    @DisplayName("GIVEN answer success loginStatus INVALID_CREDENTIALS WHEN login called THEN error INVALID_CREDENTIALS is shown")
    @Test
    fun invalidStatusResultsInErrorState() {
    }

    @DisplayName("GIVEN answer success and login status success WHEN login called THEN navigation event is sent")
    @Test
    fun successLoginResultsInNavigation() {
    }
}
