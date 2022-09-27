package org.fnives.test.showcase.hilt.core.content

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.fnives.test.showcase.hilt.core.di.LoggedInModuleInject
import org.fnives.test.showcase.hilt.core.shared.Optional
import org.fnives.test.showcase.hilt.core.shared.mapIntoResource
import org.fnives.test.showcase.hilt.core.shared.wrapIntoAnswer
import org.fnives.test.showcase.hilt.network.content.ContentRemoteSource
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.shared.Resource

internal class ContentRepository @LoggedInModuleInject internal constructor(
    private val contentRemoteSource: ContentRemoteSource,
) {

    private val mutableContentFlow = MutableStateFlow(Optional<List<Content>>(null))
    private val requestFlow: Flow<Resource<List<Content>>> = flow {
        emit(Resource.Loading())
        val response = wrapIntoAnswer { contentRemoteSource.get() }.mapIntoResource()
        if (response is Resource.Success) {
            mutableContentFlow.value = Optional(response.data)
        }
        emit(response)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val contents: Flow<Resource<List<Content>>> = mutableContentFlow.flatMapLatest {
        if (it.item != null) flowOf(Resource.Success(it.item)) else requestFlow
    }
        .distinctUntilChanged()

    fun fetch() {
        mutableContentFlow.value = Optional(null)
    }
}
