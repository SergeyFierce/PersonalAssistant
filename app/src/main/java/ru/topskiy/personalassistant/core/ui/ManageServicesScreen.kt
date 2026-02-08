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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.topskiy.personalassistant.core.model.AppService
import ru.topskiy.personalassistant.core.model.ServiceCategory
import ru.topskiy.personalassistant.R
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.core.model.ServiceRegistry
import ru.topskiy.personalassistant.ui.theme.CatalogCardBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogCardBgLight
import ru.topskiy.personalassistant.ui.theme.CatalogCardBorderDark
import ru.topskiy.personalassistant.ui.theme.CatalogCardBorderLight
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgDark
import ru.topskiy.personalassistant.ui.theme.CatalogGroupedBgLight
import ru.topskiy.personalassistant.ui.theme.CatalogIconDark
import ru.topskiy.personalassistant.ui.theme.CatalogIconLight
import ru.topskiy.personalassistant.ui.theme.CatalogListDividerDark
import ru.topskiy.personalassistant.ui.theme.CatalogListDividerLight
import ru.topskiy.personalassistant.ui.theme.CatalogSectionHeaderDark
import ru.topskiy.personalassistant.ui.theme.CatalogSectionHeaderLight
import ru.topskiy.personalassistant.ui.theme.SwitchThumbChecked
import ru.topskiy.personalassistant.ui.theme.SwitchTrackCheckedGreen
import ru.topskiy.personalassistant.ui.theme.TopAppBarDark
import ru.topskiy.personalassistant.ui.theme.TopAppBarLight

private const val iosCardCornerDp = 12f
private val minServiceCardWidthDp = 96.dp
private val horizontalInsetDp = 16.dp

@Composable
private fun ServiceCatalogSectionHeader(title: String, darkTheme: Boolean) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = if (darkTheme) CatalogSectionHeaderDark else CatalogSectionHeaderLight,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, end = 4.dp, bottom = 6.dp),
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ServiceCatalogCard(
    service: AppService,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    darkTheme: Boolean
) {
    val cardBg = if (darkTheme) CatalogCardBgDark else CatalogCardBgLight
    val borderColor = if (darkTheme) CatalogCardBorderDark else CatalogCardBorderLight
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(iosCardCornerDp.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = stringResource(service.titleResId),
                modifier = Modifier.size(28.dp),
                tint = if (darkTheme) CatalogIconDark else CatalogIconLight
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(service.titleResId),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.size(width = 36.dp, height = 24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SwitchThumbChecked,
                    checkedTrackColor = SwitchTrackCheckedGreen,
                    checkedBorderColor = SwitchTrackCheckedGreen
                )
            )
        }
    }
}

@Composable
private fun ServiceCatalogListView(
    enabledIds: Set<ServiceId>,
    onToggle: (ServiceId, Boolean) -> Unit,
    darkTheme: Boolean
) {
    val groupShape = RoundedCornerShape(iosCardCornerDp.dp)
    val cardBg = if (darkTheme) CatalogCardBgDark else CatalogCardBgLight
    val dividerColor = if (darkTheme) CatalogListDividerDark else CatalogListDividerLight

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ServiceRegistry.groupedByCategory.forEach { (category, services) ->
            ServiceCatalogSectionHeader(stringResource(category.titleResId), darkTheme)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = groupShape,
                color = cardBg,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(0.5.dp, if (darkTheme) CatalogCardBorderDark else CatalogCardBorderLight)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    services.forEachIndexed { index, service ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                color = dividerColor,
                                thickness = 0.5.dp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = service.icon,
                                contentDescription = stringResource(service.titleResId),
                                modifier = Modifier.size(24.dp),
                                tint = if (darkTheme) CatalogIconDark else CatalogIconLight
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(service.titleResId),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = service.id in enabledIds,
                                onCheckedChange = { onToggle(service.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SwitchThumbChecked,
                                    checkedTrackColor = SwitchTrackCheckedGreen,
                                    checkedBorderColor = SwitchTrackCheckedGreen
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ServiceCatalogGrid(
    services: List<AppService>,
    columnCount: Int,
    enabledIds: Set<ServiceId>,
    onToggle: (ServiceId, Boolean) -> Unit,
    darkTheme: Boolean
) {
    val rows = services.chunked(columnCount)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowServices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowServices.forEach { service ->
                    Box(modifier = Modifier.weight(1f)) {
                        ServiceCatalogCard(
                            service = service,
                            enabled = service.id in enabledIds,
                            onToggle = { onToggle(service.id, it) },
                            darkTheme = darkTheme
                        )
                    }
                }
                repeat(columnCount - rowServices.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

private enum class ServiceCatalogViewMode { LIST, GRID }

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageServicesScreen(params: ScreenParams) {
    val loadedViewMode by params.viewModel.loadedCatalogViewMode.collectAsStateWithLifecycle()

    BackHandler(enabled = !params.drawerActive) {
        params.navController.popBackStack()
    }

    Scaffold(
        topBar = {
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
                    val viewMode = when (loadedViewMode) {
                        null -> ServiceCatalogViewMode.GRID
                        true -> ServiceCatalogViewMode.LIST
                        false -> ServiceCatalogViewMode.GRID
                    }
                    val viewToggleInteractionSource = remember { MutableInteractionSource() }
                    val isViewTogglePressed by viewToggleInteractionSource.collectIsPressedAsState()
                    val viewToggleScale by animateFloatAsState(
                        targetValue = if (isViewTogglePressed) 0.88f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "view_toggle_scale"
                    )
                    IconButton(
                        onClick = { params.viewModel.setServicesCatalogListView(loadedViewMode != true) },
                        interactionSource = viewToggleInteractionSource,
                        modifier = Modifier.graphicsLayer {
                            scaleX = viewToggleScale
                            scaleY = viewToggleScale
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
                                    (scaleOut(
                                        targetScale = 0.8f,
                                        animationSpec = tween(120)
                                    ) + fadeOut(animationSpec = tween(120)))
                            },
                            label = "view_toggle_icon"
                        ) { mode ->
                            Icon(
                                imageVector = if (mode == ServiceCatalogViewMode.GRID) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                                contentDescription = if (mode == ServiceCatalogViewMode.GRID) stringResource(R.string.view_as_list) else stringResource(R.string.view_as_grid)
                            )
                        }
                    }
                },
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
                )
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
            val columnCount = ((maxWidth - horizontalInsetDp * 2) / minServiceCardWidthDp)
                .toInt()
                .coerceIn(2, 5)
            val listState = rememberLazyListState()
            if (loadedViewMode == null) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                val viewMode = if (loadedViewMode == true) ServiceCatalogViewMode.LIST else ServiceCatalogViewMode.GRID
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220))) togetherWith
                            (fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.92f, animationSpec = tween(180)))
                    },
                    label = "catalog_view"
                ) { mode ->
                    when (mode) {
                        ServiceCatalogViewMode.LIST -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(
                                        start = horizontalInsetDp,
                                        top = 12.dp,
                                        end = horizontalInsetDp,
                                        bottom = 24.dp
                                    )
                            ) {
                                ServiceCatalogListView(
                                    enabledIds = params.uiState.enabledServices,
                                    onToggle = { id, checked -> params.viewModel.toggleService(id, checked) },
                                    darkTheme = params.darkTheme
                                )
                            }
                        }
                        ServiceCatalogViewMode.GRID -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = horizontalInsetDp,
                                    top = 12.dp,
                                    end = horizontalInsetDp,
                                    bottom = 24.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                ServiceRegistry.groupedByCategory.forEach { (category, services) ->
                                    item(key = category) {
                                        ServiceCatalogSectionHeader(stringResource(category.titleResId), params.darkTheme)
                                    }
                                    item(key = "grid_${category.name}") {
                                        ServiceCatalogGrid(
                                            services = services,
                                            columnCount = columnCount,
                                            enabledIds = params.uiState.enabledServices,
                                            onToggle = { id, checked -> params.viewModel.toggleService(id, checked) },
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
            }
        }
    }
}
