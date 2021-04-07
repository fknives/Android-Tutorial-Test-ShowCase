package org.fnives.test.showcase.core.login

import kotlinx.coroutines.test.runBlockingTest
import org.fnives.test.showcase.core.shared.UnexpectedException
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.network.auth.LoginRemoteSource
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
internal class LoginUseCaseTest {

    private lateinit var sut: LoginUseCase
    private lateinit var mockLoginRemoteSource: LoginRemoteSource
    private lateinit var mockUserDataLocalStorage: UserDataLocalStorage

    @BeforeEach
    fun setUp() {
        mockLoginRemoteSource = mock()
        mockUserDataLocalStorage = mock()
        sut = LoginUseCase(mockLoginRemoteSource, mockUserDataLocalStorage)
    }

    @Test
    fun GIVEN_empty_username_WHEN_trying_to_login_THEN_invalid_username_is_returned() = runBlockingTest {
        val expected = Answer.Success(LoginStatus.INVALID_USERNAME)

        val actual = sut.invoke(LoginCredentials("", "a"))

        Assertions.assertEquals(expected, actual)
        verifyZeroInteractions(mockLoginRemoteSource)
        verifyZeroInteractions(mockUserDataLocalStorage)
    }

    @Test
    fun GIVEN_empty_password_WHEN_trying_to_login_THEN_invalid_password_is_returned() = runBlockingTest {
        val expected = Answer.Success(LoginStatus.INVALID_PASSWORD)

        val actual = sut.invoke(LoginCredentials("a", ""))

        Assertions.assertEquals(expected, actual)
        verifyZeroInteractions(mockLoginRemoteSource)
        verifyZeroInteractions(mockUserDataLocalStorage)
    }

    @Test
    fun GIVEN_login_invalid_credentials_response_WHEN_trying_to_login_THEN_invalid_credentials_is_returned() = runBlockingTest {
        val expected = Answer.Success(LoginStatus.INVALID_CREDENTIALS)
        whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
            .doReturn(LoginStatusResponses.InvalidCredentials)

        val actual = sut.invoke(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
        verifyZeroInteractions(mockUserDataLocalStorage)
    }

    @Test
    fun GIVEN_valid_login_response_WHEN_trying_to_login_THEN_Success_is_returned() = runBlockingTest {
        val expected = Answer.Success(LoginStatus.SUCCESS)
        whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
            .doReturn(LoginStatusResponses.Success(Session("c", "d")))

        val actual = sut.invoke(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
        verify(mockUserDataLocalStorage, times(1)).session = Session("c", "d")
    }

    @Test
    fun GIVEN_throwing_remote_source_WHEN_trying_to_login_THEN_error_is_returned() = runBlockingTest {
        val exception = RuntimeException()
        val expected = Answer.Error<LoginStatus>(UnexpectedException(exception))
        whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
            .doThrow(exception)

        val actual = sut.invoke(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
        verifyZeroInteractions(mockUserDataLocalStorage)
    }
}
