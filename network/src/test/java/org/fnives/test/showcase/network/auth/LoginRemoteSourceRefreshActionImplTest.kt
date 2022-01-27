package org.fnives.test.showcase.network.auth

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.di.createNetworkModules
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.testutil.MockServerScenarioSetupExtensions
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
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
import org.mockito.kotlin.mock

@Suppress("TestFunctionName")
class LoginRemoteSourceRefreshActionImplTest : KoinTest {

    private val sut by inject<LoginRemoteSourceImpl>()
    private lateinit var mockNetworkSessionLocalStorage: NetworkSessionLocalStorage

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup

    @BeforeEach
    fun setUp() {
        mockNetworkSessionLocalStorage = mock()
        startKoin {
            modules(
                createNetworkModules(
                    baseUrl = BaseUrl(mockServerScenarioSetupExtensions.url),
                    enableLogging = true,
                    networkSessionExpirationListenerProvider = { mock() },
                    networkSessionLocalStorageProvider = { mockNetworkSessionLocalStorage }
                ).toList()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
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
