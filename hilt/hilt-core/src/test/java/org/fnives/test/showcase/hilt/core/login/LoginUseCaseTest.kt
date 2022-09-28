package org.fnives.test.showcase.hilt.core.login

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.hilt.core.shared.UnexpectedException
import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.hilt.network.auth.LoginRemoteSource
import org.fnives.test.showcase.hilt.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.model.shared.Answer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
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

    @DisplayName("GIVEN empty username WHEN trying to login THEN invalid username is returned")
    @Test
    fun emptyUserNameReturnsLoginStatusError() = runTest {
        val expected = Answer.Success(LoginStatus.INVALID_USERNAME)

        val actual = sut.invoke(LoginCredentials("", "a"))

        Assertions.assertEquals(expected, actual)
        verifyNoInteractions(mockLoginRemoteSource)
        verifyNoInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("GIVEN empty password WHEN trying to login THEN invalid password is returned")
    @Test
    fun emptyPasswordNameReturnsLoginStatusError() = runTest {
        val expected = Answer.Success(LoginStatus.INVALID_PASSWORD)

        val actual = sut.invoke(LoginCredentials("a", ""))

        Assertions.assertEquals(expected, actual)
        verifyNoInteractions(mockLoginRemoteSource)
        verifyNoInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("GIVEN invalid credentials response WHEN trying to login THEN invalid credentials is returned ")
    @Test
    fun invalidLoginResponseReturnInvalidCredentials() = runTest {
        val expected = Answer.Success(LoginStatus.INVALID_CREDENTIALS)
        whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
            .doReturn(LoginStatusResponses.InvalidCredentials)

        val actual = sut.invoke(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
        verifyNoInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("GIVEN success response WHEN trying to login THEN session is saved and success is returned")
    @Test
    fun validResponseResultsInSavingSessionAndSuccessReturned() = runTest {
        val expected = Answer.Success(LoginStatus.SUCCESS)
        whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
            .doReturn(LoginStatusResponses.Success(Session("c", "d")))

        val actual = sut.invoke(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
        verify(mockUserDataLocalStorage, times(1)).session = Session("c", "d")
        verifyNoMoreInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("GIVEN error response WHEN trying to login THEN session is not touched and error is returned")
    @Test
    fun invalidResponseResultsInErrorReturned() = runTest {
        val exception = RuntimeException()
        val expected = Answer.Error<LoginStatus>(UnexpectedException(exception))
        whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
            .doThrow(exception)

        val actual = sut.invoke(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
        verifyNoInteractions(mockUserDataLocalStorage)
    }
}
