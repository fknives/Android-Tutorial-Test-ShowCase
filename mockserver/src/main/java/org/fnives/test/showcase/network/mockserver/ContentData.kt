package org.fnives.test.showcase.network.mockserver

import org.fnives.test.showcase.model.content.Content
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.ImageUrl
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthRequestMatchingChecker
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario

object ContentData {

    /**
     * Returned for [ContentScenario.Success]
     */
    val contentSuccess: List<Content> = listOf(
        Content(ContentId("1"), "title_1", "says_1", ImageUrl("img_1")),
        Content(ContentId("2"), "title_2", "says_2", ImageUrl("img_2")),
        Content(ContentId("3"), "title_3", "says_3", ImageUrl("img_3"))
    )

    /**
     * Returned for [ContentScenario.SuccessWithMissingFields]
     */
    val contentSuccessWithMissingFields: List<Content> = listOf(
        Content(ContentId("1"), "title_1", "says_1", ImageUrl("img_1"))
    )

    /**
     * Returned for [AuthScenario.Success]
     */
    val loginSuccessResponse = Session("login-access", "login-refresh")

    /**
     * Expected for [AuthScenario.Success]
     */
    fun createExpectedLoginRequestJson(username: String, password: String) =
        AuthRequestMatchingChecker.createExpectedJson(username = username, password = password)

    /**
     * Returned for [RefreshTokenScenario.Success]
     */
    val refreshSuccessResponse = Session("refreshed-access", "refreshed-refresh")
}
