package ru.topskiy.personalassistant.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.topskiy.personalassistant.core.model.AppService
import ru.topskiy.personalassistant.core.model.ServiceId
import ru.topskiy.personalassistant.ui.theme.DockBarBgDark
import ru.topskiy.personalassistant.ui.theme.DockBarBgLight
import ru.topskiy.personalassistant.ui.theme.DockBarBorderDark
import ru.topskiy.personalassistant.ui.theme.DockBarBorderLight
import ru.topskiy.personalassistant.ui.theme.DockBarSelectedDark
import ru.topskiy.personalassistant.ui.theme.DockBarSelectedLight
import ru.topskiy.personalassistant.ui.theme.DockBarUnselectedDark
import ru.topskiy.personalassistant.ui.theme.DockBarUnselectedLight
import ru.topskiy.personalassistant.ui.theme.FavoriteStarYellow

private val DOCK_ITEM_WIDTH_DP = 68.dp
private val DOCK_ITEM_SPACING_DP = 8.dp
private val DOCK_EDGE_PADDING_DP = 8.dp

@Composable
fun DockBar(
    dockServices: List<AppService>,
    currentServiceId: ServiceId,
    favoriteServiceId: ServiceId?,
    darkTheme: Boolean,
    onSelectService: (ServiceId) -> Unit,
    dockListState: LazyListState
) {
    val n = dockServices.size
    if (n <= 1) return

    val dockBarBg = if (darkTheme) DockBarBgDark else DockBarBgLight
    val dockBarBorder = if (darkTheme) DockBarBorderDark else DockBarBorderLight
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
                .shadow(2.dp, pillShape)
                .border(0.5.dp, dockBarBorder, pillShape)
                .clip(pillShape)
        ) {
            val density = androidx.compose.ui.platform.LocalDensity.current
            val itemWidthPx = with(density) { DOCK_ITEM_WIDTH_DP.toPx() }
            var viewportWidthPx by remember { mutableStateOf(0f) }

            LaunchedEffect(currentServiceId, dockServices, viewportWidthPx) {
                val index = dockServices.indexOfFirst { it.id == currentServiceId }
                if (index >= 0 && viewportWidthPx > 0f) {
                    val centerOffsetPx = ((viewportWidthPx - itemWidthPx) / 2f).toInt()
                    dockListState.animateScrollToItem(index, scrollOffset = -centerOffsetPx)
                }
            }

            LazyRow(
                modifier = Modifier
                    .padding(horizontal = 0.dp, vertical = 4.dp)
                    .onSizeChanged { viewportWidthPx = it.width.toFloat() },
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
                        darkTheme = darkTheme,
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

@Composable
private fun DockItem(
    service: AppService,
    selected: Boolean,
    isFavorite: Boolean,
    darkTheme: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val selectedColor = if (darkTheme) DockBarSelectedDark else DockBarSelectedLight
    val selectedBg = selectedColor.copy(alpha = 0.1f)
    val unselectedColor = if (darkTheme) DockBarUnselectedDark else DockBarUnselectedLight
    val contentColor = if (selected) selectedColor else unselectedColor
    val bg by animateColorAsState(
        targetValue = if (selected) selectedBg else Color.Transparent,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "dock_item_bg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(width)
            .then(
                if (isFavorite) Modifier.border(0.5.dp, FavoriteStarYellow, DOCK_ITEM_SHAPE)
                else Modifier
            )
            .clip(DOCK_ITEM_SHAPE)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
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
