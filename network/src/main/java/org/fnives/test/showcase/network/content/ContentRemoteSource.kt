package org.fnives.test.showcase.network.content

import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import kotlin.jvm.Throws

interface ContentRemoteSource {

    @Throws(NetworkException::class, ParsingException::class)
    suspend fun get(): List<Content>
}
