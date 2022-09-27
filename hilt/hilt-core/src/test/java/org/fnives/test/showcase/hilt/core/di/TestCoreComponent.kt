package org.fnives.test.showcase.hilt.core.di

import dagger.BindsInstance
import dagger.Component
import org.fnives.test.showcase.hilt.core.login.LogoutUseCaseTest
import org.fnives.test.showcase.hilt.core.session.SessionExpirationListener
import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.hilt.network.di.BindsBaseOkHttpClient
import org.fnives.test.showcase.hilt.network.di.HiltNetworkModule
import javax.inject.Singleton

@Singleton
@Component(modules = [CoreModule::class, HiltNetworkModule::class, ReloadLoggedInModuleInjectModuleImpl::class, BindsBaseOkHttpClient::class])
internal interface TestCoreComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun setBaseUrl(baseUrl: String): Builder

        @BindsInstance
        fun setEnableLogging(enableLogging: Boolean): Builder

        @BindsInstance
        fun setSessionExpirationListener(listener: SessionExpirationListener): Builder

        @BindsInstance
        fun setUserDataLocalStorage(storage: UserDataLocalStorage): Builder

        fun build(): TestCoreComponent
    }

    fun inject(logoutUseCaseTest: LogoutUseCaseTest)
}
