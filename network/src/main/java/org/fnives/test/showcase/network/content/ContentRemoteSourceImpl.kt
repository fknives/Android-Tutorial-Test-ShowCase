package org.fnives.test.showcase.network.content

import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.ImageUrl
import org.fnives.test.showcase.network.shared.ExceptionWrapper
import javax.inject.Inject

internal class ContentRemoteSourceImpl @Inject constructor(
    private val contentService: ContentService
) : ContentRemoteSource {

    override suspend fun get(): List<Content> =
        ExceptionWrapper.wrap {
            contentService.getContent().mapNotNull(::mapResponse)
        }

    companion object {

        private fun mapResponse(response: ContentResponse): Content? {
            return Content(
                id = response.id?.let(::ContentId) ?: return null,
                title = response.title ?: return null,
                description = response.description ?: return null,
                imageUrl = ImageUrl(response.imageUrl ?: return null)
            )
        }
    }
}
