package org.fnives.test.showcase.core.content

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
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
internal class AddContentToFavouriteUseCaseTest {

    private lateinit var sut: AddContentToFavouriteUseCase
    private lateinit var mockFavouriteContentLocalStorage: FavouriteContentLocalStorage

    @BeforeEach
    fun setUp() {
        mockFavouriteContentLocalStorage = mock()
        sut = AddContentToFavouriteUseCase(mockFavouriteContentLocalStorage)
    }

    @DisplayName("WHEN_nothing_happens_THEN_the_storage_is_not_touched")
    @Test
    fun initializationDoesntAffectStorage() {
        verifyZeroInteractions(mockFavouriteContentLocalStorage)
    }

    @DisplayName("GIVEN_contentId_WHEN_called_THEN_storage_is_called")
    @Test
    fun contentIdIsDelegatedToStorage() = runBlockingTest {
        sut.invoke(ContentId("a"))

        verify(mockFavouriteContentLocalStorage, times(1)).markAsFavourite(ContentId("a"))
        verifyNoMoreInteractions(mockFavouriteContentLocalStorage)
    }

    @DisplayName("GIVEN_throwing_local_storage_WHEN_thrown_THEN_its_propagated")
    @Test
    fun storageThrowingIsPropagated() = runBlockingTest {
        whenever(mockFavouriteContentLocalStorage.markAsFavourite(ContentId("a"))).doThrow(
            RuntimeException()
        )

        assertThrows(RuntimeException::class.java) {
            runBlocking { sut.invoke(ContentId("a")) }
        }
    }
}
