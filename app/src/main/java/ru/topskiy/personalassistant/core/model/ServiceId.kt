package ru.topskiy.personalassistant.core.model

/**
 * Идентификаторы сервисов приложения.
 *
 * Порядок отображения в UI не зависит от порядка значений здесь — он задаётся только
 * в [ServiceRegistry.displayOrder]. При добавлении нового сервиса достаточно добавить
 * новое значение enum; порядок в списке displayOrder задаётся там.
 */
enum class ServiceId {
    DEALS,
    NOTES,
    FINANCE,
    SUBSCRIPTIONS,
    OBLIGATIONS,
    CREDITS,
    SHIFTS,
    PROJECTS,
    MEDICINES
}
