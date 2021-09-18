package org.fnives.test.showcase.core.login

import org.fnives.test.showcase.core.di.koin.repositoryModule
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.koin.core.context.loadKoinModules
import org.koin.mp.KoinPlatformTools
import org.fnives.test.showcase.core.di.hilt.ReloadLoggedInModuleInjectModule

class LogoutUseCase(
    private val storage: UserDataLocalStorage,
    private val reloadLoggedInModuleInjectModule: ReloadLoggedInModuleInjectModule?
) {

    suspend fun invoke() {
        if (KoinPlatformTools.defaultContext().getOrNull() == null) {
            reloadLoggedInModuleInjectModule?.reload()
        } else {
            loadKoinModules(repositoryModule())
        }
        storage.session = null
    }
}
