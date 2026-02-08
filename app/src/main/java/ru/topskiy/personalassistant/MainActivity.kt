package ru.topskiy.personalassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.topskiy.personalassistant.core.ui.AppNavHost
import ru.topskiy.personalassistant.core.ui.AppStateViewModel
import ru.topskiy.personalassistant.core.ui.DrawerContent
import ru.topskiy.personalassistant.ui.theme.PersonalAssistantTheme
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState

@Composable
private fun DrawerBackHandler(
    drawerActive: Boolean,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    BackHandler(enabled = drawerActive) {
        scope.launch { drawerState.close() }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppStateViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(initialValue = "system")
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            PersonalAssistantTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val drawerActive = drawerState.currentValue == DrawerValue.Open ||
                    drawerState.targetValue == DrawerValue.Open
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    viewModel.messageEvent.collect { messageResId ->
                        snackbarHostState.showSnackbar(context.getString(messageResId))
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = true,
                        drawerContent = {
                            DrawerContent(
                                navController = navController,
                                uiState = uiState,
                                darkTheme = darkTheme,
                                onToggleTheme = { viewModel.setTheme(if (darkTheme) "light" else "dark") },
                                onCloseDrawerAndThen = { afterClose ->
                                    afterClose()
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets(0),
                            snackbarHost = { SnackbarHost(snackbarHostState) }
                        ) { innerPadding ->
                            AppNavHost(
                                navController = navController,
                                viewModel = viewModel,
                                uiState = uiState,
                                darkTheme = darkTheme,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                drawerActive = drawerActive,
                                scope = scope,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                    DrawerBackHandler(drawerActive, drawerState, scope)
                }
            }
        }
    }
}