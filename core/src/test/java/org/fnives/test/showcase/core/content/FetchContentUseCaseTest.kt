package org.fnives.test.showcase.core.content

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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

    @DisplayName("WHEN nothing happens THEN the storage is not touched")
    @Test
    fun initializationDoesntAffectRepository() {
        verifyZeroInteractions(mockContentRepository)
    }

    @DisplayName("WHEN called THEN repository is called")
    @Test
    fun whenCalledRepositoryIsFetched() = runBlockingTest {
        sut.invoke()

        verify(mockContentRepository, times(1)).fetch()
        verifyNoMoreInteractions(mockContentRepository)
    }

    @DisplayName("GIVEN throwing local storage WHEN thrown THEN its thrown")
    @Test
    fun whenRepositoryThrowsUseCaseAlsoThrows() = runBlockingTest {
        whenever(mockContentRepository.fetch()).doThrow(RuntimeException())

        assertThrows(RuntimeException::class.java) {
            runBlocking { sut.invoke() }
        }
    }
}
