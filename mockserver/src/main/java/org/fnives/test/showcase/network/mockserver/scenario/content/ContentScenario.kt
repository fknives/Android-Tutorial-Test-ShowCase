package org.fnives.test.showcase.network.mockserver.scenario.content

import org.fnives.test.showcase.network.mockserver.scenario.general.GenericScenario

sealed class ContentScenario : GenericScenario<ContentScenario>() {

    abstract val usingRefreshedToken: Boolean

    class Success(override val usingRefreshedToken: Boolean) : ContentScenario()
    class SuccessWithMissingFields(override val usingRefreshedToken: Boolean) : ContentScenario()
    class Unauthorized(override val usingRefreshedToken: Boolean) : ContentScenario()
    class Error(override val usingRefreshedToken: Boolean) : ContentScenario()
    class UnexpectedJsonAsSuccessResponse(override val usingRefreshedToken: Boolean) : ContentScenario()
    class MalformedJsonAsSuccessResponse(override val usingRefreshedToken: Boolean) : ContentScenario()
}
