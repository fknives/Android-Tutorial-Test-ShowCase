package org.fnives.test.showcase.core.login

import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import javax.inject.Inject

class IsUserLoggedInUseCase @Inject constructor(
    private val userDataLocalStorage: UserDataLocalStorage
) {

    fun invoke(): Boolean = userDataLocalStorage.session != null
}
