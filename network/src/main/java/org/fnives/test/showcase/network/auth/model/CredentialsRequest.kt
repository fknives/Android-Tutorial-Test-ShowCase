package org.fnives.test.showcase.network.auth.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CredentialsRequest(
    @Json(name = "username")
    val user: String,
    @Json(name = "password")
    val password: String
)
