package org.fnives.test.showcase.core.content

import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId

class RemoveContentFromFavouritesUseCase internal constructor(
    private val favouriteContentLocalStorage: FavouriteContentLocalStorage
) {

    suspend fun invoke(contentId: ContentId) {
        favouriteContentLocalStorage.deleteAsFavourite(contentId)
    }
}
