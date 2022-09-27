package org.fnives.test.showcase.hilt.network.auth.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class CredentialsRequest(
    @Json(name = "username")
    val user: String,
    @Json(name = "password")
    val password: String
)
