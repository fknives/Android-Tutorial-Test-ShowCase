package org.fnives.test.showcase.network.shared.exceptions

class ParsingException(cause: Throwable) : RuntimeException(cause.message, cause)
