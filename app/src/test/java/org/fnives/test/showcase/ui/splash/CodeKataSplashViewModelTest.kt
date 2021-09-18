package org.fnives.test.showcase.ui.splash

import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.testutils.InstantExecutorExtension
import org.fnives.test.showcase.testutils.TestMainDispatcher
import org.fnives.test.showcase.ui.shared.Event
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Disabled("CodeKata")
internal class CodeKataSplashViewModelTest {

    @BeforeEach
    fun setUp() {

    }

    @DisplayName("GIVEN not logged in user WHEN splash started THEN after half a second navigated to authentication")
    @Test
    fun loggedOutUserGoesToAuthentication() {

    }

    @DisplayName("GIVEN logged in user WHEN splash started THEN after half a second navigated to home")
    @Test
    fun loggedInUserGoestoHome() {

    }

    @DisplayName("GIVEN not logged in user WHEN splash started THEN before half a second no event is sent")
    @Test
    fun withoutEnoughTimeNoNavigationHappens() {

    }
}