package ru.topskiy.personalassistant.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.topskiy.personalassistant.core.model.AppService
import ru.topskiy.personalassistant.core.model.ServiceId

@Composable
fun DockBar(
    dockServices: List<AppService>,
    currentServiceId: ServiceId,
    onSelectService: (ServiceId) -> Unit,
    dockListState: LazyListState
) {
    val cs = MaterialTheme.colorScheme
    val n = dockServices.size
    if (n <= 1) return

    val itemWidth = 84.dp
    val itemSpacing = 12.dp
    val dockItemFromTapeEdge = 22.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val pillShape = RoundedCornerShape(999.dp)

        Surface(
            shape = pillShape,
            color = cs.surface.copy(alpha = 0.82f),
            tonalElevation = 0.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 10.dp)
                .clip(pillShape)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(cs.surface.copy(alpha = 0.70f))
                    ) {
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val itemWidthPx = with(density) { itemWidth.toPx() }
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
                                .padding(horizontal = 0.dp, vertical = 10.dp)
                                .onSizeChanged { viewportWidthPx = it.width.toFloat() },
                            state = dockListState,
                            contentPadding = PaddingValues(horizontal = dockItemFromTapeEdge),
                            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(dockServices, key = { it.id }) { service ->
                                DockItem(service, service.id == currentServiceId, itemWidth) {
                                    onSelectService(service.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockItem(
    service: AppService,
    selected: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "dock_item_scale"
    )
    val bg by animateColorAsState(
        targetValue = if (selected) cs.primaryContainer else Color.Transparent,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "dock_item_bg"
    )
    val contentColor =
        if (selected) cs.onPrimaryContainer
        else cs.onSurfaceVariant.copy(alpha = 0.85f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(width)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = service.icon,
            contentDescription = stringResource(service.titleResId),
            modifier = Modifier.size(28.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(service.titleResId),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
