package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import ru.topskiy.personalassistant.R

@Composable
fun BootstrapScreen(
    navController: NavHostController,
    viewModel: AppStateViewModel,
    uiState: AppStateUiState
) {
    LaunchedEffect(Unit) {
        val state = viewModel.getInitialState()
        val targetRoute = if (!state.onboardingDone) {
            ONBOARDING_ROUTE
        } else {
            MAIN_ROUTE
        }
        navController.navigate(targetRoute) {
            popUpTo(BOOTSTRAP_ROUTE) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.loading),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
