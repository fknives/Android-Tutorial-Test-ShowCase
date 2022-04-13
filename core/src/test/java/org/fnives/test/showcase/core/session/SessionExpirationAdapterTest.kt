package org.fnives.test.showcase.core.session

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions

@Suppress("TestFunctionName")
internal class SessionExpirationAdapterTest {

    private lateinit var sut: SessionExpirationAdapter
    private lateinit var mockSessionExpirationListener: SessionExpirationListener

    @BeforeEach
    fun setUp() {
        mockSessionExpirationListener = mock()
        sut = SessionExpirationAdapter(mockSessionExpirationListener)
    }

    @DisplayName("WHEN nothing is changed THEN delegate is not touched")
    @Test
    fun verifyNoInteractionsIfNoInvocations() {
        verifyNoInteractions(mockSessionExpirationListener)
    }

    @DisplayName("WHEN onSessionExpired is called THEN delegated is also called")
    @Test
    fun verifyOnSessionExpirationIsDelegated() {
        sut.onSessionExpired()

        verify(mockSessionExpirationListener, times(1)).onSessionExpired()
        verifyNoMoreInteractions(mockSessionExpirationListener)
    }
}
