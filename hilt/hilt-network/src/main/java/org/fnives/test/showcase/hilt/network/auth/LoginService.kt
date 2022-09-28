package org.fnives.test.showcase.hilt.network.auth

import org.fnives.test.showcase.hilt.network.auth.model.CredentialsRequest
import org.fnives.test.showcase.hilt.network.auth.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface LoginService {

    @POST("login")
    suspend fun login(@Body credentials: CredentialsRequest): Response<LoginResponse>

    @PUT("login/{token}")
    suspend fun refreshToken(@Path("token") token: String): LoginResponse
}
