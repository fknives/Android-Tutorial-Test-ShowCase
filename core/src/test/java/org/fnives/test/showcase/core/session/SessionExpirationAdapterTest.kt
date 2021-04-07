package org.fnives.test.showcase.core.session

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions

@Suppress("TestFunctionName")
internal class SessionExpirationAdapterTest {

    private lateinit var sut: SessionExpirationAdapter
    private lateinit var mockSessionExpirationListener: SessionExpirationListener

    @BeforeEach
    fun setUp() {
        mockSessionExpirationListener = mock()
        sut = SessionExpirationAdapter(mockSessionExpirationListener)
    }

    @Test
    fun WHEN_onSessionExpired_is_called_THEN_its_delegated() {
        sut.onSessionExpired()

        verify(mockSessionExpirationListener, times(1)).onSessionExpired()
        verifyNoMoreInteractions(mockSessionExpirationListener)
    }

    @Test
    fun WHEN_nothing_is_changed_THEN_delegate_is_not_touched() {
        verifyZeroInteractions(mockSessionExpirationListener)
    }
}
