package ru.topskiy.personalassistant.core.model

import androidx.compose.material.icons.Icons
import ru.topskiy.personalassistant.R
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
            titleResId = R.string.service_deals,
            route = "service/deals",
            icon = Icons.Outlined.Assignment,
            category = ServiceCategory.PLANNING
        ),
        AppService(
            id = ServiceId.NOTES,
            titleResId = R.string.service_notes,
            route = "service/notes",
            icon = Icons.Outlined.Note,
            category = ServiceCategory.PLANNING
        ),
        AppService(
            id = ServiceId.PROJECTS,
            titleResId = R.string.service_projects,
            route = "service/projects",
            icon = Icons.Outlined.Folder,
            category = ServiceCategory.PLANNING
        ),
        AppService(
            id = ServiceId.SHIFTS,
            titleResId = R.string.service_shifts,
            route = "service/shifts",
            icon = Icons.Outlined.Schedule,
            category = ServiceCategory.PLANNING
        ),
        AppService(
            id = ServiceId.FINANCE,
            titleResId = R.string.service_finance,
            route = "service/finance",
            icon = Icons.Outlined.AccountBalance,
            category = ServiceCategory.FINANCE
        ),
        AppService(
            id = ServiceId.SUBSCRIPTIONS,
            titleResId = R.string.service_subscriptions,
            route = "service/subscriptions",
            icon = Icons.Outlined.Subscriptions,
            category = ServiceCategory.FINANCE
        ),
        AppService(
            id = ServiceId.OBLIGATIONS,
            titleResId = R.string.service_obligations,
            route = "service/obligations",
            icon = Icons.Outlined.Description,
            category = ServiceCategory.FINANCE
        ),
        AppService(
            id = ServiceId.CREDITS,
            titleResId = R.string.service_credits,
            route = "service/credits",
            icon = Icons.Outlined.CreditCard,
            category = ServiceCategory.FINANCE
        ),
        AppService(
            id = ServiceId.MEDICINES,
            titleResId = R.string.service_medicines,
            route = "service/medicines",
            icon = Icons.Outlined.Medication,
            category = ServiceCategory.HEALTH
        )
    )

    /** Группировка по категориям в порядке enum. */
    val groupedByCategory: List<Pair<ServiceCategory, List<AppService>>> =
        ServiceCategory.entries.map { category ->
            category to all.filter { it.category == category }
        }.filter { it.second.isNotEmpty() }

    fun byId(id: ServiceId): AppService =
        all.first { it.id == id }
}
