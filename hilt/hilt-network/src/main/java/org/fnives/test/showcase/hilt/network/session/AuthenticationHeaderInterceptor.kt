package org.fnives.test.showcase.hilt.network.session

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

internal class AuthenticationHeaderInterceptor @Inject internal constructor(
    private val authenticationHeaderUtils: AuthenticationHeaderUtils
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(authenticationHeaderUtils.attachToken(chain.request()))
}
