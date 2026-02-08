package ru.topskiy.personalassistant.core.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

internal const val DRAWER_DRAG_THRESHOLD_PX = 40f

internal fun Modifier.drawerOpenGestureInTopBar(onOpenDrawer: () -> Unit): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        val edgePx = with(density) { 64.dp.toPx() }
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
