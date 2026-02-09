package ru.topskiy.personalassistant

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.topskiy.personalassistant.core.model.ServiceId

/**
 * Compose UI-тесты: онбординг, главный экран с доком, управление сервисами, настройки
 * (включая запрет выключить последний сервис и переключение темы).
 */
@RunWith(AndroidJUnit4::class)
class OnboardingMainManageSettingsUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun string(id: Int): String = composeRule.activity.getString(id)

    /** Ждём завершения bootstrap: исчезновение "Загрузка…" и появление онбординга или главного экрана. */
    private fun waitPastBootstrap() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            val loading = composeRule.onAllNodesWithText(string(R.string.loading))
            val onboarding = composeRule.onAllNodesWithText(string(R.string.onboarding_title))
            val main = composeRule.onAllNodesWithContentDescription(string(R.string.menu))
            try {
                loading.fetchSemanticsNodes().isEmpty() &&
                    (onboarding.fetchSemanticsNodes().isNotEmpty() || main.fetchSemanticsNodes().isNotEmpty())
            } catch (_: Exception) {
                false
            }
        }
    }

    /** Если виден онбординг — выбираем один сервис и нажимаем «Начать». */
    private fun completeOnboardingIfShown() {
        waitPastBootstrap()
        if (composeRule.onAllNodesWithText(string(R.string.onboarding_title)).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText(string(R.string.service_deals)).performScrollTo().performClick()
            composeRule.onNodeWithText(string(R.string.onboarding_start)).performScrollTo().performClick()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                try {
                    composeRule.onAllNodesWithText(string(R.string.service_in_development)).fetchSemanticsNodes().isNotEmpty() ||
                        composeRule.onAllNodesWithContentDescription(string(R.string.menu)).fetchSemanticsNodes().isNotEmpty()
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    private fun openDrawer() {
        composeRule.onNodeWithContentDescription(string(R.string.menu)).performClick()
        composeRule.waitUntil(timeoutMillis = 2_000) {
            try {
                composeRule.onAllNodesWithText(string(R.string.drawer_services)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
    }

    @Test
    fun onboarding_selectServiceAndStart_navigatesToMain() {
        waitPastBootstrap()
        composeRule.onNodeWithText(string(R.string.onboarding_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.service_deals)).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.onboarding_start)).performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule.onAllNodesWithText(string(R.string.service_in_development)).fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithContentDescription(string(R.string.menu)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        composeRule.onNodeWithContentDescription(string(R.string.menu)).assertIsDisplayed()
    }

    @Test
    fun mainScreen_showsDockAndDrawerItems() {
        completeOnboardingIfShown()
        openDrawer()
        composeRule.onNodeWithText(string(R.string.drawer_services)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.drawer_settings)).assertIsDisplayed()
    }

    @Test
    fun manageServices_screenOpensAndShowsCatalog() {
        completeOnboardingIfShown()
        openDrawer()
        composeRule.onNodeWithText(string(R.string.drawer_services)).performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            try {
                composeRule.onAllNodesWithText(string(R.string.manage_services_title)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        composeRule.onNodeWithText(string(R.string.service_deals)).assertIsDisplayed()
    }

    @Test
    fun manageServices_cannotDisableLastService_showsSnackbar() {
        completeOnboardingIfShown()
        openDrawer()
        composeRule.onNodeWithText(string(R.string.drawer_services)).performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            try {
                composeRule.onAllNodesWithTag("service_${ServiceId.DEALS}").fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        val switchDeals = composeRule.onNodeWithTag("service_${ServiceId.DEALS}")
        switchDeals.assertIsOn()
        switchDeals.performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            try {
                composeRule.onAllNodesWithText(string(R.string.min_one_service_required)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        composeRule.onNodeWithText(string(R.string.min_one_service_required)).assertIsDisplayed()
        switchDeals.assertIsOn()
    }

    @Test
    fun settings_screenOpensAndThemeCanBeSwitched() {
        completeOnboardingIfShown()
        openDrawer()
        composeRule.onNodeWithText(string(R.string.drawer_settings)).performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            try {
                composeRule.onAllNodesWithText(string(R.string.settings_title)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        composeRule.onNodeWithText(string(R.string.settings_theme_section)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.theme_light)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.theme_dark)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.theme_dark)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(string(R.string.theme_light)).performClick()
        composeRule.waitForIdle()
    }
}
