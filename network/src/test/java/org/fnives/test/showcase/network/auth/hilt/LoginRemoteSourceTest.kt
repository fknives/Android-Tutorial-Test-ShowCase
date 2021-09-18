package org.fnives.test.showcase.network.auth.hilt

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.network.DaggerTestNetworkComponent
import org.fnives.test.showcase.network.auth.LoginRemoteSource
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.ContentData.createExpectedLoginRequestJson
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
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
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import javax.inject.Inject

@Suppress("TestFunctionName")
class LoginRemoteSourceTest {

    @Inject
    internal lateinit var sut: LoginRemoteSource

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup
        get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup

    @BeforeEach
    fun setUp() {
        val mockNetworkSessionLocalStorage = mock<NetworkSessionLocalStorage>()
        DaggerTestNetworkComponent.builder()
            .setBaseUrl(mockServerScenarioSetupExtensions.url)
            .setEnableLogging(true)
            .setNetworkSessionLocalStorage(mockNetworkSessionLocalStorage)
            .setNetworkSessionExpirationListener(mock())
            .build()
            .inject(this)
    }

    @DisplayName("GIVEN successful response WHEN request is fired THEN login status success is returned")
    @Test
    fun successResponseIsParsedProperly() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.Success("a", "b"))
        val expected = LoginStatusResponses.Success(ContentData.loginSuccessResponse)

        val actual = sut.login(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN successful response WHEN request is fired THEN the request is setup properly")
    @Test
    fun requestProperlySetup() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.Success("a", "b"), false)

        sut.login(LoginCredentials("a", "b"))
        val request = mockServerScenarioSetup.takeRequest()

        Assertions.assertEquals("POST", request.method)
        Assertions.assertEquals("Android", request.getHeader("Platform"))
        Assertions.assertEquals(null, request.getHeader("Authorization"))
        Assertions.assertEquals("/login", request.path)
        val loginRequest = createExpectedLoginRequestJson("a", "b")
        JSONAssert.assertEquals(
            loginRequest,
            request.body.readUtf8(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @DisplayName("GIVEN bad request response WHEN request is fired THEN login status invalid credentials is returned")
    @Test
    fun badRequestMeansInvalidCredentials() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.InvalidCredentials("a", "b"))
        val expected = LoginStatusResponses.InvalidCredentials

        val actual = sut.login(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN_internal_error_response_WHEN_request_is_fired_THEN_network_exception_is_thrown")
    @Test
    fun genericErrorMeansNetworkError() {
        mockServerScenarioSetup.setScenario(AuthScenario.GenericError("a", "b"))

        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.login(LoginCredentials("a", "b")) }
        }
    }

    @DisplayName("GIVEN invalid json response WHEN request is fired THEN network exception is thrown")
    @Test
    fun invalidJsonMeansParsingException() {
        mockServerScenarioSetup.setScenario(AuthScenario.UnexpectedJsonAsSuccessResponse("a", "b"))

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials("a", "b")) }
        }
    }

    @DisplayName("GIVEN malformed json response WHEN request is fired THEN network exception is thrown")
    @Test
    fun malformedJsonMeansParsingException() {
        mockServerScenarioSetup.setScenario(AuthScenario.MalformedJsonAsSuccessResponse("a", "b"))

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials("a", "b")) }
        }
    }
}
