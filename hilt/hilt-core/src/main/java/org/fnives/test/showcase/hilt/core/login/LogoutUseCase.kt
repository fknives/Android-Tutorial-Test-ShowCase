package org.fnives.test.showcase.hilt.core.login

import org.fnives.test.showcase.hilt.core.di.ReloadLoggedInModuleInjectModule
import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage

class LogoutUseCase(
    private val storage: UserDataLocalStorage,
    private val reloadLoggedInModuleInjectModule: ReloadLoggedInModuleInjectModule,
) {

    suspend fun invoke() {
        reloadLoggedInModuleInjectModule.reload()
        storage.session = null
    }
}
