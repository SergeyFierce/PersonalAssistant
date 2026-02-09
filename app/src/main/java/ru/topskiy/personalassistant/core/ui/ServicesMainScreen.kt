package ru.topskiy.personalassistant.core.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry
import ru.topskiy.personalassistant.ui.theme.ScreenBackgroundDark
import ru.topskiy.personalassistant.ui.theme.ScreenBackgroundLight
import ru.topskiy.personalassistant.ui.theme.TopAppBarDark
import ru.topskiy.personalassistant.ui.theme.TopAppBarLight

private const val PRESS_AGAIN_TO_EXIT_INTERVAL_MS = 2000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesMainScreen(
    params: ScreenParams,
    dockListState: LazyListState
) {
    val uiState = params.uiState
    val viewModel = params.viewModel
    val enabled = uiState.enabledServices
    val homeServiceId = uiState.homeServiceId()
    val currentServiceId = remember(uiState) {
        val last = uiState.lastService
        when {
            last != null && last in enabled -> last
            else -> homeServiceId
        }
    }

    var lastBackPressTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    val pressAgainToExitMessage = stringResource(R.string.press_again_to_exit)

    BackHandler(enabled = !params.drawerActive) {
        if (currentServiceId == homeServiceId) {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < PRESS_AGAIN_TO_EXIT_INTERVAL_MS) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, pressAgainToExitMessage, Toast.LENGTH_SHORT).show()
            }
        } else {
            params.viewModel.setLastService(homeServiceId)
        }
    }

    val dockServices = ServiceRegistry.all.filter { it.id in enabled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = currentServiceId,
                        transitionSpec = {
                            val initialIndex = dockServices.indexOfFirst { it.id == initialState }
                            val targetIndex = dockServices.indexOfFirst { it.id == targetState }
                            horizontalSlideWithFadeContentTransform(forward = targetIndex > initialIndex)
                        },
                        label = "service_title"
                    ) { serviceId ->
                        Text(stringResource(ServiceRegistry.byId(serviceId).titleResId))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = params.onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (params.darkTheme) TopAppBarDark else TopAppBarLight,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
            )
        },
        bottomBar = {
            if (dockServices.size > 1) {
                DockBar(
                    dockServices = dockServices,
                    currentServiceId = currentServiceId,
                    onSelectService = { id -> params.viewModel.setLastService(id) },
                    dockListState = dockListState
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawerOpenGestureOnContent(params.onOpenDrawer)
                .background(if (params.darkTheme) ScreenBackgroundDark else ScreenBackgroundLight)
                .padding(16.dp)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentServiceId,
                transitionSpec = {
                    val initialIndex = dockServices.indexOfFirst { it.id == initialState }
                    val targetIndex = dockServices.indexOfFirst { it.id == targetState }
                    horizontalSlideWithFadeContentTransform(forward = targetIndex > initialIndex)
                },
                label = "service_content"
            ) { serviceId ->
                Text(
                    text = stringResource(R.string.service_in_development),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
