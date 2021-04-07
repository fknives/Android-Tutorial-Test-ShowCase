package org.fnives.test.showcase.core.storage

import org.fnives.test.showcase.model.session.Session
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
internal class NetworkSessionLocalStorageAdapterTest {

    private lateinit var sut: NetworkSessionLocalStorageAdapter
    private lateinit var mockUserDataLocalStorage: UserDataLocalStorage

    @BeforeEach
    fun setUp() {
        mockUserDataLocalStorage = mock()
        sut = NetworkSessionLocalStorageAdapter(mockUserDataLocalStorage)
    }

    @Test
    fun GIVEN_null_as_session_WHEN_saved_THEN_its_delegated() {
        sut.session = null

        verify(mockUserDataLocalStorage, times(1)).session = null
        verifyNoMoreInteractions(mockUserDataLocalStorage)
    }

    @Test
    fun GIVEN_session_WHEN_saved_THEN_its_delegated() {
        val expected = Session("a", "b")

        sut.session = Session("a", "b")

        verify(mockUserDataLocalStorage, times(1)).session = expected
        verifyNoMoreInteractions(mockUserDataLocalStorage)
    }

    @Test
    fun WHEN_session_requested_THEN_its_returned_from_delegated() {
        val expected = Session("a", "b")
        whenever(mockUserDataLocalStorage.session).doReturn(expected)

        val actual = sut.session

        Assertions.assertSame(expected, actual)
        verify(mockUserDataLocalStorage, times(1)).session
        verifyNoMoreInteractions(mockUserDataLocalStorage)
    }
}
