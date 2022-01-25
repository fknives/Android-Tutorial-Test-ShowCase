package org.fnives.test.showcase.ui.splash

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Disabled("CodeKata")
@OptIn(ExperimentalCoroutinesApi::class)
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
    fun loggedInUserGoesToHome() {
    }

    @DisplayName("GIVEN not logged in user WHEN splash started THEN before half a second no event is sent")
    @Test
    fun withoutEnoughTimeNoNavigationHappens() {
    }
}
