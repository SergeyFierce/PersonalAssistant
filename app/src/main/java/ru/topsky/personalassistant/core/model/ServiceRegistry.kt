package ru.topsky.personalassistant.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Subscriptions

/**
 * Единый реестр сервисов приложения — один источник истины.
 */
object ServiceRegistry {

    val all: List<AppService> = listOf(
        AppService(
            id = ServiceId.DEALS,
            titleRu = "Дела",
            route = "service/deals",
            icon = Icons.Outlined.Assignment
        ),
        AppService(
            id = ServiceId.NOTES,
            titleRu = "Заметки",
            route = "service/notes",
            icon = Icons.Outlined.Note
        ),
        AppService(
            id = ServiceId.FINANCE,
            titleRu = "Финансы",
            route = "service/finance",
            icon = Icons.Outlined.AccountBalance
        ),
        AppService(
            id = ServiceId.SUBSCRIPTIONS,
            titleRu = "Подписки",
            route = "service/subscriptions",
            icon = Icons.Outlined.Subscriptions
        ),
        AppService(
            id = ServiceId.OBLIGATIONS,
            titleRu = "Обязательства",
            route = "service/obligations",
            icon = Icons.Outlined.Description
        ),
        AppService(
            id = ServiceId.CREDITS,
            titleRu = "Кредиты",
            route = "service/credits",
            icon = Icons.Outlined.CreditCard
        ),
        AppService(
            id = ServiceId.SHIFTS,
            titleRu = "Смены",
            route = "service/shifts",
            icon = Icons.Outlined.Schedule
        ),
        AppService(
            id = ServiceId.PROJECTS,
            titleRu = "Проекты",
            route = "service/projects",
            icon = Icons.Outlined.Folder
        ),
        AppService(
            id = ServiceId.MEDICINES,
            titleRu = "Лекарства",
            route = "service/medicines",
            icon = Icons.Outlined.Medication
        )
    )

    fun byId(id: ServiceId): AppService =
        all.first { it.id == id }
}
