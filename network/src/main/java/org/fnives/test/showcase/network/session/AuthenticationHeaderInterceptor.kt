package org.fnives.test.showcase.network.session

import okhttp3.Interceptor
import okhttp3.Response

internal class AuthenticationHeaderInterceptor(
    private val authenticationHeaderUtils: AuthenticationHeaderUtils
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(authenticationHeaderUtils.attachToken(chain.request()))
}
