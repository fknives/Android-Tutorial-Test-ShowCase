package org.fnives.test.showcase.hilt.core.storage.content

import kotlinx.coroutines.flow.Flow
import org.fnives.test.showcase.model.content.ContentId

interface FavouriteContentLocalStorage {

    fun observeFavourites(): Flow<List<ContentId>>

    suspend fun markAsFavourite(contentId: ContentId)

    suspend fun deleteAsFavourite(contentId: ContentId)
}
