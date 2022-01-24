package org.fnives.test.showcase.network.content

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.network.di.createNetworkModules
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.fnives.test.showcase.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.shared.MockServerScenarioSetupExtensions
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
class SessionExpirationTest : KoinTest {

    private val sut: ContentRemoteSourceImpl by inject()

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup
    private lateinit var mockNetworkSessionLocalStorage: NetworkSessionLocalStorage
    private lateinit var mockNetworkSessionExpirationListener: NetworkSessionExpirationListener

    @BeforeEach
    fun setUp() {
        mockNetworkSessionLocalStorage = mock()
        mockNetworkSessionExpirationListener = mock()
        startKoin {
            modules(
                createNetworkModules(
                    baseUrl = BaseUrl(mockServerScenarioSetupExtensions.url),
                    enableLogging = true,
                    networkSessionExpirationListenerProvider = { mockNetworkSessionExpirationListener },
                    networkSessionLocalStorageProvider = { mockNetworkSessionLocalStorage }
                ).toList()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @DisplayName("GIVEN 401 THEN refresh token ok response WHEN content requested THE tokens are refreshed and request retried with new tokens")
    @Test
    fun successRefreshResultsInRequestRetry() = runBlocking {
        var sessionToReturnByMock: Session? = ContentData.loginSuccessResponse
        mockServerScenarioSetup.setScenario(
            ContentScenario.Unauthorized(usingRefreshedToken = false)
                .then(ContentScenario.Success(usingRefreshedToken = true)),
            validateArguments = false
        )
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Success, validateArguments = false)
        whenever(mockNetworkSessionLocalStorage.session).doAnswer { sessionToReturnByMock }
        doAnswer { sessionToReturnByMock = it.arguments[0] as Session? }
            .whenever(mockNetworkSessionLocalStorage).session = anyOrNull()

        sut.get()

        mockServerScenarioSetup.takeRequest()
        val refreshRequest = mockServerScenarioSetup.takeRequest()
        val retryAfterTokenRefreshRequest = mockServerScenarioSetup.takeRequest()

        Assertions.assertEquals("PUT", refreshRequest.method)
        Assertions.assertEquals(
            "/login/${ContentData.loginSuccessResponse.refreshToken}",
            refreshRequest.path
        )
        Assertions.assertEquals(null, refreshRequest.getHeader("Authorization"))
        Assertions.assertEquals("Android", refreshRequest.getHeader("Platform"))
        Assertions.assertEquals("", refreshRequest.body.readUtf8())
        Assertions.assertEquals(
            ContentData.refreshSuccessResponse.accessToken,
            retryAfterTokenRefreshRequest.getHeader("Authorization")
        )
        verify(mockNetworkSessionLocalStorage, times(1)).session =
            ContentData.refreshSuccessResponse
        verifyZeroInteractions(mockNetworkSessionExpirationListener)
    }

    @DisplayName("GIVEN 401 THEN failing refresh WHEN content requested THE error is returned and callback is Called")
    @Test
    fun failingRefreshResultsInSessionExpiration() = runBlocking {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.Unauthorized(usingRefreshedToken = false), validateArguments = false)
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Error, validateArguments = false)

        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.get() }
        }
        verify(mockNetworkSessionLocalStorage, times(3)).session
        verify(mockNetworkSessionLocalStorage, times(1)).session = null
        verifyNoMoreInteractions(mockNetworkSessionLocalStorage)
        verify(mockNetworkSessionExpirationListener, times(1)).onSessionExpired()
    }
}
