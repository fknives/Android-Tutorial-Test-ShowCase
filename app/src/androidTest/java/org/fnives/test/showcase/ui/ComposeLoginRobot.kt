package org.fnives.test.showcase.ui

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.fnives.test.showcase.ui.compose.screen.auth.AuthScreenTag

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
        composeTestRule.onNodeWithTag(AuthScreenTag.PasswordInput).assertTextEquals(password)
    }

    fun assertUsername(username: String): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.UsernameInput).assertTextEquals(username)
    }

    fun clickOnLogin(): ComposeLoginRobot = apply {
        composeTestRule.onNodeWithTag(AuthScreenTag.LoginButton).performClick()
    }
}