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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ru.topskiy.personalassistant.ui.theme.PersonalAssistantTheme
import ru.topsky.personalassistant.core.datastore.SettingsRepository
import ru.topsky.personalassistant.core.ui.AppNavHost
import ru.topsky.personalassistant.core.ui.AppStateViewModel
import ru.topsky.personalassistant.core.ui.DrawerContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import androidx.compose.material3.DrawerState

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsRepository = remember { SettingsRepository(applicationContext) }
            val themeMode by settingsRepository.themeFlow.collectAsStateWithLifecycle(initialValue = "system")
            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            PersonalAssistantTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val viewModel: AppStateViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return AppStateViewModel(settingsRepository) as T
                        }
                    }
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val drawerActive = drawerState.currentValue == DrawerValue.Open ||
                    drawerState.targetValue == DrawerValue.Open

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
                            contentWindowInsets = WindowInsets(0)
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