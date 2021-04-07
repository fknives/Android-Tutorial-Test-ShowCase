package org.fnives.test.showcase.core.content

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
internal class FetchContentUseCaseTest {

    private lateinit var sut: FetchContentUseCase
    private lateinit var mockContentRepository: ContentRepository

    @BeforeEach
    fun setUp() {
        mockContentRepository = mock()
        sut = FetchContentUseCase(mockContentRepository)
    }

    @Test
    fun WHEN_nothing_happens_THEN_the_storage_is_not_touched() {
        verifyZeroInteractions(mockContentRepository)
    }

    @Test
    fun WHEN_called_THEN_repository_is_called() = runBlockingTest {
        sut.invoke()

        verify(mockContentRepository, times(1)).fetch()
        verifyNoMoreInteractions(mockContentRepository)
    }

    @Test
    fun GIVEN_throwing_local_storage_WHEN_thrown_THEN_its_thrown() = runBlockingTest {
        whenever(mockContentRepository.fetch()).doThrow(RuntimeException())

        assertThrows(RuntimeException::class.java) {
            runBlocking { sut.invoke() }
        }
    }
}
