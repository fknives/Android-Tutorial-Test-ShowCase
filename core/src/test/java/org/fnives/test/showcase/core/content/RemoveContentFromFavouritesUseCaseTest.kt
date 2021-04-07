package org.fnives.test.showcase.core.content

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
import org.junit.jupiter.api.Assertions
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
internal class RemoveContentFromFavouritesUseCaseTest {

    private lateinit var sut: RemoveContentFromFavouritesUseCase
    private lateinit var mockFavouriteContentLocalStorage: FavouriteContentLocalStorage

    @BeforeEach
    fun setUp() {
        mockFavouriteContentLocalStorage = mock()
        sut = RemoveContentFromFavouritesUseCase(mockFavouriteContentLocalStorage)
    }

    @Test
    fun WHEN_nothing_happens_THEN_the_storage_is_not_touched() {
        verifyZeroInteractions(mockFavouriteContentLocalStorage)
    }

    @Test
    fun GIVEN_contentId_WHEN_called_THEN_storage_is_called() = runBlockingTest {
        sut.invoke(ContentId("a"))

        verify(mockFavouriteContentLocalStorage, times(1)).deleteAsFavourite(ContentId("a"))
        verifyNoMoreInteractions(mockFavouriteContentLocalStorage)
    }

    @Test
    fun GIVEN_throwing_local_storage_WHEN_thrown_THEN_its_thrown() = runBlockingTest {
        whenever(mockFavouriteContentLocalStorage.deleteAsFavourite(ContentId("a"))).doThrow(RuntimeException())

        Assertions.assertThrows(RuntimeException::class.java) {
            runBlocking { sut.invoke(ContentId("a")) }
        }
    }
}
