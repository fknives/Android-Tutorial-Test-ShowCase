package org.fnives.test.showcase.network.auth

import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.EOFException
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.network.auth.CodeKataLoginRemoteSourceTest.Companion.getLoginBodyJson
import org.fnives.test.showcase.network.auth.CodeKataLoginRemoteSourceTest.Companion.readResourceFile
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.network.di.createNetworkModules
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.kotlin.mock
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import retrofit2.HttpException

class PlainLoginRemoteSourceTest : KoinTest {

    private val sut by inject<LoginRemoteSource>()
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        startKoin {
            modules(
                createNetworkModules(
                    baseUrl = BaseUrl(mockWebServer.url("mockserver/").toString()),
                    enableLogging = true,
                    networkSessionExpirationListenerProvider = { mock() },
                    networkSessionLocalStorageProvider = { mock() }
                ).toList()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        mockWebServer.shutdown()
    }

    @DisplayName("GIVEN successful response WHEN request is fired THEN login status success is returned")
    @Test
    fun successResponseIsParsedProperly() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(readResourceFile("success_response_login.json")))
        val session = Session(accessToken = "login-access", refreshToken = "login-refresh")
        val expected = LoginStatusResponses.Success(session = session)

        val actual = sut.login(LoginCredentials(username = "alma", password = "banan"))

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN successful response WHEN request is fired THEN the request is setup properly")
    @Test
    fun requestProperlySetup() = runBlocking {

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(readResourceFile("success_response_login.json")))

        sut.login(LoginCredentials(username = "alma", password = "banan"))

        val request = mockWebServer.takeRequest()

        Assertions.assertEquals("POST", request.method)
        Assertions.assertEquals("Android", request.getHeader("Platform"))
        Assertions.assertEquals(null, request.getHeader("Authorization"))
        Assertions.assertEquals("/mockserver/login", request.path)
        val loginRequestBody = getLoginBodyJson(username = "alma", password = "banan")
        JSONAssert.assertEquals(
            loginRequestBody,
            request.body.readUtf8(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @DisplayName("GIVEN bad request response WHEN request is fired THEN login status invalid credentials is returned")
    @Test
    fun badRequestMeansInvalidCredentials() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody("{}"))

        val expected = LoginStatusResponses.InvalidCredentials

        val actual = sut.login(LoginCredentials(username = "a", password = "b"))

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN internal error response WHEN request is fired THEN network exception is thrown")
    @Test
    fun genericErrorMeansNetworkError() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        val actual = Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
        }

        Assertions.assertEquals("HTTP 500 Server Error", actual.message)
        Assertions.assertTrue(actual.cause is HttpException)
    }

    @DisplayName("GIVEN invalid json response WHEN request is fired THEN network exception is thrown")
    @Test
    fun invalidJsonMeansParsingException() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val actual = Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
        }

        Assertions.assertEquals("Expected BEGIN_OBJECT but was BEGIN_ARRAY at path \$", actual.message)
        Assertions.assertTrue(actual.cause is JsonDataException)
    }

    @DisplayName("GIVEN json response with missing field WHEN request is fired THEN network exception is thrown")
    @Test
    fun missingFieldJsonMeansParsingException() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val actual = Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
        }

        Assertions.assertEquals("Required value 'accessToken' missing at \$", actual.message)
        Assertions.assertTrue(actual.cause is JsonDataException)
    }

    @DisplayName("GIVEN malformed json response WHEN request is fired THEN network exception is thrown")
    @Test
    fun malformedJsonMeansParsingException() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{"))

        val actual = Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
        }

        Assertions.assertEquals("End of input", actual.message)
        Assertions.assertTrue(actual.cause is EOFException)
    }
}
