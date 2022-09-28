package org.fnives.test.showcase.hilt.core.storage

import org.fnives.test.showcase.model.session.Session
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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

    @DisplayName("GIVEN null as session WHEN saved THEN its delegated")
    @Test
    fun settingNullSessionIsDelegated() {
        sut.session = null

        verify(mockUserDataLocalStorage, times(1)).session = null
        verifyNoMoreInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("GIVEN session WHEN saved THEN its delegated")
    @Test
    fun settingDataAsSessionIsDelegated() {
        val expected = Session("a", "b")

        sut.session = Session("a", "b")

        verify(mockUserDataLocalStorage, times(1)).session = expected
        verifyNoMoreInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("WHEN session requested THEN its returned from delegated")
    @Test
    fun gettingSessionReturnsFromDelegate() {
        val expected = Session("a", "b")
        whenever(mockUserDataLocalStorage.session).doReturn(expected)

        val actual = sut.session

        Assertions.assertSame(expected, actual)
        verify(mockUserDataLocalStorage, times(1)).session
        verifyNoMoreInteractions(mockUserDataLocalStorage)
    }
}
