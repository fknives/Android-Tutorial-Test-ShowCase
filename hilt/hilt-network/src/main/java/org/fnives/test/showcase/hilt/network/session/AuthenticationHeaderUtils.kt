package org.fnives.test.showcase.hilt.network.session

import okhttp3.Request
import javax.inject.Inject

internal class AuthenticationHeaderUtils @Inject internal constructor(
    private val networkSessionLocalStorage: NetworkSessionLocalStorage
) {

    fun hasToken(okhttpRequest: Request): Boolean =
        okhttpRequest.header(KEY) == networkSessionLocalStorage.session?.accessToken

    fun attachToken(okhttpRequest: Request): Request =
        okhttpRequest.newBuilder()
            .header(KEY, networkSessionLocalStorage.session?.accessToken.orEmpty()).build()

    companion object {
        private const val KEY = "Authorization"
    }
}
