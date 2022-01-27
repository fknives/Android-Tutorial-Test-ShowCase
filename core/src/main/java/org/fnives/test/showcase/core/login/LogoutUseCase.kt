package org.fnives.test.showcase.core.login

import org.fnives.test.showcase.core.di.repositoryModule
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.koin.core.context.loadKoinModules

class LogoutUseCase(
    private val storage: UserDataLocalStorage
) {

    suspend fun invoke() {
        loadKoinModules(repositoryModule())
        storage.session = null
    }
}
