package org.fnives.test.showcase.hilt.network.shared.exceptions

class ParsingException(cause: Throwable) : RuntimeException(cause.message, cause)
