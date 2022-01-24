package org.fnives.test.showcase.network.content

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.network.auth.CodeKataLoginRemoteSourceTest.Companion.readResourceFile
import org.fnives.test.showcase.network.di.createNetworkModules
import org.fnives.test.showcase.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
import retrofit2.HttpException

class PlainSessionExpirationTest : KoinTest {

    private val sut by inject<ContentRemoteSource>()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var mockNetworkSessionLocalStorage: NetworkSessionLocalStorage
    private lateinit var mockNetworkSessionExpirationListener: NetworkSessionExpirationListener

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockNetworkSessionLocalStorage = mock()
        mockNetworkSessionExpirationListener = mock()
        startKoin {
            modules(
                createNetworkModules(
                    baseUrl = BaseUrl(mockWebServer.url("mockserver/").toString()),
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
        mockWebServer.shutdown()
    }

    @DisplayName("GIVEN 401 THEN refresh token ok response WHEN content requested THE tokens are refreshed and request retried with new tokens")
    @Test
    fun successRefreshResultsInRequestRetry() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(readResourceFile("success_response_login.json")))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        var sessionToReturnByMock: Session? = Session(accessToken = "expired-access", refreshToken = "expired-refresh")
        whenever(mockNetworkSessionLocalStorage.session).doAnswer { sessionToReturnByMock }
        doAnswer { sessionToReturnByMock = it.arguments[0] as Session? }
            .whenever(mockNetworkSessionLocalStorage).session = anyOrNull()

        sut.get()

        mockWebServer.takeRequest()
        val refreshRequest = mockWebServer.takeRequest()
        val contentRequestAfterRefreshed = mockWebServer.takeRequest()

        Assertions.assertEquals("PUT", refreshRequest.method)
        Assertions.assertEquals("/mockserver/login/expired-refresh", refreshRequest.path)
        Assertions.assertEquals(null, refreshRequest.getHeader("Authorization"))
        Assertions.assertEquals("Android", refreshRequest.getHeader("Platform"))
        Assertions.assertEquals("", refreshRequest.body.readUtf8())

        Assertions.assertEquals("login-access", contentRequestAfterRefreshed.getHeader("Authorization"))
        val expectedSavedSession = Session(accessToken = "login-access", refreshToken = "login-refresh")
        verify(mockNetworkSessionLocalStorage, times(1)).session = expectedSavedSession
        verifyZeroInteractions(mockNetworkSessionExpirationListener)
    }

    @DisplayName("GIVEN 401 THEN failing refresh WHEN content requested THE error is returned and callback is Called")
    @Test
    fun failingRefreshResultsInSessionExpiration() = runBlocking {
        val currentSession = Session(accessToken = "expired-access", refreshToken = "expired-refresh")
        whenever(mockNetworkSessionLocalStorage.session).doReturn(currentSession)
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        val actual = Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.get() }
        }

        Assertions.assertEquals("HTTP 401 Client Error", actual.message)
        Assertions.assertTrue(actual.cause is HttpException)
        verify(mockNetworkSessionLocalStorage, times(3)).session
        verify(mockNetworkSessionLocalStorage, times(1)).session = null
        verifyNoMoreInteractions(mockNetworkSessionLocalStorage)
        verify(mockNetworkSessionExpirationListener, times(1)).onSessionExpired()
    }
}
