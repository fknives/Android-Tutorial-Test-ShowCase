package org.fnives.test.showcase.core.shared

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@Suppress("TestFunctionName")
internal class AnswerUtilsKtTest {

    @Test
    fun GIVEN_network_exception_thrown_WHEN_wrapped_into_answer_THEN_answer_error_is_returned() = runBlocking {
        val exception = NetworkException(Throwable())
        val expected = Answer.Error<Unit>(exception)

        val actual = wrapIntoAnswer<Unit> { throw exception }

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_parsing_exception_thrown_WHEN_wrapped_into_answer_THEN_answer_error_is_returned() = runBlocking {
        val exception = ParsingException(Throwable())
        val expected = Answer.Error<Unit>(exception)

        val actual = wrapIntoAnswer<Unit> { throw exception }

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_parsing_throwable_thrown_WHEN_wrapped_into_answer_THEN_answer_error_is_returned() = runBlocking {
        val exception = Throwable()
        val expected = Answer.Error<Unit>(UnexpectedException(exception))

        val actual = wrapIntoAnswer<Unit> { throw exception }

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_string_WHEN_wrapped_into_answer_THEN_string_answer_is_returned() = runBlocking {
        val expected = Answer.Success("banan")

        val actual = wrapIntoAnswer { "banan" }

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_cancellation_exception_WHEN_wrapped_into_answer_THEN_cancellation_exception_is_thrown() {
        Assertions.assertThrows(CancellationException::class.java) {
            runBlocking { wrapIntoAnswer { throw CancellationException() } }
        }
    }

    @Test
    fun GIVEN_success_answer_WHEN_converted_into_resource_THEN_Resource_success_is_returned() {
        val expected = Resource.Success("alma")

        val actual = Answer.Success("alma").mapIntoResource()

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_error_answer_WHEN_converted_into_resource_THEN_Resource_error_is_returned() {
        val exception = Throwable()
        val expected = Resource.Error<Unit>(exception)

        val actual = Answer.Error<Unit>(exception).mapIntoResource()

        Assertions.assertEquals(expected, actual)
    }
}
