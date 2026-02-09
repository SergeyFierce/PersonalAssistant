package ru.topskiy.personalassistant

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Базовый UI‑тест ключевого сценария: проверяем, что приложение стартует,
 * показывает экран загрузки и не падает при инициализации bootstrap → онбординг/главный экран.
 *
 * Тест намеренно упрощён и не лезет в навигацию по маршрутам, чтобы не усложнять Hilt/DI‑сетап.
 */
@RunWith(AndroidJUnit4::class)
class BootstrapFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsAndShowsLoadingText() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.loading))
            .assertIsDisplayed()
    }
}

