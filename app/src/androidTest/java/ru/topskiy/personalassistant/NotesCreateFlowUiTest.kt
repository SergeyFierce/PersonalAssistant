package ru.topskiy.personalassistant

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.topskiy.personalassistant.core.model.ServiceId

/**
 * Compose UI-тест: создание заметки через FAB и появление её в списке.
 */
@RunWith(AndroidJUnit4::class)
class NotesCreateFlowUiTest {

    companion object {
        private const val BOOTSTRAP_TIMEOUT_MS = 10_000
        private const val NAVIGATION_OR_STEP_TIMEOUT_MS = 5_000
        private const val DRAWER_OPEN_TIMEOUT_MS = 2_000
        private const val SCREEN_WAIT_TIMEOUT_MS = 5_000
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun string(id: Int): String = composeRule.activity.getString(id)

    private fun waitPastBootstrap() {
        composeRule.waitUntil(timeoutMillis = BOOTSTRAP_TIMEOUT_MS) {
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

    private fun completeOnboardingIfShown() {
        waitPastBootstrap()
        if (composeRule.onAllNodesWithText(string(R.string.onboarding_title)).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText(string(R.string.service_deals)).performScrollTo().performClick()
            composeRule.onNodeWithText(string(R.string.onboarding_start)).performScrollTo().performClick()
            composeRule.waitUntil(timeoutMillis = NAVIGATION_OR_STEP_TIMEOUT_MS) {
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
        composeRule.waitUntil(timeoutMillis = DRAWER_OPEN_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithText(string(R.string.drawer_services)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun ensureNotesEnabledAndOpenNotes() {
        openDrawer()
        composeRule.onNodeWithText(string(R.string.drawer_services)).performClick()
        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithText(string(R.string.manage_services_title)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        val notesTag = "service_${ServiceId.NOTES}"
        if (composeRule.onAllNodesWithTag(notesTag).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag(notesTag).performScrollTo().performClick()
        }
        composeRule.onNodeWithContentDescription(string(R.string.back)).performClick()
        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithContentDescription(string(R.string.service_notes)).fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithContentDescription(string(R.string.service_deals)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        composeRule.onNodeWithContentDescription(string(R.string.service_notes)).performClick()
        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithContentDescription(string(R.string.notes_fab_add)).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
    }

    @Test
    fun notes_fabCreateNote_noteAppearsInList() {
        completeOnboardingIfShown()
        ensureNotesEnabledAndOpenNotes()

        composeRule.onNodeWithContentDescription(string(R.string.notes_fab_add)).performClick()
        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithTag("note_editor_title").fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        val testTitle = "Test Note Title"
        val testBody = "Test body text"
        composeRule.onNodeWithTag("note_editor_title").performTextInput(testTitle)
        composeRule.onNodeWithTag("note_editor_body").performTextInput(testBody)
        composeRule.onNodeWithText(string(R.string.notes_editor_save)).performClick()

        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithText(testTitle).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        composeRule.onNodeWithText(testTitle).assertIsDisplayed()
    }

    @Test
    fun notes_editExistingNote_updatesInList() {
        completeOnboardingIfShown()
        ensureNotesEnabledAndOpenNotes()

        // Сначала создаём заметку
        val originalTitle = "Original note title"
        val originalBody = "Original note body"
        composeRule.onNodeWithContentDescription(string(R.string.notes_fab_add)).performClick()
        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithTag("note_editor_title").fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        composeRule.onNodeWithTag("note_editor_title").performTextInput(originalTitle)
        composeRule.onNodeWithTag("note_editor_body").performTextInput(originalBody)
        composeRule.onNodeWithText(string(R.string.notes_editor_save)).performClick()
        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithText(originalTitle).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        // Открываем существующую заметку
        composeRule.onNodeWithText(originalTitle).performClick()
        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithTag("note_editor_title").fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        // Меняем заголовок и текст
        val updatedTitle = "Updated note title"
        val updatedBody = "Updated body text"
        composeRule.onNodeWithTag("note_editor_title").performTextClearance()
        composeRule.onNodeWithTag("note_editor_title").performTextInput(updatedTitle)
        composeRule.onNodeWithTag("note_editor_body").performTextClearance()
        composeRule.onNodeWithTag("note_editor_body").performTextInput(updatedBody)
        composeRule.onNodeWithText(string(R.string.notes_editor_save)).performClick()

        // Проверяем, что список показывает обновлённый заголовок
        composeRule.waitUntil(timeoutMillis = SCREEN_WAIT_TIMEOUT_MS) {
            try {
                composeRule.onAllNodesWithText(updatedTitle).fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }
        composeRule.onNodeWithText(updatedTitle).assertIsDisplayed()
    }
}
