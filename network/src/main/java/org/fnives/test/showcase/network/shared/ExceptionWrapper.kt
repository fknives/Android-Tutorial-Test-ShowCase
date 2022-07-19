package org.fnives.test.showcase.network.shared

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import java.io.EOFException

internal object ExceptionWrapper {

    @Suppress("RethrowCaughtException")
    @Throws(NetworkException::class, ParsingException::class)
    suspend fun <T> wrap(request: suspend () -> T) = try {
        request()
    } catch (jsonDataException: JsonDataException) {
        System.err.println("got response from request, thrown exception: jsonDataException = $jsonDataException")
        throw ParsingException(jsonDataException)
    } catch (jsonEncodingException: JsonEncodingException) {
        System.err.println("got response from request, thrown exception: jsonEncodingException = $jsonEncodingException")
        throw ParsingException(jsonEncodingException)
    } catch (eofException: EOFException) {
        System.err.println("got response from request, thrown exception: eofException = $eofException")
        throw ParsingException(eofException)
    } catch (parsingException: ParsingException) {
        System.err.println("got response from request, thrown exception: parsingException = $parsingException")
        throw parsingException
    } catch (networkException: NetworkException) {
        System.err.println("got response from request, thrown exception: networkException = $networkException")
        throw networkException
    } catch (throwable: Throwable) {
        System.err.println("got response from request, thrown exception: throwable = $throwable")
        throw NetworkException(throwable)
    }
}
