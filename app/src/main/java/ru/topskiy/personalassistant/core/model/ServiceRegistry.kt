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
 *
 * **Порядок отображения** задаётся только здесь — списком [displayOrder]. Док-бар, каталог,
 * онбординг и остальные экраны используют [all] и [groupedByCategory], поэтому порядок менять
 * нигде больше не нужно.
 *
 * ---
 * **Как добавить новый сервис:**
 *
 * 1. **ServiceId** — добавить новое значение enum (например, `NEW_SERVICE`). Порядок значений
 *    в enum на отображение не влияет.
 *
 * 2. **res/values/strings.xml** — добавить строку для названия (например, `service_new_service`).
 *
 * 3. **servicesById** — добавить запись: `ServiceId.NEW_SERVICE to AppService(...)` с полями:
 *    - id — тот же ServiceId
 *    - titleResId — R.string.service_new_service
 *    - route — строка маршрута, например "service/new_service"
 *    - icon — Icons.Outlined.* (при необходимости добавить import)
 *    - category — одна из [ServiceCategory]
 *
 * 4. **displayOrder** — вставить `ServiceId.NEW_SERVICE` в нужную позицию списка. Это единственное
 *    место, где задаётся порядок сервисов во всём приложении.
 */
object ServiceRegistry {

    /**
     * Единственное место, задающее порядок сервисов (док, каталог, онбординг).
     * Не зависит от порядка enum [ServiceId].
     */
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
