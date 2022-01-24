package org.fnives.test.showcase.core.login

import org.fnives.test.showcase.core.storage.UserDataLocalStorage

class IsUserLoggedInUseCase(
    private val userDataLocalStorage: UserDataLocalStorage
) {

    fun invoke(): Boolean = userDataLocalStorage.session != null
}
