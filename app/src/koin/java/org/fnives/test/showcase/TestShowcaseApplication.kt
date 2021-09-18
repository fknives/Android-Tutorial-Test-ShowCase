package org.fnives.test.showcase

import android.app.Application
import org.fnives.test.showcase.di.BaseUrlProvider
import org.fnives.test.showcase.di.createAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TestShowcaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TestShowcaseApplication)
            modules(createAppModules(BaseUrlProvider.get()))
        }
    }
}
