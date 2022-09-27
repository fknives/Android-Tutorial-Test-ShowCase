package org.fnives.test.showcase.hilt.core.content

import org.fnives.test.showcase.hilt.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
import javax.inject.Inject

class AddContentToFavouriteUseCase @Inject internal constructor(
    private val favouriteContentLocalStorage: FavouriteContentLocalStorage,
) {

    suspend fun invoke(contentId: ContentId) =
        favouriteContentLocalStorage.markAsFavourite(contentId)
}
