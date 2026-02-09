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
 * Порядок отображения в UI задаётся только списком [displayOrder].
 *
 * Как добавить новый сервис:
 * 1. Добавьте новый идентификатор в [ServiceId].
 * 2. Опишите сервис в [servicesById] (titleResId, icon, category, route).
 * 3. Добавьте идентификатор в [displayOrder] в нужное место (это определит порядок в док-баре, каталоге, онбординге и др. экранах).
 */
object ServiceRegistry {

    /** Явный порядок отображения сервисов во всех экранах (док, каталог, онбординг). Не зависит от порядка enum [ServiceId]. */
    val displayOrder: List<ServiceId> = listOf(
        ServiceId.DEALS,
        ServiceId.NOTES,
        ServiceId.PROJECTS,
        ServiceId.SHIFTS,
        ServiceId.FINANCE,
        ServiceId.SUBSCRIPTIONS,
        ServiceId.OBLIGATIONS,
        ServiceId.CREDITS,
        ServiceId.MEDICINES
    )

    private val servicesById: Map<ServiceId, AppService> = mapOf(
        ServiceId.DEALS to AppService(
            id = ServiceId.DEALS,
            titleResId = R.string.service_deals,
            route = "service/deals",
            icon = Icons.Outlined.Assignment,
            category = ServiceCategory.PLANNING
        ),
        ServiceId.NOTES to AppService(
            id = ServiceId.NOTES,
            titleResId = R.string.service_notes,
            route = "service/notes",
            icon = Icons.Outlined.Note,
            category = ServiceCategory.PLANNING
        ),
        ServiceId.PROJECTS to AppService(
            id = ServiceId.PROJECTS,
            titleResId = R.string.service_projects,
            route = "service/projects",
            icon = Icons.Outlined.Folder,
            category = ServiceCategory.PLANNING
        ),
        ServiceId.SHIFTS to AppService(
            id = ServiceId.SHIFTS,
            titleResId = R.string.service_shifts,
            route = "service/shifts",
            icon = Icons.Outlined.Schedule,
            category = ServiceCategory.PLANNING
        ),
        ServiceId.FINANCE to AppService(
            id = ServiceId.FINANCE,
            titleResId = R.string.service_finance,
            route = "service/finance",
            icon = Icons.Outlined.AccountBalance,
            category = ServiceCategory.FINANCE
        ),
        ServiceId.SUBSCRIPTIONS to AppService(
            id = ServiceId.SUBSCRIPTIONS,
            titleResId = R.string.service_subscriptions,
            route = "service/subscriptions",
            icon = Icons.Outlined.Subscriptions,
            category = ServiceCategory.FINANCE
        ),
        ServiceId.OBLIGATIONS to AppService(
            id = ServiceId.OBLIGATIONS,
            titleResId = R.string.service_obligations,
            route = "service/obligations",
            icon = Icons.Outlined.Description,
            category = ServiceCategory.FINANCE
        ),
        ServiceId.CREDITS to AppService(
            id = ServiceId.CREDITS,
            titleResId = R.string.service_credits,
            route = "service/credits",
            icon = Icons.Outlined.CreditCard,
            category = ServiceCategory.FINANCE
        ),
        ServiceId.MEDICINES to AppService(
            id = ServiceId.MEDICINES,
            titleResId = R.string.service_medicines,
            route = "service/medicines",
            icon = Icons.Outlined.Medication,
            category = ServiceCategory.HEALTH
        )
    )

    /** Все сервисы в порядке отображения [displayOrder]. */
    val all: List<AppService> = displayOrder.map { servicesById.getValue(it) }

    /** Группировка по категориям; порядок категорий — [ServiceCategory.entries], порядок сервисов внутри категории — [displayOrder]. */
    val groupedByCategory: List<Pair<ServiceCategory, List<AppService>>> =
        ServiceCategory.entries.map { category ->
            category to all.filter { it.category == category }
        }.filter { it.second.isNotEmpty() }

    fun byId(id: ServiceId): AppService = servicesById.getValue(id)
}
