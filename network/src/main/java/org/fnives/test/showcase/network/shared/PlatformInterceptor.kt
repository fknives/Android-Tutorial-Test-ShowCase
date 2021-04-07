package org.fnives.test.showcase.network.shared

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class PlatformInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("Platform", "Android").build())
}
