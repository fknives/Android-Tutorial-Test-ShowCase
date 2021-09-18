package org.fnives.test.showcase.storage.favourite

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId
import javax.inject.Inject

class FavouriteContentLocalStorageImpl @Inject constructor(private val favouriteDao: FavouriteDao) : FavouriteContentLocalStorage {
    override fun observeFavourites(): Flow<List<ContentId>> =
        favouriteDao.get().map { it.map(FavouriteEntity::contentId).map(::ContentId) }

    override suspend fun markAsFavourite(contentId: ContentId) {
        favouriteDao.addFavourite(FavouriteEntity(contentId.id))
    }

    override suspend fun deleteAsFavourite(contentId: ContentId) {
        favouriteDao.deleteFavourite(FavouriteEntity(contentId.id))
    }
}
