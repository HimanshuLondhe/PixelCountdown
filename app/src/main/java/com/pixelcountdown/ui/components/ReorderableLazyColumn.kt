package com.pixelcountdown.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex

@Composable
fun <T> ReorderableLazyColumn(
    items: List<T>,
    onReorder: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    itemKey: (T) -> Any,
    itemContent: @Composable (T, Boolean) -> Unit
) {
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggedItemOffset by remember { mutableStateOf(0f) }
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        state = state,
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        state.layoutInfo.visibleItemsInfo
                            .firstOrNull { item ->
                                offset.y.toInt() in item.offset..(item.offset + item.size)
                            }?.also {
                                draggedItemIndex = it.index
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        draggedItemOffset += dragAmount.y

                        draggedItemIndex?.let { currentIndex ->
                            val targetIndex = calculateTargetIndex(state, currentIndex, draggedItemOffset)
                            if (targetIndex != null && targetIndex != currentIndex) {
                                val newList = items.toMutableList().apply {
                                    val item = removeAt(currentIndex)
                                    add(targetIndex, item)
                                }
                                onReorder(newList)
                                draggedItemIndex = targetIndex
                                draggedItemOffset = 0f
                            }
                        }
                    },
                    onDragEnd = {
                        draggedItemIndex = null
                        draggedItemOffset = 0f
                    },
                    onDragCancel = {
                        draggedItemIndex = null
                        draggedItemOffset = 0f
                    }
                )
            }
    ) {
        itemsIndexed(items, key = { _, item -> itemKey(item) }) { index, item ->
            val isDragging = index == draggedItemIndex
            val scale by animateFloatAsState(if (isDragging) 1.05f else 1.0f)
            val elevation by animateFloatAsState(if (isDragging) 8f else 0f)

            Box(
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) draggedItemOffset else 0f
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = elevation
                    }
            ) {
                itemContent(item, isDragging)
            }
        }
    }
}

private fun calculateTargetIndex(
    state: LazyListState,
    currentIndex: Int,
    offset: Float
): Int? {
    val layoutInfo = state.layoutInfo
    val currentItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentIndex } ?: return null
    val currentMid = currentItem.offset + currentItem.size / 2 + offset

    return layoutInfo.visibleItemsInfo
        .firstOrNull { item ->
            val itemMid = item.offset + item.size / 2
            if (offset > 0) {
                // Dragging down
                item.index > currentIndex && currentMid > itemMid
            } else {
                // Dragging up
                item.index < currentIndex && currentMid < itemMid
            }
        }?.index
}
