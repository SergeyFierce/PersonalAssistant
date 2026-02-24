package ru.topskiy.personalassistant.core.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
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
    val initialServiceId = remember {
        viewModel.getInitialServiceIdForMainScreen(
            enabled,
            uiState.favoriteService,
            uiState.lastService,
            homeServiceId
        )
    }
    var currentServiceId by remember { mutableStateOf(initialServiceId) }

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
            currentServiceId = homeServiceId
        }
    }

    val dockServices = ServiceRegistry.all.filter { it.id in enabled }
    val windowWidthClass = rememberWindowWidthClass()

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
                    favoriteServiceId = params.uiState.favoriteService,
                    darkTheme = params.darkTheme,
                    onSelectService = { id ->
                        params.viewModel.setLastService(id)
                        currentServiceId = id
                    },
                    dockListState = dockListState
                )
            }
        }
    ) { innerPadding ->
        val baseModifier = Modifier
            .fillMaxSize()
            .drawerOpenGestureOnContent(params.onOpenDrawer)
            .background(if (params.darkTheme) ScreenBackgroundDark else ScreenBackgroundLight)
            .padding(innerPadding)
            .padding(16.dp)

        when (windowWidthClass) {
            WindowWidthClass.Compact,
            WindowWidthClass.Medium -> {
                Box(
                    modifier = baseModifier,
                    contentAlignment = Alignment.Center
                ) {
                    ServicesMainContent(
                        params = params,
                        dockServices = dockServices,
                        currentServiceId = currentServiceId
                    )
                }
            }

            WindowWidthClass.Expanded -> {
                Row(
                    modifier = baseModifier,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ServicesSideListPane(
                        dockServices = dockServices,
                        currentServiceId = currentServiceId,
                        darkTheme = params.darkTheme,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        onSelectService = { id ->
                            params.viewModel.setLastService(id)
                            currentServiceId = id
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(2f),
                        contentAlignment = Alignment.Center
                    ) {
                        ServicesMainContent(
                            params = params,
                            dockServices = dockServices,
                            currentServiceId = currentServiceId
                        )
                    }
                }
            }
        }
    }
}

/**
 * Контент текущего сервиса на главном экране.
 *
 * Сейчас только [ServiceId.NOTES] имеет полноценный UI (NotesScreen) и служит
 * референсом для будущих сервисов: остальные сервисы по‑прежнему показывают
 * заглушку «Сервис в разработке», пока их домены не будут реализованы по
 * тому же шаблону (Repository → UseCase → ViewModel → Screen).
 */
@Composable
private fun ServicesMainContent(
    params: ScreenParams,
    dockServices: List<ru.topskiy.personalassistant.core.model.AppService>,
    currentServiceId: ServiceId
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
        when (serviceId) {
            ServiceId.NOTES -> {
                val mainEntry = params.navController.getBackStackEntry(MAIN_ROUTE)
                val notesViewModel: NotesViewModel = hiltViewModel(mainEntry!!)
                NotesScreen(
                    params = params,
                    viewModel = notesViewModel
                )
            }
            else -> Text(
                text = stringResource(R.string.service_in_development),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ServicesSideListPane(
    dockServices: List<ru.topskiy.personalassistant.core.model.AppService>,
    currentServiceId: ServiceId,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    onSelectService: (ServiceId) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (darkTheme) {
                    ScreenBackgroundDark.copy(alpha = 0.9f)
                } else {
                    ScreenBackgroundLight.copy(alpha = 0.9f)
                }
            )
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        dockServices.forEach { appService ->
            val isSelected = appService.id == currentServiceId
            val backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            } else {
                Color.Transparent
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .clickable { onSelectService(appService.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(id = appService.titleResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

