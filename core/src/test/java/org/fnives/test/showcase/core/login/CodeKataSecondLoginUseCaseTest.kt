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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class CodeKataSecondLoginUseCaseTest {

    @BeforeEach
    fun setUp() {
        TODO()
    }

    @DisplayName("GIVEN empty username WHEN trying to login THEN invalid username is returned")
    @Test
    fun emptyUserNameReturnsLoginStatusError() {
        TODO()
    }

    @DisplayName("GIVEN empty password WHEN trying to login THEN invalid password is returned")
    @Test
    fun emptyPasswordNameReturnsLoginStatusError() {
        TODO()
    }

    @DisplayName("GIVEN invalid credentials response WHEN trying to login THEN invalid credentials is returned ")
    @Test
    fun invalidLoginResponseReturnInvalidCredentials() {
        TODO()
    }

    @DisplayName("GIVEN success response WHEN trying to login THEN session is saved and success is returned")
    @Test
    fun validResponseResultsInSavingSessionAndSuccessReturned() {
        TODO()
    }

    @DisplayName("GIVEN error resposne WHEN trying to login THEN session is not touched and error is returned")
    @Test
    fun invalidResponseResultsInErrorReturned() {
        TODO()
    }
}