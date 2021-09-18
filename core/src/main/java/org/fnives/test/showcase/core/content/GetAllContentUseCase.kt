package org.fnives.test.showcase.core.content

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.model.shared.Resource
import javax.inject.Inject

class GetAllContentUseCase @Inject internal constructor(
    private val contentRepository: ContentRepository,
    private val favouriteContentLocalStorage: FavouriteContentLocalStorage
) {

    fun get(): Flow<Resource<List<FavouriteContent>>> =
        contentRepository.contents.combine(
            favouriteContentLocalStorage.observeFavourites(),
            ::combineContentWithFavourites
        )

    companion object {
        private fun combineContentWithFavourites(
            contentResource: Resource<List<Content>>,
            favouriteContents: List<ContentId>
        ): Resource<List<FavouriteContent>> =
            when (contentResource) {
                is Resource.Error -> Resource.Error(contentResource.error)
                is Resource.Loading -> Resource.Loading()
                is Resource.Success ->
                    Resource.Success(
                        combineContentWithFavourites(contentResource.data, favouriteContents)
                    )
            }

        private fun combineContentWithFavourites(
            content: List<Content>,
            favourite: List<ContentId>
        ): List<FavouriteContent> =
            content.map {
                FavouriteContent(content = it, isFavourite = favourite.contains(it.id))
            }
    }
}
