package org.fnives.test.showcase.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import org.fnives.test.showcase.compose.screen.AppNavigationTag

class ComposeScreenRobot(
    private val composeTestRule: ComposeTestRule,
) {

    fun assertHomeScreen(): ComposeScreenRobot = apply {
        composeTestRule.onNodeWithTag(AppNavigationTag.HomeScreen).assertIsDisplayed()
    }

    fun assertAuthScreen(): ComposeScreenRobot = apply {
        composeTestRule.onNodeWithTag(AppNavigationTag.AuthScreen).assertIsDisplayed()
    }
}