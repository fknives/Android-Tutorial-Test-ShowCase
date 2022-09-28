package org.fnives.test.showcase.hilt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.fnives.test.showcase.hilt.network.di.BindsBaseOkHttpClient
import org.fnives.test.showcase.hilt.network.di.SessionLessQualifier
import org.fnives.test.showcase.hilt.network.shared.PlatformInterceptor
import org.fnives.test.showcase.hilt.network.testutil.HttpsConfigurationModuleTemplate
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BindsBaseOkHttpClient::class]
)
object HttpsConfigurationModule {

    @Provides
    @Singleton
    @SessionLessQualifier
    fun bindsBaseOkHttpClient(enableLogging: Boolean, platformInterceptor: PlatformInterceptor) =
        HttpsConfigurationModuleTemplate.bindsBaseOkHttpClient(enableLogging, platformInterceptor)
}
