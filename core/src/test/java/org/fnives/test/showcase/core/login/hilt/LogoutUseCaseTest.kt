package org.fnives.test.showcase.core.login.hilt

import kotlinx.coroutines.test.runBlockingTest
import org.fnives.test.showcase.core.content.ContentRepository
import org.fnives.test.showcase.core.di.koin.createCoreModule
import org.fnives.test.showcase.core.login.LogoutUseCase
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.model.network.BaseUrl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import javax.inject.Inject

@Suppress("TestFunctionName")
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

    @Test
    fun WHEN_no_call_THEN_storage_is_not_interacted() {
        verifyZeroInteractions(mockUserDataLocalStorage)
    }

    @Test
    fun WHEN_logout_invoked_THEN_storage_is_cleared() = runBlockingTest {
        val repositoryBefore = contentRepository

        sut.invoke()

        testCoreComponent.inject(this@LogoutUseCaseTest)
        val repositoryAfter = contentRepository
        verify(mockUserDataLocalStorage, times(1)).session = null
        verifyNoMoreInteractions(mockUserDataLocalStorage)
        Assertions.assertNotSame(repositoryBefore, repositoryAfter)
    }
}
