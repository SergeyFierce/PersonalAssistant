package ru.topskiy.personalassistant.core.ui

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import kotlinx.coroutines.CoroutineScope

const val BOOTSTRAP_ROUTE = "bootstrap"
const val ONBOARDING_ROUTE = "onboarding"
const val MAIN_ROUTE = "main"
const val MANAGE_SERVICES_ROUTE = "manage_services"
const val SETTINGS_ROUTE = "settings"

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: AppStateViewModel,
    uiState: AppStateUiState,
    darkTheme: Boolean,
    onOpenDrawer: () -> Unit,
    drawerActive: Boolean,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val dockListState = rememberLazyListState()
    NavHost(
        navController = navController,
        startDestination = BOOTSTRAP_ROUTE,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            )
        },
        exitTransition = {
            if (targetState.destination.route == MANAGE_SERVICES_ROUTE || targetState.destination.route == SETTINGS_ROUTE) {
                ExitTransition.None
            } else {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 250)
                )
            }
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 250)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 250)
            )
        }
    ) {
        composable(route = BOOTSTRAP_ROUTE) {
            BootstrapScreen(
                navController = navController,
                viewModel = viewModel,
                uiState = uiState
            )
        }

        composable(route = ONBOARDING_ROUTE) {
            OnboardingScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = MAIN_ROUTE) {
            ServicesMainScreen(
                navController = navController,
                viewModel = viewModel,
                uiState = uiState,
                darkTheme = darkTheme,
                onOpenDrawer = onOpenDrawer,
                drawerActive = drawerActive,
                scope = scope,
                dockListState = dockListState
            )
        }

        composable(
            route = MANAGE_SERVICES_ROUTE,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            }
        ) {
            ManageServicesScreen(
                navController = navController,
                viewModel = viewModel,
                uiState = uiState,
                darkTheme = darkTheme,
                onOpenDrawer = onOpenDrawer,
                drawerActive = drawerActive,
                scope = scope
            )
        }

        composable(
            route = SETTINGS_ROUTE,
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            }
        ) {
            SettingsScreen(
                navController = navController,
                viewModel = viewModel,
                uiState = uiState,
                darkTheme = darkTheme,
                onOpenDrawer = onOpenDrawer,
                drawerActive = drawerActive,
                scope = scope
            )
        }
    }
}
