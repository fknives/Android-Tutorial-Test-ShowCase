package org.fnives.test.showcase.network.auth

import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.internal.http.RealResponseBody
import okio.Buffer
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.network.auth.model.LoginResponse
import org.fnives.test.showcase.network.auth.model.LoginStatusResponses
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.Response
import java.io.IOException

@Suppress("TestFunctionName")
class LoginErrorConverterTest {

    private lateinit var sut: LoginErrorConverter

    @BeforeEach
    fun setUp() {
        sut = LoginErrorConverter()
    }

    @DisplayName("GIVEN throwing lambda WHEN parsing login error THEN network exception is thrown")
    @Test
    fun generallyThrowingLambdaResultsInNetworkException() {
        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking {
                sut.invoke { throw IOException() }
            }
        }
    }

    @DisplayName("GIVEN jsonException throwing lambda WHEN parsing login error THEN network exception is thrown")
    @Test
    fun jsonDataThrowingLambdaResultsInParsingException() {
        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking {
                sut.invoke { throw JsonDataException("") }
            }
        }
    }

    @DisplayName("GIVEN 400 error response WHEN parsing login error THEN invalid credentials is returned")
    @Test
    fun code400ResponseResultsInInvalidCredentials() = runBlockingTest {
        val expected = LoginStatusResponses.InvalidCredentials

        val actual = sut.invoke {
            val responseBody = RealResponseBody(null, 0, Buffer())
            Response.error(400, responseBody)
        }

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN successful response WHEN parsing login error THEN successful response is returned")
    @Test
    fun successResponseResultsInSessionResponse() = runBlockingTest {
        val loginResponse = LoginResponse("a", "r")
        val expectedSession = Session(accessToken = loginResponse.accessToken, refreshToken = loginResponse.refreshToken)
        val expected = LoginStatusResponses.Success(expectedSession)

        val actual = sut.invoke {
            Response.success(200, loginResponse)
        }

        Assertions.assertEquals(expected, actual)
    }
}
