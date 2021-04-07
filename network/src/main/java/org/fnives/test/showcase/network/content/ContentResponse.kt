package org.fnives.test.showcase.network.content

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ContentResponse(
    @Json(name = "id")
    val id: String?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "image")
    val imageUrl: String?,
    @Json(name = "says")
    val description: String?
)
