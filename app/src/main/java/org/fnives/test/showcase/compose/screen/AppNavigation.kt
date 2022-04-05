package org.fnives.test.showcase.compose.screen

import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.fnives.test.showcase.compose.screen.auth.AuthScreen
import org.fnives.test.showcase.compose.screen.auth.rememberAuthScreenState
import org.fnives.test.showcase.compose.screen.home.HomeScreen
import org.fnives.test.showcase.compose.screen.home.rememberHomeScreenState
import org.fnives.test.showcase.compose.screen.splash.SplashScreen
import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.koin.androidx.compose.get

@Composable
fun AppNavigation(isUserLogeInUseCase: IsUserLoggedInUseCase = get()) {
    val navController = rememberNavController()

    LaunchedEffect(isUserLogeInUseCase) {
        delay(500)
        navController.navigate(if (isUserLogeInUseCase.invoke()) "Home" else "Auth")
    }

    NavHost(
        navController,
        startDestination = "Splash",
        modifier = Modifier.background(MaterialTheme.colors.surface)
    ) {
        composable("Splash") { SplashScreen() }
        composable("Auth") {
            AuthScreen(
                modifier = Modifier.testTag(AppNavigationTag.AuthScreen),
                authScreenState = rememberAuthScreenState(
                    onLoginSuccess = { navController.navigate("Home") }
                )
            )
        }
        composable("Home") {
            HomeScreen(
                modifier = Modifier.testTag(AppNavigationTag.HomeScreen),
                homeScreenState = rememberHomeScreenState(
                    onLogout = { navController.navigate("Auth") }
                )
            )
        }
    }
}

object AppNavigationTag {
    const val AuthScreen = "AppNavigationTag.AuthScreen"
    const val HomeScreen = "AppNavigationTag.HomeScreen"
}
