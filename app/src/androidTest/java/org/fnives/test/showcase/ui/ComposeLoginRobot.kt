package org.fnives.test.showcase.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
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
        composeTestRule.onNodeWithTag(AuthScreenTag.PasswordInput).assertTextContains(password)
    }

    fun assertUsername(username: String): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.UsernameInput).assertTextContains(username)
    }

    fun clickOnLogin(): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.LoginButton).performClick()
    }
}