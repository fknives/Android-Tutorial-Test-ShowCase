package org.fnives.test.showcase.core.shared

class UnexpectedException(cause: Throwable) : RuntimeException(cause.message, cause) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return this.cause == (other as UnexpectedException).cause
    }

    override fun hashCode(): Int = super.hashCode() + cause.hashCode()
}
