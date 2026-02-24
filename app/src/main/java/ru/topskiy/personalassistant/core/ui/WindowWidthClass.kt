package ru.topskiy.personalassistant.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Классы ширины окна для адаптации под телефоны/планшеты.
 *
 * Пороговые значения подобраны по рекомендациям Material:
 * - Compact: < 600dp
 * - Medium: 600–839dp
 * - Expanded: ≥ 840dp
 */
enum class WindowWidthClass {
    Compact,
    Medium,
    Expanded
}

/**
 * Определяет текущий класс ширины окна на основе [LocalConfiguration.screenWidthDp].
 *
 * Используется для переключения компоновки (одноколоночная на телефонах,
 * двухколоночная на планшетах и широких экранах).
 */
@Composable
fun rememberWindowWidthClass(): WindowWidthClass {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> WindowWidthClass.Compact
        widthDp < 840 -> WindowWidthClass.Medium
        else -> WindowWidthClass.Expanded
    }
}

