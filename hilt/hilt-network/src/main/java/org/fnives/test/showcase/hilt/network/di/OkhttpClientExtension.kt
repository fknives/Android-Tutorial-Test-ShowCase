package org.fnives.test.showcase.hilt.network.di

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal fun OkHttpClient.Builder.setupLogging(enable: Boolean) = run {
    if (enable) {
        addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
    } else {
        this
    }
}
