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

    @DisplayName("GIVEN_initialized_viewModel_WHEN_observed_THEN_loading_false_other_fields_are_empty")
    @Test
    fun initialSetup() {

    }

    @DisplayName("GIVEN_password_text_WHEN_onPasswordChanged_is_called_THEN_password_livedata_is_updated")
    @Test
    fun whenPasswordChangedLiveDataIsUpdated() {

    }

    @DisplayName("GIVEN_username_text_WHEN_onUsernameChanged_is_called_THEN_username_livedata_is_updated")
    @Test
    fun whenUsernameChangedLiveDataIsUpdated() {

    }

    @DisplayName("GIVEN_no_password_or_username_WHEN_login_is_Called_THEN_empty_credentials_are_used_in_usecase")
    @Test
    fun noPasswordUsesEmptyStringInLoginUseCase() {

    }
}