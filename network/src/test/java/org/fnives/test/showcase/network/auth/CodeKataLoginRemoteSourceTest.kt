package org.fnives.test.showcase.network.auth

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader

@Disabled("CodeKata")
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

    @DisplayName("GIVEN internal error response WHEN request is fired THEN network exception is thrown")
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
