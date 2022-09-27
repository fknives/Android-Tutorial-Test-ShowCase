package org.fnives.test.showcase.hilt.core.login

import org.fnives.test.showcase.hilt.core.storage.UserDataLocalStorage
import javax.inject.Inject

class IsUserLoggedInUseCase @Inject internal constructor(
    private val userDataLocalStorage: UserDataLocalStorage,
) {

    fun invoke(): Boolean = userDataLocalStorage.session != null
}
