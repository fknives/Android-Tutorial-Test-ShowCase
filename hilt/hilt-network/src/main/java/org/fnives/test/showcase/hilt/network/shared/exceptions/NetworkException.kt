package org.fnives.test.showcase.hilt.network.shared.exceptions

class NetworkException(cause: Throwable) : RuntimeException(cause.message, cause)
