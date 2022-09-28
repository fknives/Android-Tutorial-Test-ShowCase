package org.fnives.test.showcase.hilt.network.testutil

import dagger.BindsInstance
import dagger.Component
import org.fnives.test.showcase.hilt.network.auth.LoginRemoteSourceRefreshActionImplTest
import org.fnives.test.showcase.hilt.network.auth.LoginRemoteSourceTest
import org.fnives.test.showcase.hilt.network.content.ContentRemoteSourceImplTest
import org.fnives.test.showcase.hilt.network.content.SessionExpirationTest
import org.fnives.test.showcase.hilt.network.di.BindsBaseOkHttpClient
import org.fnives.test.showcase.hilt.network.di.HiltNetworkModule
import org.fnives.test.showcase.hilt.network.session.NetworkSessionExpirationListener
import org.fnives.test.showcase.hilt.network.session.NetworkSessionLocalStorage
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
