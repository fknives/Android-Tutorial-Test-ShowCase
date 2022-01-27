package org.fnives.test.showcase.core.login

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.content.ContentRepository
import org.fnives.test.showcase.core.di.createCoreModule
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.network.BaseUrl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
internal class LogoutUseCaseTest : KoinTest {

    private lateinit var sut: LogoutUseCase
    private lateinit var mockUserDataLocalStorage: UserDataLocalStorage

    @BeforeEach
    fun setUp() {
        mockUserDataLocalStorage = mock()
        sut = LogoutUseCase(mockUserDataLocalStorage)
        startKoin {
            modules(
                createCoreModule(
                    baseUrl = BaseUrl("https://a.b.com"),
                    enableNetworkLogging = true,
                    favouriteContentLocalStorageProvider = { mock() },
                    sessionExpirationListenerProvider = { mock() },
                    userDataLocalStorageProvider = { mock() }
                ).toList()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @DisplayName("WHEN no call THEN storage is not interacted")
    @Test
    fun initializedDoesntAffectStorage() {
        verifyZeroInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("WHEN logout invoked THEN storage is cleared")
    @Test
    fun logoutResultsInStorageCleaning() = runTest {
        val repositoryBefore = getKoin().get<ContentRepository>()

        sut.invoke()

        val repositoryAfter = getKoin().get<ContentRepository>()
        verify(mockUserDataLocalStorage, times(1)).session = null
        verifyNoMoreInteractions(mockUserDataLocalStorage)
        Assertions.assertNotSame(repositoryBefore, repositoryAfter)
    }
}
