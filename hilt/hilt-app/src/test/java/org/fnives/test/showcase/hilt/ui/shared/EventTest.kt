package org.fnives.test.showcase.hilt.ui.shared

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Suppress("TestFunctionName")
internal class EventTest {

    @DisplayName("GIVEN event WHEN consumed is called THEN value is returned")
    @Test
    fun consumedReturnsValue() {
        val expected = "a"

        val actual = Event("a").consume()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN consumed event WHEN consumed is called THEN null is returned")
    @Test
    fun consumedEventReturnsNull() {
        val expected: String? = null
        val event = Event("a")
        event.consume()

        val actual = event.consume()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN event WHEN peek is called THEN value is returned")
    @Test
    fun peekReturnsValue() {
        val expected = "a"

        val actual = Event("a").peek()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN consumed event WHEN peek is called THEN value is returned")
    @Test
    fun consumedEventPeekedReturnsValue() {
        val expected = "a"
        val event = Event("a")
        event.consume()

        val actual = event.peek()

        Assertions.assertEquals(expected, actual)
    }
}
