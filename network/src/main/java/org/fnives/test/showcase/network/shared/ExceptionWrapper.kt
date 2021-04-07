package org.fnives.test.showcase.network.shared

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import java.io.EOFException

internal object ExceptionWrapper {

    @Throws(NetworkException::class, ParsingException::class)
    suspend fun <T> wrap(request: suspend () -> T) = try {
        request()
    } catch (jsonDataException: JsonDataException) {
        throw ParsingException(jsonDataException)
    } catch (jsonEncodingException: JsonEncodingException) {
        throw ParsingException(jsonEncodingException)
    } catch (eofException: EOFException) {
        throw ParsingException(eofException)
    } catch (parsingException: ParsingException) {
        throw parsingException
    } catch (networkException: NetworkException) {
        throw networkException
    } catch (throwable: Throwable) {
        throw NetworkException(throwable)
    }
}
