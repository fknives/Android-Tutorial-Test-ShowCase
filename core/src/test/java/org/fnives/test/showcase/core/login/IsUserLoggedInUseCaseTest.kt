package org.fnives.test.showcase.core.login

import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.session.Session
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
internal class IsUserLoggedInUseCaseTest {

    private lateinit var sut: IsUserLoggedInUseCase
    private lateinit var mockUserDataLocalStorage: UserDataLocalStorage

    @BeforeEach
    fun setUp() {
        mockUserDataLocalStorage = mock()
        sut = IsUserLoggedInUseCase(mockUserDataLocalStorage)
    }

    @DisplayName("WHEN nothing is called THEN storage is not called")
    @Test
    fun creatingDoesntAffectStorage() {
        verifyNoInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("GIVEN session data saved WHEN is user logged in checked THEN true is returned")
    @Test
    fun sessionInStorageResultsInLoggedIn() {
        whenever(mockUserDataLocalStorage.session).doReturn(Session("a", "b"))

        val actual = sut.invoke()

        Assertions.assertEquals(true, actual)
    }

    @DisplayName("GIVEN no session data saved WHEN is user logged in checked THEN false is returned")
    @Test
    fun noSessionInStorageResultsInLoggedOut() {
        whenever(mockUserDataLocalStorage.session).doReturn(null)

        val actual = sut.invoke()

        Assertions.assertEquals(false, actual)
    }

    @DisplayName("GIVEN no session THEN session THEN no session WHEN is user logged in checked over again THEN every return is correct")
    @Test
    fun multipleSessionSettingsResultsInCorrectResponses() {
        whenever(mockUserDataLocalStorage.session).doReturn(null)
        val actual1 = sut.invoke()
        whenever(mockUserDataLocalStorage.session).doReturn(Session("", ""))
        val actual2 = sut.invoke()
        whenever(mockUserDataLocalStorage.session).doReturn(null)
        val actual3 = sut.invoke()

        Assertions.assertEquals(false, actual1)
        Assertions.assertEquals(true, actual2)
        Assertions.assertEquals(false, actual3)
    }
}
