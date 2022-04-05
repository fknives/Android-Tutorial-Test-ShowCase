package org.fnives.test.showcase.ui

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import org.fnives.test.showcase.compose.screen.AppNavigationTag

class ComposeNavigationRobot(
    private val composeTestRule: ComposeTestRule,
) {

    fun assertHomeScreen(): ComposeNavigationRobot = apply {
        composeTestRule.onNodeWithTag(AppNavigationTag.HomeScreen).assertExists()
    }

    fun assertAuthScreen(): ComposeNavigationRobot = apply {
        composeTestRule.onNodeWithTag(AppNavigationTag.AuthScreen).assertExists()
    }
}
