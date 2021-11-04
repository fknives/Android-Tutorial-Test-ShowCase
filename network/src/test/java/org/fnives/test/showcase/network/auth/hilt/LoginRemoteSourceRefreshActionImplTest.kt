package org.fnives.test.showcase.network.auth.hilt

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.network.DaggerTestNetworkComponent
import org.fnives.test.showcase.network.auth.LoginRemoteSourceImpl
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.shared.MockServerScenarioSetupExtensions
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
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
    private val mockServerScenarioSetup
        get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup

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

    @DisplayName("GIVEN_successful_response_WHEN_refresh_request_is_fired_THEN_session_is_returned")
    @Test
    fun successResponseResultsInSession() = runBlocking {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Success)
        val expected = ContentData.refreshSuccessResponse

        val actual = sut.refresh(ContentData.refreshSuccessResponse.refreshToken)

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN_successful_response_WHEN_refresh_request_is_fired_THEN_the_request_is_setup_properly")
    @Test
    fun refreshRequestIsSetupProperly() = runBlocking {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Success, false)

        sut.refresh(ContentData.refreshSuccessResponse.refreshToken)
        val request = mockServerScenarioSetup.takeRequest()

        Assertions.assertEquals("PUT", request.method)
        Assertions.assertEquals("Android", request.getHeader("Platform"))
        Assertions.assertEquals(null, request.getHeader("Authorization"))
        Assertions.assertEquals(
            "/login/${ContentData.refreshSuccessResponse.refreshToken}",
            request.path
        )
        Assertions.assertEquals("", request.body.readUtf8())
    }

    @DisplayName("GIVEN_internal_error_response_WHEN_refresh_request_is_fired_THEN_network_exception_is_thrown")
    @Test
    fun generalErrorResponseResultsInNetworkException() {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Error)

        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.refresh(ContentData.refreshSuccessResponse.refreshToken) }
        }
    }

    @DisplayName("GIVEN_invalid_json_response_WHEN_refresh_request_is_fired_THEN_network_exception_is_thrown")
    @Test
    fun jsonErrorResponseResultsInParsingException() {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.UnexpectedJsonAsSuccessResponse)

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.refresh(ContentData.loginSuccessResponse.refreshToken) }
        }
    }

    @DisplayName("GIVEN_malformed_json_response_WHEN_refresh_request_is_fired_THEN_parsing_exception_is_thrown")
    @Test
    fun malformedJsonErrorResponseResultsInParsingException() {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.MalformedJson)

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.refresh(ContentData.loginSuccessResponse.refreshToken) }
        }
    }
}
