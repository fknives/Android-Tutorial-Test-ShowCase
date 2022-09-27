package org.fnives.test.showcase.hilt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.fnives.test.showcase.hilt.test.shared.di.TestBaseUrlHolder

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BaseUrlModule::class]
)
object TestBaseUrlModule {

    @Provides
    fun provideBaseUrl(): String = TestBaseUrlHolder.url
}
