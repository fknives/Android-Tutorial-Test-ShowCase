package org.fnives.test.showcase.network.mockserver.scenario.refresh

import org.fnives.test.showcase.network.mockserver.scenario.general.GenericScenario

sealed class RefreshTokenScenario : GenericScenario<RefreshTokenScenario>() {
    object Success : RefreshTokenScenario()
    object Error : RefreshTokenScenario()
    object UnexpectedJsonAsSuccessResponse : RefreshTokenScenario()
    object MalformedJson : RefreshTokenScenario()
}
