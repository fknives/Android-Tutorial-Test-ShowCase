package org.fnives.test.showcase.core.content

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
import org.junit.jupiter.api.Assertions
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

    @DisplayName("WHEN nothing happens THEN the storage is not touched")
    @Test
    fun initializationDoesntAffectStorage() {
        verifyZeroInteractions(mockFavouriteContentLocalStorage)
    }

    @DisplayName("GIVEN contentId WHEN called THEN storage is called")
    @Test
    fun contentIdIsDelegatedToStorage() = runTest {
        sut.invoke(ContentId("a"))

        verify(mockFavouriteContentLocalStorage, times(1)).markAsFavourite(ContentId("a"))
        verifyNoMoreInteractions(mockFavouriteContentLocalStorage)
    }

    @DisplayName("GIVEN throwing local storage WHEN thrown THEN its propagated")
    @Test
    fun storageThrowingIsPropagated() = runTest {
        whenever(mockFavouriteContentLocalStorage.markAsFavourite(ContentId("a"))).doThrow(
            RuntimeException()
        )

        assertThrows(RuntimeException::class.java) {
            runBlocking { sut.invoke(ContentId("a")) }
        }
    }
}
