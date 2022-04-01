package org.fnives.test.showcase.ui

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.core.app.ApplicationProvider
import org.fnives.test.showcase.compose.screen.auth.AuthScreenTag

class ComposeLoginRobot(
    private val composeTestRule: ComposeTestRule,
) {

    fun setUsername(username: String): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.UsernameInput).performTextInput(username)
    }

    fun setPassword(password: String): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.PasswordInput).performTextInput(password)
    }

    fun assertPassword(password: String): ComposeLoginRobot = apply {
        with(composeTestRule) {
            onNodeWithTag(AuthScreenTag.PasswordVisibilityToggle).performClick()
            onNodeWithTag(AuthScreenTag.PasswordInput).assertTextContains(password)
        }
    }

    fun assertUsername(username: String): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.UsernameInput).assertTextContains(username)
    }

    fun clickOnLogin(): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.LoginButton).performClick()
    }

    fun assertLoading(): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.LoadingIndicator).assertIsDisplayed()
    }
    fun assertNotLoading(): ComposeLoginRobot = apply {
        composeTestRule.onAllNodesWithTag(AuthScreenTag.LoadingIndicator).assertCountEquals(0)
    }

    fun assertErrorIsShown(stringId: Int): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.LoginError)
            .assertTextContains(ApplicationProvider.getApplicationContext<Context>().resources.getString(stringId))
    }

}