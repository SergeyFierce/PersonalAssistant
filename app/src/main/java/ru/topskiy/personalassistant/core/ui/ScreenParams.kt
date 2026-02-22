package ru.topskiy.personalassistant.core.ui

import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope

/**
 * Общие параметры экранов со Scaffold и drawer (Сервисы, Настройки, Управление сервисами).
 *
 * @param navController Контроллер навигации для переходов и popBackStack.
 * @param viewModel Общий ViewModel состояния приложения и настроек.
 * @param uiState Текущее UI-состояние (включённые сервисы, избранный, онбординг и т.д.).
 * @param darkTheme true — тёмная тема, false — светлая.
 * @param onOpenDrawer Колбэк открытия бокового меню (drawer).
 * @param drawerActive true, если drawer открыт или в процессе открытия.
 * @param scope CoroutineScope для запуска корутин на экране (например, закрытие drawer).
 */
data class ScreenParams(
    val navController: NavHostController,
    val viewModel: AppStateViewModel,
    val uiState: AppStateUiState,
    val darkTheme: Boolean,
    val onOpenDrawer: () -> Unit,
    val drawerActive: Boolean,
    val scope: CoroutineScope
)
