package org.fnives.test.showcase.network.content

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.di.koin.createNetworkModules
import org.fnives.test.showcase.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.kotlin.mock

@Disabled("CodeKata")
class CodeKataSessionExpirationTest : KoinTest {

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

    @DisplayName("GIVEN_401_THEN_refresh_token_ok_response_WHEN_content_requested_THE_tokens_are_refreshed_and_request_retried_with_new_tokens")
    @Test
    fun successRefreshResultsInRequestRetry() = runBlocking {
    }

    @DisplayName("GIVEN 401 THEN failing refresh WHEN content requested THE error is returned and callback is Called")
    @Test
    fun failingRefreshResultsInSessionExpiration() = runBlocking {
    }
}
