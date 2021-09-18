package org.fnives.test.showcase.core.login.hilt

import dagger.BindsInstance
import dagger.Component
import org.fnives.test.showcase.core.di.hilt.CoreModule
import org.fnives.test.showcase.core.di.hilt.ReloadLoggedInModuleInjectModuleImpl
import org.fnives.test.showcase.core.session.SessionExpirationListener
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.network.di.hilt.HiltNetworkModule
import javax.inject.Singleton

@Singleton
@Component(modules = [CoreModule::class, HiltNetworkModule::class, ReloadLoggedInModuleInjectModuleImpl::class])
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