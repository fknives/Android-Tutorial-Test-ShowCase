package org.fnives.test.showcase.core.content

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.fnives.test.showcase.core.shared.Optional
import org.fnives.test.showcase.core.shared.mapIntoResource
import org.fnives.test.showcase.core.shared.wrapIntoAnswer
import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.content.ContentRemoteSource
import kotlin.coroutines.cancellation.CancellationException

internal class ContentRepository(
    private val contentRemoteSource: ContentRemoteSource
) {

    private val mutableContentFlow = MutableStateFlow(Optional<List<Content>>(null))
    private val requestFlow: Flow<Resource<List<Content>>> = flow {
        try {
            System.err.println("emit loading")
            emit(Resource.Loading())
            System.err.println("calling request")
            val response = wrapIntoAnswer { contentRemoteSource.get() }.mapIntoResource()
            System.err.println("got response: $response")
            if (response is Resource.Success) {
                mutableContentFlow.value = Optional(response.data)
                System.err.println("updated flow")
            } else {
                System.err.println("emit response")
                emit(response)
            }
        } catch(throwable: Throwable) {
            System.err.println("exception in flow{} $throwable = ${throwable.stackTrace}")
            if (throwable is CancellationException) throw throwable
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val contents: Flow<Resource<List<Content>>> = mutableContentFlow.flatMapLatest {
        System.err.println("calling flatmap: ${it.item}")
        if (it.item != null) flowOf(Resource.Success(it.item)) else requestFlow
    }
        .distinctUntilChanged()

    fun fetch() {
        mutableContentFlow.value = Optional(null)
    }
}
