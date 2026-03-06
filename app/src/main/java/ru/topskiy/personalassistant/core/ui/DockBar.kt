package ru.topskiy.personalassistant.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.topskiy.personalassistant.core.model.AppService
import ru.topskiy.personalassistant.core.model.ServiceId

private val DOCK_ITEM_WIDTH_DP = 76.dp
private val DOCK_ITEM_SPACING_DP = 8.dp
private val DOCK_EDGE_PADDING_DP = 8.dp
private const val DOCK_ITEM_ANIMATION_DURATION_MS = 220

@Composable
fun DockBar(
    dockServices: List<AppService>,
    currentServiceId: ServiceId,
    favoriteServiceId: ServiceId?,
    onSelectService: (ServiceId) -> Unit,
    dockListState: LazyListState
) {
    val n = dockServices.size
    if (n <= 1) return

    val dockBarBg = MaterialTheme.colorScheme.surface
    val dockBarBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val pillShape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = pillShape,
            color = dockBarBg,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 10.dp)
                .border(0.5.dp, dockBarBorder, pillShape)
                .clip(pillShape)
        ) {
            LaunchedEffect(currentServiceId, dockServices) {
                val index = dockServices.indexOfFirst { it.id == currentServiceId }
                if (index >= 0) {
                    dockListState.animateScrollToItem(index)
                }
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp),
                state = dockListState,
                contentPadding = PaddingValues(horizontal = DOCK_EDGE_PADDING_DP),
                horizontalArrangement = Arrangement.spacedBy(DOCK_ITEM_SPACING_DP),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(dockServices, key = { it.id }) { service ->
                    DockItem(
                        service = service,
                        selected = service.id == currentServiceId,
                        isFavorite = service.id == favoriteServiceId,
                        width = DOCK_ITEM_WIDTH_DP,
                        onClick = { onSelectService(service.id) }
                    )
                }
            }
        }
    }
}

/** Капсульная (pill) форма для элементов дока. */
private val DOCK_ITEM_SHAPE = RoundedCornerShape(999.dp)

private val dockItemColorTween = tween<Color>(durationMillis = DOCK_ITEM_ANIMATION_DURATION_MS, easing = FastOutSlowInEasing)

@Composable
private fun DockItem(
    service: AppService,
    selected: Boolean,
    isFavorite: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val selectedContentColor = MaterialTheme.colorScheme.primary
    val unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val indicatorColor = MaterialTheme.colorScheme.primaryContainer
    val targetContentColor = if (selected) selectedContentColor else unselectedContentColor
    val contentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = dockItemColorTween,
        label = "dock_item_content"
    )
    val bg by animateColorAsState(
        targetValue = if (selected) indicatorColor else Color.Transparent,
        animationSpec = dockItemColorTween,
        label = "dock_item_bg"
    )

    val favoriteModifier = when {
        selected -> Modifier
        isFavorite -> Modifier.border(
            0.5.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
            DOCK_ITEM_SHAPE
        )
        else -> Modifier
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(width)
            .then(favoriteModifier)
            .clip(DOCK_ITEM_SHAPE)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = service.icon,
            contentDescription = stringResource(service.titleResId),
            modifier = Modifier.size(22.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(service.titleResId),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
