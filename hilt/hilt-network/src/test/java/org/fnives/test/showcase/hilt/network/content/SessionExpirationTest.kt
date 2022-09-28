package org.fnives.test.showcase.hilt.network.content

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.hilt.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.hilt.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.hilt.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.hilt.network.testutil.DaggerTestNetworkComponent
import org.fnives.test.showcase.hilt.network.testutil.MockServerScenarioSetupExtensions
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import javax.inject.Inject

@Suppress("TestFunctionName")
class SessionExpirationTest {

    @Inject
    internal lateinit var sut: ContentRemoteSourceImpl

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
        DaggerTestNetworkComponent.builder()
            .setBaseUrl(mockServerScenarioSetupExtensions.url)
            .setEnableLogging(true)
            .setNetworkSessionLocalStorage(mockNetworkSessionLocalStorage)
            .setNetworkSessionExpirationListener(mockNetworkSessionExpirationListener)
            .build()
            .inject(this)
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
        verifyNoInteractions(mockNetworkSessionExpirationListener)
    }

    @DisplayName("GIVEN 401 THEN failing refresh WHEN content requested THE error is returned and callback is Called")
    @Test
    fun failingRefreshResultsInSessionExpiration() = runBlocking {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.Unauthorized(usingRefreshedToken = false), validateArguments = false)
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Error, validateArguments = false)

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
