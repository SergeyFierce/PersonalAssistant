package ru.topskiy.personalassistant.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgLight
import ru.topskiy.personalassistant.ui.theme.TopAppBarDark
import ru.topskiy.personalassistant.ui.theme.TopAppBarLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(params: ScreenParams) {
    BackHandler(enabled = !params.drawerActive) {
        params.navController.popBackStack()
    }

    val windowWidthClass = rememberWindowWidthClass()

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
                    containerColor = if (params.darkTheme) TopAppBarDark else TopAppBarLight,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .drawerOpenGestureOnContent(params.onOpenDrawer)
            .background(if (params.darkTheme) CatalogGroupedBgDark else CatalogGroupedBgLight)
            .padding(innerPadding)

        when (windowWidthClass) {
            WindowWidthClass.Compact,
            WindowWidthClass.Medium -> {
                SettingsListColumn(
                    modifier = contentModifier.verticalScroll(rememberScrollState()),
                    darkTheme = params.darkTheme,
                    onNavigateAppearance = { params.navController.navigate(SETTINGS_APPEARANCE_ROUTE) },
                    onNavigateNotifications = { params.navController.navigate(SETTINGS_NOTIFICATIONS_ROUTE) },
                    onNavigateAbout = { params.navController.navigate(SETTINGS_ABOUT_ROUTE) },
                    onNavigatePrivacy = { params.navController.navigate(SETTINGS_PRIVACY_ROUTE) }
                )
            }

            WindowWidthClass.Expanded -> {
                Row(
                    modifier = contentModifier.padding(horizontal = 48.dp, vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        SettingsListColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            darkTheme = params.darkTheme,
                            onNavigateAppearance = { params.navController.navigate(SETTINGS_APPEARANCE_ROUTE) },
                            onNavigateNotifications = { params.navController.navigate(SETTINGS_NOTIFICATIONS_ROUTE) },
                            onNavigateAbout = { params.navController.navigate(SETTINGS_ABOUT_ROUTE) },
                            onNavigatePrivacy = { params.navController.navigate(SETTINGS_PRIVACY_ROUTE) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Settings – phone",
    widthDp = 411,
    heightDp = 891,
    showBackground = true
)
@Composable
private fun SettingsScreenPhonePreview() {
    SettingsListColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CatalogGroupedBgLight)
            .padding(16.dp),
        darkTheme = false,
        onNavigateAppearance = {},
        onNavigateNotifications = {},
        onNavigateAbout = {},
        onNavigatePrivacy = {}
    )
}

@Preview(
    name = "Settings – tablet",
    widthDp = 1024,
    heightDp = 600,
    showBackground = true
)
@Composable
private fun SettingsScreenTabletPreview() {
    SettingsListColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CatalogGroupedBgLight)
            .padding(horizontal = 48.dp, vertical = 24.dp),
        darkTheme = false,
        onNavigateAppearance = {},
        onNavigateNotifications = {},
        onNavigateAbout = {},
        onNavigatePrivacy = {}
    )
}

@Composable
private fun SettingsListColumn(
    modifier: Modifier,
    darkTheme: Boolean,
    onNavigateAppearance: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigatePrivacy: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        SettingsGroup(darkTheme = darkTheme) {
            SettingsRow(
                title = stringResource(R.string.settings_appearance),
                darkTheme = darkTheme,
                onClick = onNavigateAppearance
            )
            SettingsRowDivider(darkTheme = darkTheme)
            SettingsRow(
                title = stringResource(R.string.settings_notifications),
                darkTheme = darkTheme,
                onClick = onNavigateNotifications
            )
            SettingsRowDivider(darkTheme = darkTheme)
            SettingsRow(
                title = stringResource(R.string.settings_about),
                darkTheme = darkTheme,
                onClick = onNavigateAbout
            )
            SettingsRowDivider(darkTheme = darkTheme)
            SettingsRow(
                title = stringResource(R.string.settings_privacy),
                darkTheme = darkTheme,
                onClick = onNavigatePrivacy
            )
        }
    }
}

