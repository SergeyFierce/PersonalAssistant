package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val DRAWER_DRAG_THRESHOLD_PX = 40f

/** Ширина зоны у левого края экрана (в dp), в которой свайп вправо открывает drawer. */
internal val DRAWER_EDGE_ZONE_DP: Dp = 64.dp

private fun Modifier.drawerOpenGesture(onOpenDrawer: () -> Unit): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        val edgePx = with(density) { DRAWER_EDGE_ZONE_DP.toPx() }
        var dragStartX = 0f
        detectHorizontalDragGestures(
            onDragStart = { offset -> dragStartX = offset.x },
            onHorizontalDrag = { _, dragAmount ->
                if (dragStartX <= edgePx && dragAmount > DRAWER_DRAG_THRESHOLD_PX) {
                    onOpenDrawer()
                }
            }
        )
    }
)

/** Жест открытия drawer: свайп слева направо. Вешать на область контента (не на TopAppBar). */
internal fun Modifier.drawerOpenGestureOnContent(onOpenDrawer: () -> Unit): Modifier =
    drawerOpenGesture(onOpenDrawer)

/** Жест открытия drawer в зоне TopAppBar (свайп слева направо). */
internal fun Modifier.drawerOpenGestureInTopBar(onOpenDrawer: () -> Unit): Modifier =
    drawerOpenGesture(onOpenDrawer)
