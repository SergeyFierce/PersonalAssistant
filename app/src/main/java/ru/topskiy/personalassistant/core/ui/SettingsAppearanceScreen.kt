package ru.topskiy.personalassistant.core.ui

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgLight
import ru.topskiy.personalassistant.ui.theme.TopAppBarDark
import ru.topskiy.personalassistant.ui.theme.TopAppBarLight

private val ROW_HORIZONTAL_PADDING = 16.dp
private val ROW_VERTICAL_PADDING = 11.dp
private val THEME_ICON_SIZE_DP = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearanceScreen(params: ScreenParams) {
    val themeMode by params.viewModel.themeMode.collectAsStateWithLifecycle()

    BackHandler {
        params.navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_appearance)) },
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
                SettingsThemeRow(
                    title = if (themeMode == "dark") stringResource(R.string.settings_theme_switch_day) else stringResource(R.string.settings_theme_switch_night),
                    darkTheme = params.darkTheme,
                    lottieProgress = if (themeMode == "dark") 0f else 0.5f,
                    onClick = { params.viewModel.setTheme(if (themeMode == "dark") "light" else "dark") }
                )
            }
        }
    }
}

@Composable
private fun SettingsThemeRow(
    title: String,
    darkTheme: Boolean,
    lottieProgress: Float,
    onClick: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.day_night))
    val tintColor = if (darkTheme) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#6D6D72")
    val colorFilter = remember(tintColor) {
        PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
    }
    val lottieDynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR_FILTER,
            value = colorFilter,
            *arrayOf("**")
        )
    )
    val animatedProgress by animateFloatAsState(
        targetValue = lottieProgress,
            animationSpec = tween(
            durationMillis = THEME_ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        ),
        label = "theme_lottie"
    )
    val titleColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(THEME_ICON_SIZE_DP)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { animatedProgress },
                modifier = Modifier.size(THEME_ICON_SIZE_DP),
                dynamicProperties = lottieDynamicProperties
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
