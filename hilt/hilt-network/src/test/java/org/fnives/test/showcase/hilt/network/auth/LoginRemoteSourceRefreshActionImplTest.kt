package org.fnives.test.showcase.hilt.network.auth

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.hilt.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.hilt.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.hilt.network.shared.exceptions.ParsingException
import org.fnives.test.showcase.hilt.network.testutil.DaggerTestNetworkComponent
import org.fnives.test.showcase.hilt.network.testutil.MockServerScenarioSetupExtensions
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.test.inject
import org.mockito.kotlin.mock
import javax.inject.Inject

@Suppress("TestFunctionName")
class LoginRemoteSourceRefreshActionImplTest {

    @Inject
    internal lateinit var sut: LoginRemoteSourceImpl
    private lateinit var mockNetworkSessionLocalStorage: NetworkSessionLocalStorage

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup

    @BeforeEach
    fun setUp() {
        mockNetworkSessionLocalStorage = mock()
        DaggerTestNetworkComponent.builder()
            .setBaseUrl(mockServerScenarioSetupExtensions.url)
            .setEnableLogging(true)
            .setNetworkSessionLocalStorage(mockNetworkSessionLocalStorage)
            .setNetworkSessionExpirationListener(mock())
            .build()
            .inject(this)
    }

    @DisplayName("GIVEN successful response WHEN refresh request is fired THEN session is returned")
    @Test
    fun successResponseResultsInSession() = runBlocking {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Success, validateArguments = false)
        val expected = ContentData.refreshSuccessResponse

        val actual = sut.refresh(ContentData.refreshSuccessResponse.refreshToken)

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN successful response WHEN refresh request is fired THEN the request is setup properly")
    @Test
    fun refreshRequestIsSetupProperly() = runBlocking {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Success, validateArguments = false)

        sut.refresh(ContentData.refreshSuccessResponse.refreshToken)
        val request = mockServerScenarioSetup.takeRequest()

        Assertions.assertEquals("PUT", request.method)
        Assertions.assertEquals("Android", request.getHeader("Platform"))
        Assertions.assertEquals(null, request.getHeader("Authorization"))
        Assertions.assertEquals("/login/${ContentData.refreshSuccessResponse.refreshToken}", request.path)
        Assertions.assertEquals("", request.body.readUtf8())
    }

    @DisplayName("GIVEN internal error response WHEN refresh request is fired THEN network exception is thrown")
    @Test
    fun generalErrorResponseResultsInNetworkException() {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Error, validateArguments = false)

        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.refresh(ContentData.refreshSuccessResponse.refreshToken) }
        }
    }

    @DisplayName("GIVEN invalid json response WHEN refresh request is fired THEN network exception is thrown")
    @Test
    fun jsonErrorResponseResultsInParsingException() {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.UnexpectedJsonAsSuccessResponse, validateArguments = false)

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.refresh(ContentData.loginSuccessResponse.refreshToken) }
        }
    }

    @DisplayName("GIVEN malformed json response WHEN refresh request is fired THEN parsing exception is thrown")
    @Test
    fun malformedJsonErrorResponseResultsInParsingException() {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.MalformedJson, validateArguments = false)

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.refresh(ContentData.loginSuccessResponse.refreshToken) }
        }
    }
}
