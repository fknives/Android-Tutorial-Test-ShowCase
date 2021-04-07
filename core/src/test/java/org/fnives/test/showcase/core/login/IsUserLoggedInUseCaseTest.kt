package org.fnives.test.showcase.core.login

import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.session.Session
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyZeroInteractions
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

    @Test
    fun WHEN_nothing_is_called_THEN_storage_is_not_called() {
        verifyZeroInteractions(mockUserDataLocalStorage)
    }

    @Test
    fun GIVEN_session_data_saved_WHEN_is_user_logged_in_checked_THEN_true_is_returned() {
        whenever(mockUserDataLocalStorage.session).doReturn(Session("a", "b"))

        val actual = sut.invoke()

        Assertions.assertEquals(true, actual)
    }

    @Test
    fun GIVEN_no_session_data_saved_WHEN_is_user_logged_in_checked_THEN_false_is_returned() {
        whenever(mockUserDataLocalStorage.session).doReturn(null)

        val actual = sut.invoke()

        Assertions.assertEquals(false, actual)
    }

    @Test
    fun GIVEN_no_session_THEN_session_THEN_no_session_WHEN_is_user_logged_in_checked_over_again_THEN_every_return_is_correct() {
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
