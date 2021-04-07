package org.fnives.test.showcase.network.mockserver.scenario.auth

import org.fnives.test.showcase.network.mockserver.scenario.general.GenericScenario

sealed class AuthScenario : GenericScenario<AuthScenario>() {

    abstract val username: String
    abstract val password: String

    class Success(override val username: String, override val password: String) : AuthScenario()
    class InvalidCredentials(override val username: String, override val password: String) : AuthScenario()
    class GenericError(override val username: String, override val password: String) : AuthScenario()
    class UnexpectedJsonAsSuccessResponse(override val username: String, override val password: String) : AuthScenario()
    class MalformedJsonAsSuccessResponse(override val username: String, override val password: String) : AuthScenario()
}
