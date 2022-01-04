package org.fnives.test.showcase.core.shared

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Suppress("TestFunctionName")
internal class AnswerUtilsKtTest {

    @DisplayName("GIVEN network exception thrown WHEN wrapped into answer THEN answer error is returned")
    @Test
    fun networkExceptionThrownResultsInError() = runBlocking {
        val exception = NetworkException(Throwable())
        val expected = Answer.Error<Unit>(exception)

        val actual = wrapIntoAnswer<Unit> { throw exception }

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN parsing exception thrown WHEN wrapped into answer THEN answer error is returned")
    @Test
    fun parsingExceptionThrownResultsInError() = runBlocking {
        val exception = ParsingException(Throwable())
        val expected = Answer.Error<Unit>(exception)

        val actual = wrapIntoAnswer<Unit> { throw exception }

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN unexpected throwable thrown WHEN wrapped into answer THEN answer error is returned")
    @Test
    fun unexpectedExceptionThrownResultsInError() = runBlocking {
        val exception = Throwable()
        val expected = Answer.Error<Unit>(UnexpectedException(exception))

        val actual = wrapIntoAnswer<Unit> { throw exception }

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN string WHEN wrapped into answer THEN string answer is returned")
    @Test
    fun stringIsReturnedWrappedIntoSuccess() = runBlocking {
        val expected = Answer.Success("banan")

        val actual = wrapIntoAnswer { "banan" }

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN cancellation exception WHEN wrapped into answer THEN cancellation exception is thrown")
    @Test
    fun cancellationExceptionResultsInThrowingIt() {
        Assertions.assertThrows(CancellationException::class.java) {
            runBlocking { wrapIntoAnswer { throw CancellationException() } }
        }
    }

    @DisplayName("GIVEN success answer WHEN converted into resource THEN Resource success is returned")
    @Test
    fun successAnswerConvertsToSuccessResource() {
        val expected = Resource.Success("alma")

        val actual = Answer.Success("alma").mapIntoResource()

        Assertions.assertEquals(expected, actual)
    }

    @DisplayName("GIVEN error answer WHEN converted into resource THEN Resource error is returned")
    @Test
    fun errorAnswerConvertsToErrorResource() {
        val exception = Throwable()
        val expected = Resource.Error<Unit>(exception)

        val actual = Answer.Error<Unit>(exception).mapIntoResource()

        Assertions.assertEquals(expected, actual)
    }
}
