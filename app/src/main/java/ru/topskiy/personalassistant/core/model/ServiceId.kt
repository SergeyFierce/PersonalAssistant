package ru.topskiy.personalassistant.core.model

/**
 * Идентификаторы сервисов приложения.
 * Порядок отображения в UI задаётся в [ServiceRegistry.displayOrder].
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
