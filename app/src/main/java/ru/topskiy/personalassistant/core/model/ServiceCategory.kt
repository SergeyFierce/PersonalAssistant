package ru.topskiy.personalassistant.core.model

import ru.topskiy.personalassistant.R

/**
 * Категории сервисов для группировки в каталоге.
 * @param titleResId string resource id для названия категории
 */
enum class ServiceCategory(val titleResId: Int) {
    PLANNING(R.string.category_planning),
    FINANCE(R.string.category_finance),
    HEALTH(R.string.category_health)
}
