package io.kanro.compose.jetbrains.control

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.rememberCursorPositionProvider
import io.kanro.compose.jetbrains.JBTheme

private suspend fun AwaitPointerEventScope.awaitEventFirstDown(): PointerEvent {
    var event: PointerEvent
    do {
        event = awaitPointerEvent()
    } while (
        !event.changes.all { it.changedToDown() }
    )
    return event
}

private fun Modifier.contextMenuDetector(
    state: ContextMenuState,
    enabled: Boolean = true,
): Modifier {
    return if (
        enabled && state.status == ContextMenuState.Status.Closed
    ) {
        this.pointerInput(state) {
            forEachGesture {
                awaitPointerEventScope {
                    val event = awaitEventFirstDown()
                    if (event.buttons.isSecondaryPressed) {
                        event.changes.forEach { it.consumeDownChange() }
                        state.status =
                            ContextMenuState.Status.Open(Rect(event.changes[0].position, 0f))
                    }
                }
            }
        }
    } else {
        Modifier
    }
}

@OptIn(ExperimentalFoundationApi::class)
val LocalContextMenuRepresentation:
        ProvidableCompositionLocal<ContextMenuRepresentation> = staticCompositionLocalOf {
    JBContextMenuRepresentation(Color.Unspecified, Color.Unspecified)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContextMenuArea(
    items: () -> List<ContextMenuItem>,
    state: ContextMenuState = remember { ContextMenuState() },
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(Modifier.contextMenuDetector(state, enabled), propagateMinConstraints = true) {
        content()
    }
    LocalContextMenuRepresentation.current.Representation(state, items)
}

@OptIn(ExperimentalComposeUiApi::class)
class JBContextMenuRepresentation(
    private val backgroundColor: Color,
    private val borderColor: Color,
) : ContextMenuRepresentation {

    @Composable
    override fun Representation(state: ContextMenuState, items: () -> List<ContextMenuItem>) {
        val isOpen = state.status is ContextMenuState.Status.Open
        if (isOpen) {
            Popup(
                focusable = true,
                onDismissRequest = { state.status = ContextMenuState.Status.Closed },
                popupPositionProvider = rememberCursorPositionProvider(),
                onKeyEvent = {
                    if (it.key == Key.Escape) {
                        state.status = ContextMenuState.Status.Closed
                        true
                    } else {
                        false
                    }
                },
            ) {
                val backgroundColor = if (backgroundColor.isUnspecified) {
                    JBTheme.panelColors.bgContent
                } else backgroundColor
                val borderColor = if (borderColor.isUnspecified) {
                    JBTheme.panelColors.border
                } else borderColor
                Column(
                    Modifier.background(backgroundColor)
                        .border(1.dp, borderColor)
                        .width(IntrinsicSize.Max)
                ) {
                    items().forEach {
                        DropdownMenuItem(
                            modifier = Modifier.defaultMinSize(79.dp, 21.dp),
                            onClick = {
                                it.onClick()
                                state.status = ContextMenuState.Status.Closed
                            },
                        ) {
                            Text(it.label, Modifier.padding(horizontal = 6.dp))
                        }
                    }
                }
            }
        }

    }
}