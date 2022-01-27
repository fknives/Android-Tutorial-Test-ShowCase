package org.fnives.test.showcase.core.testutil

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class AwaitElementEmitCount(private var counter: Int) {

    private val completableDeferred = CompletableDeferred<Unit>()

    init {
        assert(counter > 0)
    }

    fun <T> attach(flow: Flow<T>): Flow<T> =
        flow.onEach {
            counter--
            if (counter == 0) {
                completableDeferred.complete(Unit)
            }
        }

    suspend fun await() = completableDeferred.await()
}
