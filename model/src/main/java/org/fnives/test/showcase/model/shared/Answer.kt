package org.fnives.test.showcase.model.shared

sealed class Answer<T> {
    data class Success<T>(val data: T) : Answer<T>()
    data class Error<T>(val error: Throwable) : Answer<T>()
}
