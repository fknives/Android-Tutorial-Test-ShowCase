package org.fnives.test.showcase

import android.app.Application
import org.fnives.test.showcase.di.createAppModules
import org.fnives.test.showcase.model.network.BaseUrl
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TestShowcaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val baseUrl = BaseUrl(BuildConfig.BASE_URL)
        startKoin {
            androidContext(this@TestShowcaseApplication)
            modules(createAppModules(baseUrl))
        }
    }
}
