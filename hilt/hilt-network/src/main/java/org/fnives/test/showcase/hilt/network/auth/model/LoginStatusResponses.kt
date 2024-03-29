package org.fnives.test.showcase.hilt.network.auth.model

import org.fnives.test.showcase.model.session.Session

sealed class LoginStatusResponses {
    data class Success(val session: Session) : LoginStatusResponses()
    object InvalidCredentials : LoginStatusResponses()
}
