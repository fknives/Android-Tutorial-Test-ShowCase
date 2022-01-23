package org.fnives.test.showcase.core.login.hilt

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.content.ContentRepository
import org.fnives.test.showcase.core.login.LogoutUseCase
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import javax.inject.Inject

@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
internal class LogoutUseCaseTest {

    @Inject
    lateinit var sut: LogoutUseCase
    private lateinit var mockUserDataLocalStorage: UserDataLocalStorage
    private lateinit var testCoreComponent: TestCoreComponent

    @Inject
    lateinit var contentRepository: ContentRepository

    @BeforeEach
    fun setUp() {
        mockUserDataLocalStorage = mock()
        testCoreComponent = DaggerTestCoreComponent.builder()
            .setBaseUrl("https://a.b.com")
            .setEnableLogging(true)
            .setSessionExpirationListener(mock())
            .setUserDataLocalStorage(mockUserDataLocalStorage)
            .build()
        testCoreComponent.inject(this)
    }

    @DisplayName("WHEN no call THEN storage is not interacted")
    @Test
    fun initializedDoesntAffectStorage() {
        verifyZeroInteractions(mockUserDataLocalStorage)
    }

    @DisplayName("WHEN logout invoked THEN storage is cleared")
    @Test
    fun logoutResultsInStorageCleaning() = runTest {
        val repositoryBefore = contentRepository

        sut.invoke()

        testCoreComponent.inject(this@LogoutUseCaseTest)
        val repositoryAfter = contentRepository
        verify(mockUserDataLocalStorage, times(1)).session = null
        verifyNoMoreInteractions(mockUserDataLocalStorage)
        Assertions.assertNotSame(repositoryBefore, repositoryAfter)
    }
}
