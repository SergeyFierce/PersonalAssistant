package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import ru.topskiy.personalassistant.R
import kotlinx.coroutines.launch
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: AppStateViewModel
) {
    var selectedServices by remember { mutableStateOf(setOf<ServiceId>()) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        ServiceRegistry.all.forEach { service ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedServices = if (service.id in selectedServices) {
                            selectedServices - service.id
                        } else {
                            selectedServices + service.id
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = service.id in selectedServices,
                    onCheckedChange = { checked ->
                        selectedServices = if (checked) {
                            selectedServices + service.id
                        } else {
                            selectedServices - service.id
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = service.icon,
                    contentDescription = stringResource(service.titleResId),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(service.titleResId),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_hint_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_hint_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
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
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedServices.isNotEmpty() && !isSaving
        ) {
            Text(
                text = stringResource(R.string.onboarding_start),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
