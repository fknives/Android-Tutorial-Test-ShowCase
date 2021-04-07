package org.fnives.test.showcase.ui.shared

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("TestFunctionName")
internal class EventTest {

    @Test
    fun GIVEN_event_WHEN_consumed_is_called_THEN_value_is_returned() {
        val expected = "a"

        val actual = Event("a").consume()

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_consumed_event_WHEN_consumed_is_called_THEN_null_is_returned() {
        val expected: String? = null
        val event = Event("a")
        event.consume()

        val actual = event.consume()

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_event_WHEN_peek_is_called_THEN_value_is_returned() {
        val expected = "a"

        val actual = Event("a").peek()

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_consumed_event_WHEN_peek_is_called_THEN_value_is_returned() {
        val expected = "a"
        val event = Event("a")
        event.consume()

        val actual = event.peek()

        Assertions.assertEquals(expected, actual)
    }
}
