package ru.topsky.personalassistant.core.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Описание сервиса приложения.
 */
data class AppService(
    val id: ServiceId,
    val titleRu: String,
    val route: String,
    val icon: ImageVector
)
