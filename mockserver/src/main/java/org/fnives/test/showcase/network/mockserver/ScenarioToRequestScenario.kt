package org.fnives.test.showcase.network.mockserver

import org.fnives.test.showcase.network.mockserver.scenario.RequestScenario
import org.fnives.test.showcase.network.mockserver.scenario.RequestScenarioChain
import org.fnives.test.showcase.network.mockserver.scenario.SpecificRequestScenario
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthRequestMatchingChecker
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.network.mockserver.scenario.auth.CreateAuthInvalidCredentialsResponse
import org.fnives.test.showcase.network.mockserver.scenario.auth.CreateAuthSuccessResponse
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentRequestMatchingChecker
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.content.CreateContentSuccessResponse
import org.fnives.test.showcase.network.mockserver.scenario.content.CreateContentSuccessWithMissingFields
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateGeneralErrorResponse
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateGenericSuccessResponseByJson
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateMalformedJsonSuccessResponse
import org.fnives.test.showcase.network.mockserver.scenario.createresponse.CreateUnauthorizedResponse
import org.fnives.test.showcase.network.mockserver.scenario.general.GenericScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.CreateRefreshResponse
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshRequestMatchingChecker
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario

internal class ScenarioToRequestScenario {

    fun get(authScenario: AuthScenario, validateArguments: Boolean): RequestScenario =
        wrap(authScenario, ::convert, validateArguments)

    fun get(contentScenario: ContentScenario, validateArguments: Boolean): RequestScenario =
        wrap(contentScenario, ::convert, validateArguments)

    fun get(refreshTokenScenario: RefreshTokenScenario, validateArguments: Boolean): RequestScenario =
        wrap(refreshTokenScenario, ::convert, validateArguments)

    private fun convert(validateArguments: Boolean, authScenario: AuthScenario): RequestScenario {
        val createResponse = when (authScenario) {
            is AuthScenario.GenericError -> CreateGeneralErrorResponse()
            is AuthScenario.InvalidCredentials -> CreateAuthInvalidCredentialsResponse()
            is AuthScenario.Success -> CreateAuthSuccessResponse()
            is AuthScenario.MalformedJsonAsSuccessResponse -> CreateMalformedJsonSuccessResponse()
            is AuthScenario.UnexpectedJsonAsSuccessResponse -> CreateGenericSuccessResponseByJson("[]")
        }
        val requestMatchingChecker = AuthRequestMatchingChecker(authScenario, validateArguments)
        return SpecificRequestScenario(requestMatchingChecker, createResponse)
    }

    private fun convert(validateArguments: Boolean, contentScenario: ContentScenario): RequestScenario {
        val createResponse = when (contentScenario) {
            is ContentScenario.Error -> CreateGeneralErrorResponse()
            is ContentScenario.Success -> CreateContentSuccessResponse()
            is ContentScenario.SuccessWithMissingFields -> CreateContentSuccessWithMissingFields()
            is ContentScenario.Unauthorized -> CreateUnauthorizedResponse()
            is ContentScenario.MalformedJsonAsSuccessResponse -> CreateMalformedJsonSuccessResponse()
            is ContentScenario.UnexpectedJsonAsSuccessResponse -> CreateGenericSuccessResponseByJson("{}")
        }
        val requestMatchingChecker = ContentRequestMatchingChecker(contentScenario, validateArguments)
        return SpecificRequestScenario(requestMatchingChecker, createResponse)
    }

    private fun convert(validateArguments: Boolean, refreshTokenScenario: RefreshTokenScenario): RequestScenario {
        val contentResponse = when (refreshTokenScenario) {
            RefreshTokenScenario.Error -> CreateGeneralErrorResponse()
            RefreshTokenScenario.Success -> CreateRefreshResponse()
            RefreshTokenScenario.UnexpectedJsonAsSuccessResponse -> CreateGenericSuccessResponseByJson("{}")
            RefreshTokenScenario.MalformedJson -> CreateMalformedJsonSuccessResponse()
        }
        val requestMatchingChecker = RefreshRequestMatchingChecker(validateArguments)
        return SpecificRequestScenario(requestMatchingChecker, contentResponse)
    }

    private fun <T : GenericScenario<T>> wrap(
        scenario: T,
        convert: (Boolean, T) -> RequestScenario,
        validateArguments: Boolean
    ): RequestScenario {
        val requestScenario = convert(validateArguments, scenario)
        val previousScenario = scenario.previousScenario ?: return requestScenario

        return RequestScenarioChain(current = convert(validateArguments, previousScenario), next = requestScenario)
    }
}
