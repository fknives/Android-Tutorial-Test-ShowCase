package org.fnives.test.showcase.hilt.network.content

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ContentResponse internal constructor(
    @Json(name = "id")
    val id: String?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "image")
    val imageUrl: String?,
    @Json(name = "says")
    val description: String?
)
