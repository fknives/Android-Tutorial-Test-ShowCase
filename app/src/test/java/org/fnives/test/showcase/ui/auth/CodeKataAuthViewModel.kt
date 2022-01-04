package org.fnives.test.showcase.ui.auth

import org.fnives.test.showcase.core.login.LoginUseCase
import org.fnives.test.showcase.testutils.InstantExecutorExtension
import org.fnives.test.showcase.testutils.TestMainDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock

@Disabled("CodeKata")
@ExtendWith(InstantExecutorExtension::class, TestMainDispatcher::class)
class CodeKataAuthViewModel {

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
}
