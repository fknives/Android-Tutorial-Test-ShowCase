package org.fnives.test.showcase.hilt.network.content

import org.fnives.test.showcase.hilt.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.hilt.network.shared.exceptions.ParsingException
import org.fnives.test.showcase.model.content.Content

interface ContentRemoteSource {

    @Throws(NetworkException::class, ParsingException::class)
    suspend fun get(): List<Content>
}
