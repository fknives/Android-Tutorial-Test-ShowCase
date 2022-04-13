package org.fnives.test.showcase.core.content

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
internal class RemoveContentFromFavouritesUseCaseTest {

    private lateinit var sut: RemoveContentFromFavouritesUseCase
    private lateinit var mockFavouriteContentLocalStorage: FavouriteContentLocalStorage

    @BeforeEach
    fun setUp() {
        mockFavouriteContentLocalStorage = mock()
        sut = RemoveContentFromFavouritesUseCase(mockFavouriteContentLocalStorage)
    }

    @DisplayName("WHEN nothing happens THEN the storage is not touched")
    @Test
    fun initializationDoesntAffectStorage() {
        verifyNoInteractions(mockFavouriteContentLocalStorage)
    }

    @DisplayName("GIVEN contentId WHEN called THEN storage is called")
    @Test
    fun givenContentIdCallsStorage() = runTest {
        sut.invoke(ContentId("a"))

        verify(mockFavouriteContentLocalStorage, times(1)).deleteAsFavourite(ContentId("a"))
        verifyNoMoreInteractions(mockFavouriteContentLocalStorage)
    }

    @DisplayName("GIVEN throwing local storage WHEN thrown THEN its propogated")
    @Test
    fun storageExceptionThrowingIsPropogated() = runTest {
        whenever(mockFavouriteContentLocalStorage.deleteAsFavourite(ContentId("a"))).doThrow(RuntimeException())

        Assertions.assertThrows(RuntimeException::class.java) {
            runBlocking { sut.invoke(ContentId("a")) }
        }
    }
}
