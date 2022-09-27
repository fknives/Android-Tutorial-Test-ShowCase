package org.fnives.test.showcase.hilt.core.integration.fake

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.fnives.test.showcase.hilt.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.ContentId

class FakeFavouriteContentLocalStorage : FavouriteContentLocalStorage {

    private val dataFlow = MutableSharedFlow<List<ContentId>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        dataFlow.tryEmit(emptyList())
    }

    override fun observeFavourites(): Flow<List<ContentId>> = dataFlow.asSharedFlow()

    override suspend fun markAsFavourite(contentId: ContentId) {
        dataFlow.emit(dataFlow.replayCache.first().plus(contentId))
    }

    override suspend fun deleteAsFavourite(contentId: ContentId) {
        dataFlow.emit(dataFlow.replayCache.first().minus(contentId))
    }
}
