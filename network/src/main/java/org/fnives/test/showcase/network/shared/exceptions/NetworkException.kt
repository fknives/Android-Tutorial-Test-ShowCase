package org.fnives.test.showcase.network.shared.exceptions

class NetworkException(cause: Throwable) : RuntimeException(cause.message, cause)
