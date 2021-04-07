package org.fnives.test.showcase.network.auth

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.network.di.createNetworkModules
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.ContentData.createExpectedLoginRequestJson
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.shared.MockServerScenarioSetupExtensions
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.kotlin.mock
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

@Suppress("TestFunctionName")
class LoginRemoteSourceTest : KoinTest {

    private val sut by inject<LoginRemoteSource>()
    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup
        get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup
    private lateinit var mockNetworkSessionLocalStorage: NetworkSessionLocalStorage

    @BeforeEach
    fun setUp() {
        mockNetworkSessionLocalStorage = mock()
        startKoin {
            modules(
                createNetworkModules(
                    baseUrl = BaseUrl(mockServerScenarioSetupExtensions.url),
                    enableLogging = true,
                    networkSessionExpirationListenerProvider = mock(),
                    networkSessionLocalStorageProvider = { mockNetworkSessionLocalStorage }
                ).toList()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun GIVEN_successful_response_WHEN_request_is_fired_THEN_login_status_success_is_returned() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.Success("a", "b"))
        val expected = LoginStatusResponses.Success(ContentData.loginSuccessResponse)

        val actual = sut.login(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_successful_response_WHEN_request_is_fired_THEN_the_request_is_setup_properly() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.Success("a", "b"), false)

        sut.login(LoginCredentials("a", "b"))
        val request = mockServerScenarioSetup.takeRequest()

        Assertions.assertEquals("POST", request.method)
        Assertions.assertEquals("Android", request.getHeader("Platform"))
        Assertions.assertEquals(null, request.getHeader("Authorization"))
        Assertions.assertEquals("/login", request.path)
        val loginRequest = createExpectedLoginRequestJson("a", "b")
        JSONAssert.assertEquals(loginRequest, request.body.readUtf8(), JSONCompareMode.NON_EXTENSIBLE)
    }

    @Test
    fun GIVEN_bad_request_response_WHEN_request_is_fired_THEN_login_status_invalid_credentials_is_returned() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.InvalidCredentials("a", "b"))
        val expected = LoginStatusResponses.InvalidCredentials

        val actual = sut.login(LoginCredentials("a", "b"))

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_internal_error_response_WHEN_request_is_fired_THEN_network_exception_is_thrown() {
        mockServerScenarioSetup.setScenario(AuthScenario.GenericError("a", "b"))

        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.login(LoginCredentials("a", "b")) }
        }
    }

    @Test
    fun GIVEN_invalid_json_response_WHEN_request_is_fired_THEN_network_exception_is_thrown() {
        mockServerScenarioSetup.setScenario(AuthScenario.UnexpectedJsonAsSuccessResponse("a", "b"))

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials("a", "b")) }
        }
    }

    @Test
    fun GIVEN_malformed_json_response_WHEN_request_is_fired_THEN_network_exception_is_thrown() {
        mockServerScenarioSetup.setScenario(AuthScenario.MalformedJsonAsSuccessResponse("a", "b"))

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials("a", "b")) }
        }
    }
}
