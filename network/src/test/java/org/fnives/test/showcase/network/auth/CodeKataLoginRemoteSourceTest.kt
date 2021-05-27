package org.fnives.test.showcase.network.auth

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.network.di.createNetworkModules
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.kotlin.mock
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.io.BufferedReader
import java.io.InputStreamReader

class CodeKataLoginRemoteSourceTest {

    @BeforeEach
    fun setUp() {

    }

    @AfterEach
    fun tearDown() {

    }

    @DisplayName("GIVEN successful response WHEN request is fired THEN login status success is returned")
    @Test
    fun successResponseIsParsedProperly() = runBlocking {

    }

    @DisplayName("GIVEN successful response WHEN request is fired THEN the request is setup properly")
    @Test
    fun requestProperlySetup() = runBlocking {

    }

    @DisplayName("GIVEN bad request response WHEN request is fired THEN login status invalid credentials is returned")
    @Test
    fun badRequestMeansInvalidCredentials() = runBlocking {

    }

    @DisplayName("GIVEN_internal_error_response_WHEN_request_is_fired_THEN_network_exception_is_thrown")
    @Test
    fun genericErrorMeansNetworkError() {

    }

    @DisplayName("GIVEN invalid json response WHEN request is fired THEN network exception is thrown")
    @Test
    fun invalidJsonMeansParsingException() {

    }

    @DisplayName("GIVEN malformed json response WHEN request is fired THEN network exception is thrown")
    @Test
    fun malformedJsonMeansParsingException() {

    }

    companion object {
        internal fun Any.readResourceFile(filePath: String): String = try {
            BufferedReader(InputStreamReader(this.javaClass.classLoader.getResourceAsStream(filePath)!!))
                    .readLines().joinToString("\n")
        } catch (nullPointerException: NullPointerException) {
            throw IllegalArgumentException("$filePath file not found!", nullPointerException)
        }

        private fun BufferedReader.readLines(): List<String> {
            val result = mutableListOf<String>()
            use {
                do {
                    readLine()?.let(result::add) ?: return result
                } while (true)
            }
        }

    }
}