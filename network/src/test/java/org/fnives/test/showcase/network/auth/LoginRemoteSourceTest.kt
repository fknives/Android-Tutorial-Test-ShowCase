package org.fnives.test.showcase.network.auth

import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.runBlocking
import okio.EOFException
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.network.di.createNetworkModules
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.ContentData.createExpectedLoginRequestJson
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
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
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import retrofit2.HttpException

@Suppress("TestFunctionName")
class LoginRemoteSourceTest : KoinTest {

    private val sut by inject<LoginRemoteSource>()

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup

    @BeforeEach
    fun setUp() {
        val mockNetworkSessionLocalStorage = mock<NetworkSessionLocalStorage>()
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

    @DisplayName("GIVEN successful response WHEN request is fired THEN login status success is returned")
    @Test
    fun successResponseIsParsedProperly() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "a", password = "b"), validateArguments = false)
        val expected = LoginStatusResponses.Success(ContentData.loginSuccessResponse)

        val actual = sut.login(LoginCredentials(username = "a", password = "b"))

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN successful response WHEN request is fired THEN the request is setup properly")
    @Test
    fun requestProperlySetup() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "a", password = "b"), validateArguments = false)

        sut.login(LoginCredentials(username = "a", password = "b"))
        val request = mockServerScenarioSetup.takeRequest()

        Assertions.assertEquals("POST", request.method)
        Assertions.assertEquals("Android", request.getHeader("Platform"))
        Assertions.assertEquals(null, request.getHeader("Authorization"))
        Assertions.assertEquals("/login", request.path)
        val loginRequest = createExpectedLoginRequestJson(username = "a", password = "b")
        JSONAssert.assertEquals(
            loginRequest,
            request.body.readUtf8(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @DisplayName("GIVEN bad request response WHEN request is fired THEN login status invalid credentials is returned")
    @Test
    fun badRequestMeansInvalidCredentials() = runBlocking {
        mockServerScenarioSetup.setScenario(AuthScenario.InvalidCredentials(username = "a", password = "b"), validateArguments = false)
        val expected = LoginStatusResponses.InvalidCredentials

        val actual = sut.login(LoginCredentials(username = "a", password = "b"))

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN internal error response WHEN request is fired THEN network exception is thrown")
    @Test
    fun genericErrorMeansNetworkError() {
        mockServerScenarioSetup.setScenario(AuthScenario.GenericError(username = "a", password = "b"), validateArguments = false)

        val actual = Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
        }

        Assertions.assertEquals("HTTP 500 Server Error", actual.message)
        Assertions.assertTrue(actual.cause is HttpException)
    }

    @DisplayName("GIVEN invalid json response WHEN request is fired THEN network exception is thrown")
    @Test
    fun invalidJsonMeansParsingException() {
        val response = AuthScenario.UnexpectedJsonAsSuccessResponse(username = "a", password = "b")
        mockServerScenarioSetup.setScenario(response, validateArguments = false)

        val actual = Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
        }

        Assertions.assertEquals("Expected BEGIN_OBJECT but was BEGIN_ARRAY at path \$", actual.message)
        Assertions.assertTrue(actual.cause is JsonDataException)
    }

    @DisplayName("GIVEN json response with missing field WHEN request is fired THEN network exception is thrown")
    @Test
    fun missingFieldJsonMeansParsingException() {
        val response = AuthScenario.MissingFieldJson(username = "a", password = "b")
        mockServerScenarioSetup.setScenario(response, validateArguments = false)

        val actual = Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
        }

        Assertions.assertEquals("Required value 'accessToken' missing at \$", actual.message)
        Assertions.assertTrue(actual.cause is JsonDataException)
    }

    @DisplayName("GIVEN malformed json response WHEN request is fired THEN network exception is thrown")
    @Test
    fun malformedJsonMeansParsingException() {
        val response = AuthScenario.MalformedJsonAsSuccessResponse(username = "a", "b")
        mockServerScenarioSetup.setScenario(response, validateArguments = false)

        val actual = Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials(username = "a", "b")) }
        }

        Assertions.assertEquals("End of input", actual.message)
        Assertions.assertTrue(actual.cause is EOFException)
    }
}
