package ru.topskiy.personalassistant.core.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope

const val BOOTSTRAP_ROUTE = "bootstrap"
const val ONBOARDING_ROUTE = "onboarding"
const val MAIN_ROUTE = "main"
const val MANAGE_SERVICES_ROUTE = "manage_services"
const val SETTINGS_ROUTE = "settings"

private const val SCREEN_NAME_ONBOARDING = "onboarding"
private const val SCREEN_NAME_MAIN = "main"
private const val SCREEN_NAME_SETTINGS = "settings"
private const val SCREEN_NAME_MANAGE_SERVICES = "manage_services"

// ——— Общие анимации (навигация и AnimatedContent) ———

private val navSlideTween250 = tween<IntOffset>(durationMillis = 250)
private val navSlideTween320 = tween<IntOffset>(durationMillis = 320, easing = FastOutSlowInEasing)
private val horizontalSlideTween280 = tween<IntOffset>(durationMillis = 280, easing = FastOutSlowInEasing)
private val fadeTween200 = tween<Float>(durationMillis = 200)

/** Enter/exit для перехода «вперёд» (контент приходит справа, уходит влево). */
private fun navEnterTransition() =
    slideInHorizontally(initialOffsetX = { it }, animationSpec = navSlideTween250)

private fun navExitTransition() =
    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = navSlideTween250)

private fun navPopEnterTransition() =
    slideInHorizontally(initialOffsetX = { -it }, animationSpec = navSlideTween250)

private fun navPopExitTransition() =
    slideOutHorizontally(targetOffsetX = { it }, animationSpec = navSlideTween250)

/** Выезд вправо (drawer/экран настроек и каталога). */
private fun navDrawerLikeExitTransition() =
    slideOutHorizontally(targetOffsetX = { it }, animationSpec = navSlideTween320)

/**
 * ContentTransform для горизонтального переключения контента (например, смена сервиса в доке).
 * [forward] = true: новый контент справа (слайд влево), старый уезжает влево.
 */
fun horizontalSlideWithFadeContentTransform(forward: Boolean): ContentTransform {
    val enterSlide = if (forward) {
        slideInHorizontally(initialOffsetX = { it }, animationSpec = horizontalSlideTween280)
    } else {
        slideInHorizontally(initialOffsetX = { -it }, animationSpec = horizontalSlideTween280)
    }
    val exitSlide = if (forward) {
        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = horizontalSlideTween280)
    } else {
        slideOutHorizontally(targetOffsetX = { it }, animationSpec = horizontalSlideTween280)
    }
    return (enterSlide + fadeIn(animationSpec = fadeTween200)) togetherWith
        (exitSlide + fadeOut(animationSpec = fadeTween200))
}

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
    val screenParams = ScreenParams(
        navController = navController,
        viewModel = viewModel,
        uiState = uiState,
        darkTheme = darkTheme,
        onOpenDrawer = onOpenDrawer,
        drawerActive = drawerActive,
        scope = scope
    )
    val dockListState = rememberLazyListState()
    val context = LocalContext.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        val route = currentBackStackEntry?.destination?.route ?: return@LaunchedEffect
        if (route == BOOTSTRAP_ROUTE) return@LaunchedEffect
        val screenName = when (route) {
            ONBOARDING_ROUTE -> SCREEN_NAME_ONBOARDING
            MAIN_ROUTE -> SCREEN_NAME_MAIN
            SETTINGS_ROUTE -> SCREEN_NAME_SETTINGS
            MANAGE_SERVICES_ROUTE -> SCREEN_NAME_MANAGE_SERVICES
            else -> return@LaunchedEffect
        }
        FirebaseAnalytics.getInstance(context).logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundleOf(FirebaseAnalytics.Param.SCREEN_NAME to screenName)
        )
    }
    NavHost(
        navController = navController,
        startDestination = BOOTSTRAP_ROUTE,
        modifier = modifier,
        enterTransition = { navEnterTransition() },
        exitTransition = {
            if (targetState.destination.route == MANAGE_SERVICES_ROUTE || targetState.destination.route == SETTINGS_ROUTE) {
                ExitTransition.None
            } else {
                navExitTransition()
            }
        },
        popEnterTransition = { navPopEnterTransition() },
        popExitTransition = { navPopExitTransition() }
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
                params = screenParams,
                dockListState = dockListState
            )
        }

        composable(
            route = MANAGE_SERVICES_ROUTE,
            exitTransition = { navDrawerLikeExitTransition() },
            popExitTransition = { navDrawerLikeExitTransition() }
        ) {
            ManageServicesScreen(params = screenParams)
        }

        composable(
            route = SETTINGS_ROUTE,
            exitTransition = { navDrawerLikeExitTransition() },
            popExitTransition = { navDrawerLikeExitTransition() }
        ) {
            SettingsScreen(params = screenParams)
        }
    }
}
