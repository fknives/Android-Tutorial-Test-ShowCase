package org.fnives.test.showcase.hilt.core.shared

import kotlinx.coroutines.CancellationException
import org.fnives.test.showcase.hilt.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.hilt.network.shared.exceptions.ParsingException
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.model.shared.Resource

@Suppress("RethrowCaughtException")
internal suspend fun <T> wrapIntoAnswer(callback: suspend () -> T): Answer<T> =
    try {
        Answer.Success(callback())
    } catch (networkException: NetworkException) {
        Answer.Error(networkException)
    } catch (parsingException: ParsingException) {
        Answer.Error(parsingException)
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (throwable: Throwable) {
        Answer.Error(UnexpectedException(throwable))
    }

internal fun <T> Answer<T>.mapIntoResource() = when (this) {
    is Answer.Error -> Resource.Error(error)
    is Answer.Success -> Resource.Success(data)
}
