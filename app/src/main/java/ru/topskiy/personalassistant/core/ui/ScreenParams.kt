package ru.topskiy.personalassistant.core.ui

import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope

/** Общие параметры экранов со Scaffold и drawer (Сервисы, Настройки, Управление сервисами). */
data class ScreenParams(
    val navController: NavHostController,
    val viewModel: AppStateViewModel,
    val uiState: AppStateUiState,
    val darkTheme: Boolean,
    val onOpenDrawer: () -> Unit,
    val drawerActive: Boolean,
    val scope: CoroutineScope
)
