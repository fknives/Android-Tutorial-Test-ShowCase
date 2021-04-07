package org.fnives.test.showcase.model.shared

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val error: Throwable) : Resource<T>()
    class Loading<T> : Resource<T>() {
        override fun equals(other: Any?): Boolean =
            javaClass == other?.javaClass

        override fun hashCode(): Int = Loading::class.java.hashCode()

        override fun toString(): String = "Resource.Loading()"
    }

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    abstract override fun toString(): String
}
