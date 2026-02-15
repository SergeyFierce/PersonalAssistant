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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgLight
import ru.topskiy.personalassistant.ui.theme.TopAppBarDark
import ru.topskiy.personalassistant.ui.theme.TopAppBarLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAboutScreen(params: ScreenParams) {
    BackHandler {
        params.navController.popBackStack()
    }

    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (_: Exception) {
        "1.0"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_about)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawerOpenGestureOnContent(params.onOpenDrawer)
                .background(if (params.darkTheme) CatalogGroupedBgDark else CatalogGroupedBgLight)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsGroup(darkTheme = params.darkTheme) {
                SettingsRow(
                    title = stringResource(R.string.settings_version),
                    darkTheme = params.darkTheme,
                    value = versionName,
                    showChevron = false,
                    onClick = { }
                )
            }
        }
    }
}
