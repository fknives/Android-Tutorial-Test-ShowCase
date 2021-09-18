package org.fnives.test.showcase.network

import dagger.BindsInstance
import dagger.Component
import org.fnives.test.showcase.network.auth.hilt.LoginRemoteSourceRefreshActionImplTest
import org.fnives.test.showcase.network.auth.hilt.LoginRemoteSourceTest
import org.fnives.test.showcase.network.content.hilt.ContentRemoteSourceImplTest
import org.fnives.test.showcase.network.content.hilt.SessionExpirationTest
import org.fnives.test.showcase.network.di.hilt.BindsBaseOkHttpClient
import org.fnives.test.showcase.network.di.hilt.HiltNetworkModule
import org.fnives.test.showcase.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import javax.inject.Singleton

@Singleton
@Component(modules = [HiltNetworkModule::class, BindsBaseOkHttpClient::class])
interface TestNetworkComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun setBaseUrl(baseUrl: String): Builder

        @BindsInstance
        fun setEnableLogging(enableLogging: Boolean): Builder

        @BindsInstance
        fun setNetworkSessionLocalStorage(storage: NetworkSessionLocalStorage): Builder

        @BindsInstance
        fun setNetworkSessionExpirationListener(listener: NetworkSessionExpirationListener): Builder

        fun build(): TestNetworkComponent
    }

    fun inject(contentRemoteSourceImplTest: ContentRemoteSourceImplTest)

    fun inject(sessionExpirationTest: SessionExpirationTest)

    fun inject(loginRemoteSourceRefreshActionImplTest: LoginRemoteSourceRefreshActionImplTest)

    fun inject(loginRemoteSourceTest: LoginRemoteSourceTest)
}
