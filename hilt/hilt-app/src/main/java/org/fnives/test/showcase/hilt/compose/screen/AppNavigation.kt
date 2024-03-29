package org.fnives.test.showcase.hilt.compose.screen

import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.fnives.test.showcase.hilt.compose.di.ComposeEntryPoint.AppNavigationDependencies
import org.fnives.test.showcase.hilt.compose.di.ComposeEntryPoint.rememberEntryPoint
import org.fnives.test.showcase.hilt.compose.screen.auth.AuthScreen
import org.fnives.test.showcase.hilt.compose.screen.auth.rememberAuthScreenState
import org.fnives.test.showcase.hilt.compose.screen.home.HomeScreen
import org.fnives.test.showcase.hilt.compose.screen.home.rememberHomeScreenState
import org.fnives.test.showcase.hilt.compose.screen.splash.SplashScreen
import org.fnives.test.showcase.hilt.core.login.IsUserLoggedInUseCase

@Composable
fun AppNavigation(
    navigationDependencies: AppNavigationDependencies = rememberEntryPoint()
) = AppNavigation(navigationDependencies.isUserLoggedInUseCase)

@Composable
fun AppNavigation(isUserLogeInUseCase: IsUserLoggedInUseCase) {
    val navController = rememberNavController()

    LaunchedEffect(isUserLogeInUseCase) {
        val loginStateRoute = if (isUserLogeInUseCase.invoke()) RouteTag.HOME else RouteTag.AUTH
        if (navController.currentDestination?.route == loginStateRoute) return@LaunchedEffect
        delay(500)
        navController.navigate(
            route = loginStateRoute,
            navOptions = NavOptions.Builder().setPopUpTo(route = RouteTag.SPLASH, inclusive = true).build()
        )
    }

    NavHost(
        navController,
        startDestination = RouteTag.SPLASH,
        modifier = Modifier.background(MaterialTheme.colors.surface)
    ) {
        composable(RouteTag.SPLASH) { SplashScreen() }
        composable(RouteTag.AUTH) {
            AuthScreen(
                modifier = Modifier.testTag(AppNavigationTag.AuthScreen),
                authScreenState = rememberAuthScreenState(
                    onLoginSuccess = {
                        navController.navigate(
                            route = RouteTag.HOME,
                            navOptions = NavOptions.Builder().setPopUpTo(route = RouteTag.AUTH, inclusive = true).build()
                        )
                    }
                )
            )
        }
        composable(RouteTag.HOME) {
            HomeScreen(
                modifier = Modifier.testTag(AppNavigationTag.HomeScreen),
                homeScreenState = rememberHomeScreenState(
                    onLogout = {
                        navController.navigate(
                            route = RouteTag.AUTH,
                            navOptions = NavOptions.Builder().setPopUpTo(route = RouteTag.HOME, inclusive = true).build()
                        )
                    }
                )
            )
        }
    }
}

object RouteTag {
    const val HOME = "Home"
    const val AUTH = "Auth"
    const val SPLASH = "Splash"
}

object AppNavigationTag {
    const val AuthScreen = "AppNavigationTag.AuthScreen"
    const val HomeScreen = "AppNavigationTag.HomeScreen"
}
