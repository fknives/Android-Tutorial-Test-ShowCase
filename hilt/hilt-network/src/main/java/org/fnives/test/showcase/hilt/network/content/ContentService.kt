package org.fnives.test.showcase.hilt.network.content

import retrofit2.http.GET

interface ContentService {

    @GET("content")
    suspend fun getContent(): List<ContentResponse>
}
