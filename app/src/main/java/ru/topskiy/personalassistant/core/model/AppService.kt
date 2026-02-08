package ru.topskiy.personalassistant.core.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Описание сервиса приложения.
 * @param titleResId string resource id для названия сервиса
 */
data class AppService(
    val id: ServiceId,
    val titleResId: Int,
    val route: String,
    val icon: ImageVector,
    val category: ServiceCategory
)
