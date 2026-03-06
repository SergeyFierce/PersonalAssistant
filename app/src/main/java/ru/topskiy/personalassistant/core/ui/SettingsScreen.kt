package ru.topskiy.personalassistant.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.topskiy.personalassistant.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(params: ScreenParams) {
    BackHandler(enabled = !params.drawerActive) {
        params.navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { params.navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawerOpenGestureOnContent(params.onOpenDrawer)
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroup(darkTheme = params.darkTheme) {
                SettingsRow(
                    title = stringResource(R.string.settings_appearance),
                    darkTheme = params.darkTheme,
                    onClick = { params.navController.navigate(SETTINGS_APPEARANCE_ROUTE) }
                )
                SettingsRowDivider(darkTheme = params.darkTheme)
                SettingsRow(
                    title = stringResource(R.string.settings_notifications),
                    darkTheme = params.darkTheme,
                    onClick = { params.navController.navigate(SETTINGS_NOTIFICATIONS_ROUTE) }
                )
                SettingsRowDivider(darkTheme = params.darkTheme)
                SettingsRow(
                    title = stringResource(R.string.settings_about),
                    darkTheme = params.darkTheme,
                    onClick = { params.navController.navigate(SETTINGS_ABOUT_ROUTE) }
                )
                SettingsRowDivider(darkTheme = params.darkTheme)
                SettingsRow(
                    title = stringResource(R.string.settings_privacy),
                    darkTheme = params.darkTheme,
                    onClick = { params.navController.navigate(SETTINGS_PRIVACY_ROUTE) }
                )
            }
        }
    }
}
