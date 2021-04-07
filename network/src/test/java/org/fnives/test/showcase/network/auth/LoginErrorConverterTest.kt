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

    @Test
    fun GIVEN_throwing_lambda_WHEN_parsing_login_error_THEN_network_exception_is_thrown() {
        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking {
                sut.invoke { throw IOException() }
            }
        }
    }

    @Test
    fun GIVEN_jsonException_throwing_lambda_WHEN_parsing_login_error_THEN_network_exception_is_thrown() {
        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking {
                sut.invoke { throw JsonDataException("") }
            }
        }
    }

    @Test
    fun GIVEN_400_error_response_WHEN_parsing_login_error_THEN_invalid_credentials_is_returned() = runBlockingTest {
        val expected = LoginStatusResponses.InvalidCredentials

        val actual = sut.invoke {
            val responseBody = RealResponseBody(null, 0, Buffer())
            Response.error(400, responseBody)
        }

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_successful_response_WHEN_parsing_login_error_THEN_successful_response_is_returned() = runBlockingTest {
        val loginResponse = LoginResponse("a", "r")
        val expectedSession = Session(accessToken = loginResponse.accessToken, refreshToken = loginResponse.refreshToken)
        val expected = LoginStatusResponses.Success(expectedSession)

        val actual = sut.invoke {
            Response.success(200, loginResponse)
        }

        Assertions.assertEquals(expected, actual)
    }
}
