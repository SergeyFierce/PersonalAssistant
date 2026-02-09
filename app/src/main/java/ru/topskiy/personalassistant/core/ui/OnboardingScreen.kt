package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry
import ru.topskiy.personalassistant.ui.theme.OnboardingBackgroundDark
import ru.topskiy.personalassistant.ui.theme.OnboardingBackgroundLight
import ru.topskiy.personalassistant.ui.theme.OnboardingCardDark
import ru.topskiy.personalassistant.ui.theme.OnboardingCardLight
import ru.topskiy.personalassistant.ui.theme.OnboardingSecondaryTextDark
import ru.topskiy.personalassistant.ui.theme.OnboardingSecondaryTextLight
import ru.topskiy.personalassistant.ui.theme.OnboardingSeparatorDark
import ru.topskiy.personalassistant.ui.theme.OnboardingSeparatorLight
import ru.topskiy.personalassistant.ui.theme.OnboardingTintBlue

private val CardShape = RoundedCornerShape(12.dp)
private val ButtonShape = RoundedCornerShape(12.dp)
private val HorizontalPadding = 20.dp
private val SectionSpacing = 24.dp

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: AppStateViewModel
) {
    var selectedServices by remember { mutableStateOf(setOf<ServiceId>()) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) OnboardingBackgroundDark else OnboardingBackgroundLight
    val cardColor = if (isDark) OnboardingCardDark else OnboardingCardLight
    val secondaryColor = if (isDark) OnboardingSecondaryTextDark else OnboardingSecondaryTextLight
    val separatorColor = if (isDark) OnboardingSeparatorDark else OnboardingSeparatorLight
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val bottomPadding = (configuration.screenHeightDp * 0.04f).dp.coerceAtLeast(24.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = HorizontalPadding)
                .padding(top = 48.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // —— Приветствие (iOS large title style) ——
            Text(
                text = stringResource(R.string.onboarding_welcome),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = secondaryColor
            )

            Spacer(modifier = Modifier.height(SectionSpacing))

            // —— Секция «С чего начнём?» ——
            Text(
                text = stringResource(R.string.onboarding_title).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = secondaryColor,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = secondaryColor,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // —— Карточка со списком сервисов (iOS grouped style) ——
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                color = cardColor
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ServiceRegistry.all.forEachIndexed { index, service ->
                        val isSelected = service.id in selectedServices
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedServices = if (isSelected) {
                                        selectedServices - service.id
                                    } else {
                                        selectedServices + service.id
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    imageVector = service.icon,
                                    contentDescription = stringResource(service.titleResId),
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(service.titleResId),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OnboardingTintBlue
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(
                                            width = 2.dp,
                                            color = separatorColor,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                )
                            }
                        }
                        if (index < ServiceRegistry.all.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .padding(start = 52.dp)
                                    .background(separatorColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // —— Кнопка «Начать» внизу (iOS-style primary button) ——
        val buttonEnabled = selectedServices.isNotEmpty() && !isSaving
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding)
                .padding(bottom = bottomPadding),
            shape = ButtonShape,
            color = if (buttonEnabled) OnboardingTintBlue else secondaryColor.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(ButtonShape)
                    .clickable(enabled = buttonEnabled) {
                        val firstSelected = ServiceRegistry.all
                            .firstOrNull { it.id in selectedServices }
                            ?.id
                        if (firstSelected != null && !isSaving) {
                            isSaving = true
                            scope.launch {
                                viewModel.completeOnboarding(selectedServices, firstSelected)
                                    .onSuccess {
                                        navController.navigate(MAIN_ROUTE) {
                                            popUpTo(ONBOARDING_ROUTE) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                    .onFailure { isSaving = false }
                            }
                        }
                    }
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.onboarding_start),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (buttonEnabled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}
