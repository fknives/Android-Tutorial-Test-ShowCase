package org.fnives.test.showcase.network.session

import okhttp3.Request

internal class AuthenticationHeaderUtils(
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
