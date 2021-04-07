package org.fnives.test.showcase.di

import org.fnives.test.showcase.BuildConfig
import org.fnives.test.showcase.model.network.BaseUrl

object BaseUrlProvider {

    fun get() = BaseUrl(BuildConfig.BASE_URL)
}
