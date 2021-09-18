package org.fnives.test.showcase.core.login

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Disabled("CodeKata")
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
