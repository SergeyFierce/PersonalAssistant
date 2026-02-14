package ru.topskiy.personalassistant.core.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgLight
import ru.topskiy.personalassistant.ui.theme.TopAppBarDark
import ru.topskiy.personalassistant.ui.theme.TopAppBarLight

private enum class ServiceCatalogViewMode { LIST, GRID }

private val CATALOG_HORIZONTAL_INSET_DP = 16.dp
/** Минимальная ширина карточки: иконка сервиса (48dp круг) и кнопка избранного (48dp) не должны пересекаться. */
private val CATALOG_MIN_CARD_WIDTH_DP = 160.dp
private val CATALOG_CONTENT_TOP_DP = 12.dp
private val CATALOG_CONTENT_BOTTOM_DP = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageServicesTopBar(
    params: ScreenParams,
    loadedViewMode: Boolean?,
    onShowFavoriteInfo: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.manage_services_title))
                Text(
                    text = stringResource(R.string.enabled_count, params.uiState.enabledServices.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        },
        actions = {
            IconButton(onClick = onShowFavoriteInfo) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.content_description_favorite_info)
                )
            }
            CatalogViewModeButton(
                isListView = loadedViewMode == true,
                onToggle = { params.viewModel.setServicesCatalogListView(loadedViewMode != true) }
            )
        },
        navigationIcon = {
            IconButton(onClick = { params.navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (params.darkTheme) TopAppBarDark else TopAppBarLight,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogViewModeButton(
    isListView: Boolean,
    onToggle: () -> Unit
) {
    val viewMode = if (isListView) ServiceCatalogViewMode.LIST else ServiceCatalogViewMode.GRID
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "view_toggle_scale"
    )
    IconButton(
        onClick = onToggle,
        interactionSource = interactionSource,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                (scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(150))) togetherWith
                    (scaleOut(targetScale = 0.8f, animationSpec = tween(120)) +
                        fadeOut(animationSpec = tween(120)))
            },
            label = "view_toggle_icon"
        ) { mode ->
            Icon(
                imageVector = if (mode == ServiceCatalogViewMode.GRID) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                contentDescription = if (mode == ServiceCatalogViewMode.GRID) stringResource(R.string.view_as_list) else stringResource(R.string.view_as_grid)
            )
        }
    }
}

@Composable
private fun ManageServicesCatalogBody(
    params: ScreenParams,
    loadedViewMode: Boolean?,
    columnCount: Int
) {
    if (loadedViewMode == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }
    val viewMode = if (loadedViewMode) ServiceCatalogViewMode.LIST else ServiceCatalogViewMode.GRID
    val listState = rememberLazyListState()
    AnimatedContent(
        targetState = viewMode,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220))) togetherWith
                (fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.92f, animationSpec = tween(180)))
        },
        label = "catalog_view"
    ) { mode ->
        when (mode) {
            ServiceCatalogViewMode.LIST -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = CATALOG_HORIZONTAL_INSET_DP,
                        top = CATALOG_CONTENT_TOP_DP,
                        end = CATALOG_HORIZONTAL_INSET_DP,
                        bottom = CATALOG_CONTENT_BOTTOM_DP
                    )
            ) {
                ServiceCatalogListView(
                    enabledIds = params.uiState.enabledServices,
                    favoriteServiceId = params.uiState.favoriteService,
                    onToggle = { id, checked -> params.viewModel.toggleService(id, checked) },
                    onSetFavorite = { params.viewModel.setFavorite(it) },
                    darkTheme = params.darkTheme
                )
            }
            ServiceCatalogViewMode.GRID -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = CATALOG_HORIZONTAL_INSET_DP,
                    top = CATALOG_CONTENT_TOP_DP,
                    end = CATALOG_HORIZONTAL_INSET_DP,
                    bottom = CATALOG_CONTENT_BOTTOM_DP
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                ServiceRegistry.groupedByCategory.forEach { (category, services) ->
                    item(key = category) {
                        ServiceCatalogSectionHeader(
                            stringResource(category.titleResId),
                            params.darkTheme
                        )
                    }
                    item(key = "grid_${category.name}") {
                        ServiceCatalogGrid(
                            services = services,
                            columnCount = columnCount,
                            enabledIds = params.uiState.enabledServices,
                            favoriteServiceId = params.uiState.favoriteService,
                            onToggle = { id, checked -> params.viewModel.toggleService(id, checked) },
                            onSetFavorite = { params.viewModel.setFavorite(it) },
                            darkTheme = params.darkTheme
                        )
                    }
                    item(key = "spacer_${category.name}") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageServicesScreen(params: ScreenParams) {
    val loadedViewMode by params.viewModel.loadedCatalogViewMode.collectAsStateWithLifecycle()
    var showFavoriteExplanation by remember { mutableStateOf(false) }

    BackHandler(enabled = !params.drawerActive) {
        params.navController.popBackStack()
    }

    if (showFavoriteExplanation) {
        AlertDialog(
            onDismissRequest = { showFavoriteExplanation = false },
            title = { Text(stringResource(R.string.favorite_service_label)) },
            text = { Text(stringResource(R.string.favorite_service_explanation)) },
            confirmButton = {
                TextButton(onClick = { showFavoriteExplanation = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ManageServicesTopBar(
                params = params,
                loadedViewMode = loadedViewMode,
                onShowFavoriteInfo = { showFavoriteExplanation = true }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .drawerOpenGestureOnContent(params.onOpenDrawer)
                .background(if (params.darkTheme) CatalogGroupedBgDark else CatalogGroupedBgLight)
                .padding(innerPadding)
        ) {
            val columnCount = ((maxWidth - CATALOG_HORIZONTAL_INSET_DP * 2) / CATALOG_MIN_CARD_WIDTH_DP)
                .toInt()
                .coerceIn(2, 5)
            ManageServicesCatalogBody(params, loadedViewMode, columnCount)
        }
    }
}
